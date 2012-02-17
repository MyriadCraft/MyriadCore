package me.meiamsome.myriadbase;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public interface BlockTracker {
	public void addListener(Material listenFor);
	public ArrayList<Block> getFound();
	public void setCallbacks(int[] ids, String[] functions) throws SecurityException, NoSuchMethodException;
	public boolean canPlace(BlockPlaceEvent event);
	public boolean canDestroy(BlockBreakEvent event);
	public void move(BlockPistonExtendEvent event);
	public boolean canMove(BlockPistonExtendEvent event);
}
