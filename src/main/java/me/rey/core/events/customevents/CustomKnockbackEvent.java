package me.rey.core.events.customevents;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CustomKnockbackEvent extends Event implements Cancellable {

	
	Entity damagee, damager;
	double damage, multiplier;
	boolean cancelled;
	
	public CustomKnockbackEvent(Entity damagee, Entity damager, double damage, double multiplier) {
		this.damagee = damagee;
		this.damager = damager;
		this.damage = damage;
		this.multiplier = multiplier;
		this.cancelled = false;
	}
	
	private static final HandlerList HANDLERS = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	
	public double getMult() {
		return multiplier;
	}

	public void setMult(double multiplier) {
		this.multiplier = multiplier;
	}

	public Entity getDamagee() {
		return damagee;
	}

	public Entity getDamager() {
		return damager;
	}

	public double getDamage() {
		return damage;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	
}
