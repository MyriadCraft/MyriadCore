package me.meiamsome.myriadcore;

import java.util.ArrayList;

import me.meiamsome.myriadbase.BlockTracker;
import me.meiamsome.myriadbase.MyriadPlugin;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class WorldBlockScanner extends WorldScanner<Block> implements BlockTracker{

	ArrayList<Material> mats = new ArrayList<Material>(); 
	WorldBlockScanner(Object creator, MyriadPlugin plugin, MyriadCore mCore) {
		super(Block.class, creator, plugin, mCore);
		mCore.wt.blockSearches.add(this);
	}

	public ArrayList<Block> getFound() {
		return foundThings;
	}

	public void addListener(Material listenFor) {
		mats.add(listenFor);
	}

	@Override
	void remove(Chunk c) {
		for(Block b:foundThings) {
			if(b.getChunk().equals(c)) foundThings.remove(b);
		}
	}

	@Override
	boolean isIncluded(Object o) {
		if(!(o instanceof Block)) return false;
		return mats.contains(((Block)o).getType());
	}
	Class<?>[] getArgs(int method) {
		switch(method) {
			case 2:return new Class<?>[]{BlockPlaceEvent.class};
			case 3:return new Class<?>[]{BlockBreakEvent.class};
			case 4:return new Class<?>[]{BlockPistonExtendEvent.class};
		}
		return super.getArgs(method);
	}
	@Override
	public boolean canPlace(BlockPlaceEvent event) {
		return run(2, new Object[]{event});
	}

	@Override
	public boolean canDestroy(BlockBreakEvent event) {
		return run(3,new Object[]{event});
	}

	@Override
	public void move(BlockPistonExtendEvent event) {
		//Does anything need to happen here?
	}

	@Override
	public boolean canMove(BlockPistonExtendEvent event) {
		return run(4, new Object[]{event});
	}

}
