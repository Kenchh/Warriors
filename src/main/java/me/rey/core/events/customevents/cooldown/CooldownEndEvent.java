package me.rey.core.events.customevents.cooldown;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CooldownEndEvent extends Event {
	
	private final Player player;
	
	public CooldownEndEvent(Player player) {
		this.player = player;
	}
	
	private static final HandlerList HANDLERS = new HandlerList();
	
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
}
