package me.meiamsome.myriadcore;

import java.util.ArrayList;
import java.util.HashMap;

import me.meiamsome.myriadbase.BlockTracker;
import me.meiamsome.myriadbase.LivingTracker;
import me.meiamsome.myriadbase.MyriadPlugin;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.PluginManager;


public class WorldTracker implements Runnable, Listener {
	Chunk loading = null;
	int loadPos=0;
	long timeout = 10, lastrescan=0;
	int scans=0;
	boolean rscan=false;
	int rescan=0;
	int rescantime=100000;
	MyriadCore par;
	ArrayList<WorldScanner<Block>> blockSearches = new ArrayList<WorldScanner<Block>>();
	ArrayList<WorldScanner<LivingEntity>> liveSearches = new ArrayList<WorldScanner<LivingEntity>>();
	HashMap<Integer, WorldScanner<?>> scanners = new HashMap<Integer, WorldScanner<?>>();
	ArrayList<Chunk> scannedChunks = new ArrayList<Chunk>();
	ArrayList<Chunk> loadQueue = new ArrayList<Chunk>();
	ArrayList<Chunk> unloadQueue = new ArrayList<Chunk>();
	WorldTracker(MyriadCore parent, PluginManager pm) {
		pm.registerEvents(this, parent);
		par=parent;
		par.getServer().getScheduler().scheduleSyncRepeatingTask(par, this, 10, 10);
	}
	@Override
	public void run() {
		long start=System.currentTimeMillis();
		while(System.currentTimeMillis()-start<timeout && !unloadQueue.isEmpty()) {
			unload(unloadQueue.get(0));
			unloadQueue.remove(0);
		} 
		//if(unloadQueue.size()>0) par.log.info("[MyriadCore] [Debug] "+unloadQueue.size()+" chunks need removing.");
		//par.log.info("[MyriadCore] [Debug] "+rescan+" rescan of "+scannedChunks.size()+" chunks");
		while(System.currentTimeMillis()-start<timeout) {
			if(loading==null) {
				if(!loadQueue.isEmpty()) {
					loading=loadQueue.get(0);
					loadPos=0;
					loadQueue.remove(0);
				} else {
					if(rescan>-1) {
						if(scannedChunks.size()<rescan) {
							rescan=-1;
						} else if(scannedChunks.size()>0){
							rescan++;
							Chunk ch=scannedChunks.get(0);
							scannedChunks.remove(0);
							loading=ch;
							loadPos=0;
						}
					} else {
						if(rscan || System.currentTimeMillis()-lastrescan>rescantime) {
							rescan=0;
							rscan=false;
						}
					}
				}

				//if(par.debug && loading==null) par.log.info("[MyriadCore] [Debug] Done.");
			} else {
				if(!blockSearches.isEmpty() && loadPos<0x10000) {
					int a=Math.min(0x10000,loadPos+0xFF);
					Block b;
					while(loadPos<a) {
						b=loading.getBlock((loadPos&0xF), ((loadPos&0xFF00)/0x100), ((loadPos&0xF0)/0x10));
						for(WorldScanner<Block> sc: blockSearches)
							if(sc.isIncluded(b)) 
								sc.found(b);
						loadPos++;
					}
					//if(par.debug && loading!=null) par.log.info("[MyriadCore] [Debug] loading chunk at "+loading.getX()+", "+loading.getZ()+" "+(loadPos/0x9000)+"%");
				} else {//Now do all the living entities!
					if(liveSearches.size()>0) for(Entity e:loading.getEntities()) {
						if(!(e instanceof LivingEntity)) continue;
						for(WorldScanner<LivingEntity> scanner :liveSearches) {
							if(scanner.isIncluded(e)) scanner.found((LivingEntity)e);
						}
					}
					scannedChunks.add(loading);
					//if(par.debug) par.log.info("[MyriadCore] [Debug] Loaded chunk at "+loading.getX()+", "+loading.getZ());
					loading=null;
				}
			}
		}
		if(loading!=null) lastrescan=System.currentTimeMillis();
	}
	private void unload(Chunk ch) {
		if(!(scannedChunks.contains(ch) || ch.equals(loading))) return;
		if(ch.equals(loading)) loading=null;
		//if(par.debug) par.log.info("[MyriadCore] [Debug] Unloaded chunk at "+ch.getX()+", "+ch.getZ());
		scannedChunks.remove(ch);
		if(rescan>=0) rescan-=1;
	}
	public LivingTracker newScan(Class<? extends LivingEntity> c, Object creator, MyriadPlugin mp) {
		if(!LivingEntity.class.isAssignableFrom(c)) return null;
		WorldLivingScanner scanner=new WorldLivingScanner(creator, mp, par);
		scanner.addListener(c);
		if(par.debug) par.log.info("[MyriadCore] [Debug] New living entity scanner");
		rscan=true;
		return (LivingTracker) scanner;
	}
	public BlockTracker newScan(Material a, Object creator, MyriadPlugin mp) {
		WorldBlockScanner scanner=new WorldBlockScanner(creator, mp, par);
		scanner.addListener(a);
		if(par.debug) par.log.info("[MyriadCore] [Debug] New block scanner");
		rscan=true;
		return (BlockTracker) scanner;
	}	

	@EventHandler (priority=EventPriority.MONITOR)
	public void onChunkLoad(ChunkLoadEvent event) {loadQueue.add(event.getChunk());}
	@EventHandler (priority=EventPriority.MONITOR)
	public void onChunkUnload(ChunkUnloadEvent event) {unloadQueue.add(event.getChunk());}
	@EventHandler (priority=EventPriority.MONITOR)
	public void onChunkPopulate(ChunkPopulateEvent event) {}
	@EventHandler (priority=EventPriority.MONITOR)
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		for(WorldScanner<LivingEntity> ls: liveSearches) {
			if(ls.isIncluded(event.getEntity())) ls.found((LivingEntity) event.getEntity());
		}
	}
	@EventHandler (priority=EventPriority.MONITOR)
	public void onEntityDeath(EntityDeathEvent event) {
		for(WorldScanner<LivingEntity> ls: liveSearches) {
			((LivingTracker)ls).death(event);
		}
	}
	@EventHandler (priority=EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event) {
		boolean cancel = event.isCancelled();
		for(WorldScanner<Block> sc: blockSearches) if(!((BlockTracker)sc).canDestroy(event)) cancel=true;
		event.setCancelled(cancel);
		if(cancel) return;
		for(WorldScanner<Block> sc: blockSearches) sc.remove(event.getBlock());
	}
	@EventHandler (priority=EventPriority.HIGH)
	public void onBlockPlace(BlockPlaceEvent event) {
		boolean cancel = event.isCancelled();
		for(WorldScanner<Block> sc: blockSearches) if(!((BlockTracker)sc).canPlace(event)) cancel=true;
		event.setCancelled(cancel);
		if(cancel) return;
		for(WorldScanner<Block> sc: blockSearches) if(sc.isIncluded(event.getBlock())) sc.found(event.getBlock());
	}
	@EventHandler (priority=EventPriority.HIGH)
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		boolean cancel = event.isCancelled();
		for(WorldScanner<Block> sc: blockSearches) if(!((BlockTracker)sc).canMove(event)) cancel=true;
		event.setCancelled(cancel);
		if(cancel) return;
		for(WorldScanner<Block> sc: blockSearches) ((BlockTracker)sc).move(event);
	}
}