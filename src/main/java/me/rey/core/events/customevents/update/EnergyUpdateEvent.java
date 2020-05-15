package me.rey.core.events.customevents.update;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.rey.core.energy.EnergyHandler;

public class EnergyUpdateEvent extends Event {
	
	private static final HandlerList HANDLERS = new HandlerList();
	
	private double energy, extraCapacity, extraSpeed;
	private Player player;
	private EnergyHandler handler;
	
	public EnergyUpdateEvent(Player player, double energy, EnergyHandler handler) {
		this.player = player;
		this.energy = energy;
		this.extraCapacity = 0;
		this.extraSpeed = 0;
		this.handler = handler;
	}
	
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public double getEnergy() {
		return energy;
	}
	
	public double getExtraCapacity() {
		return extraCapacity;
	}
	
	public double getExtraSpeed() {
		return extraSpeed;
	}
	
	public void addExtraSpeed(double speed) {
		this.extraSpeed += speed;
		handler.setExtraSpeed(player.getUniqueId(), extraSpeed);
	}
	
	public void addExtraCapacity(double capacity) {
		this.extraCapacity += capacity;
		handler.setExtraCapacity(player.getUniqueId(), extraCapacity);
	}
	

}
