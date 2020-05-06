package me.rey.core.classes.abilities;

import org.bukkit.entity.Player;

public interface IBowPreparable {
	
	/*
	 * PREPARING AND UNPREPARING
	 */
	boolean prepare(Player player);
	boolean isPrepared(Player player);
	boolean unprepare(Player player);
	
	
	/*
	 * SHOOTING AND UNSHOOTING
	 */
	boolean shoot(Player player);
	boolean hasShot(Player player);
	boolean unshoot(Player player);
}
