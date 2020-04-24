package me.rey.core.players;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EnergyHandler {

	public static double MAX_ENERGY = 180;
	private static HashMap<UUID, Double> energy = new HashMap<>();
	private static Set<UUID> paused = new HashSet<>();
	
	public double getUserEnergy(UUID player) {
		return energy.containsKey(player) ? energy.get(player) : 0;
	}
	
	public void setEnergy(UUID player, double value) {
		energy.put(player, Math.max(Math.min(value, 180), 0));
	}
	
	public void togglePauseEnergy(UUID player) {
		if(isEnergyPaused(player))
			paused.remove(player);
		else
			paused.add(player);
	}
	
	public boolean isEnergyPaused(UUID player) {
		return paused.contains(player);
	}
}
