package me.meiamsome.myriadcore;

import me.meiamsome.myriadbase.CoreNotConnectedException;
import me.meiamsome.myriadbase.SignTracker;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class RealSignTracker extends SignTracker {
	MyriadCore par;
	String[] match={null,null,null,null};
	String[] perms={null,null,null};
	/* 0 - Create
	 * 1 - Create Any
	 * 2 - Delete Any
	 */
	RealSignTracker(MyriadCore parent) throws CoreNotConnectedException {
		par=parent;
		track=par.newScan(Material.SIGN,this);
		track.addListener(Material.WALL_SIGN);
		track.addListener(Material.SIGN_POST);
		try {
			track.setCallbacks(new int[]{0,2,3,4}, new String[]{"found","created","destroyed","canMove"});
		} catch (Exception e) {/*This totally never happens.*/}
	}
	String getOwner(Sign s) {
		for(int i=0;i<4;i++) if(match[i].equals("#")) return s.getLine(i);
		return null;
	}
	void setOwner(Sign s, String username) {
		for(int i=0;i<4;i++) if(match[i].equals("#")) s.setLine(i,username);
	}
	public Boolean found(Block b) {//Sign find callback - return true adds to list
		if(!(b.getState() instanceof Sign)) return false;
		for(int i=0;i<4;i++) if(match[i]!=null && !match[i].equals("#") && !((Sign)b).getLine(i).matches(match[i]))	return false;
		//Now we know we found a sign that's ours
		return true;
	}
	public Boolean created(BlockPlaceEvent bpe) {
		if(perms[0]!=null) if(!bpe.getPlayer().hasPermission(perms[0])) return false;
		if(perms[1]==null || !bpe.getPlayer().hasPermission(perms[1])) setOwner((Sign) bpe.getBlock().getState(),bpe.getPlayer().getName());
		par.broadcastMessage("New sign!");
		return true;
	}
	public Boolean destroyed(BlockBreakEvent bbe) {
		if(getOwner((Sign) bbe.getBlock().getState())!=bbe.getPlayer().getName()){
			if(perms[2]!=null) {
				if(!bbe.getPlayer().hasPermission(perms[2])) return false;
			} else if(!bbe.getPlayer().isOp()) return false;
		}
		return true;
	}
	public Boolean canMove(BlockPistonEvent bpe) {
		return false;
	}
	public void setCheck(String[] mat) {
		for(int i=0;i<mat.length && i<4;i++) 
			if(mat[i]!=null) match[i]=mat[i];
	}
	public void setPerms(String[] mat) {
		for(int i=0;i<perms.length && i<mat.length;i++) 
			if(mat[i]!=null) perms[i]=mat[i];
	}
}
