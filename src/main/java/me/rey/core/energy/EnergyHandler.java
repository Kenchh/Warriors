package me.rey.core.energy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import me.rey.core.enums.State;

public class EnergyHandler {

	public static double MAX_ENERGY = 180, INCREMENT = 0.4, PER_SECOND = INCREMENT * 20;
	private static HashMap<UUID, Double> energy = new HashMap<>(), extraCapacity = new HashMap<>(), energySpeed = new HashMap<>();
	private static Set<UUID> paused = new HashSet<>();
	
	public double getUserEnergy(UUID player) {
		return energy.containsKey(player) ? energy.get(player) : 0;
	}
	
	public void setEnergy(UUID player, double value) {
		energy.put(player, Math.max(Math.min(value, this.getCapacity(player)), 0));
	}
	
	public void togglePauseEnergy(State state, UUID player) {
		if(state == State.ENABLED)
			paused.add(player);
		else
			paused.remove(player);
	}
	
	public boolean isEnergyPaused(UUID player) {
		return paused.contains(player);
	}
	
	public boolean hasExtraSpeed(UUID player) {
		return energySpeed.containsKey(player);
	}
	
	public double getSpeed(UUID player) {
		return hasExtraSpeed(player) ? energySpeed.get(player) + 1 : 1;
	}
	
	public boolean hasExtraCapacity(UUID player) {
		return extraCapacity.containsKey(player);
	}
	
	public double getCapacity(UUID player) {
		return hasExtraCapacity(player) ? extraCapacity.get(player) + MAX_ENERGY : MAX_ENERGY;
	}
	
	public void resetCapacity(UUID player) {
		extraCapacity.remove(player);
	}
	
	public void setExtraCapacity(UUID player, double value) {
		extraCapacity.put(player, value);
	}
	
	public void resetSpeed(UUID player) {
		energySpeed.remove(player);
	}
	
	public void setExtraSpeed(UUID player, double value) {
		energySpeed.put(player,value);
	}
}
