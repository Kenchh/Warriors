package me.rey.core.utils;

import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class UtilEnt {

	public static boolean isGrounded(Entity ent) {
	    if (ent instanceof CraftEntity) {
	      return ((CraftEntity)ent).getHandle().onGround;
	    }
	    return ent.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isSolid();
	}
	
}
