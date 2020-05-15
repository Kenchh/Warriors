package me.rey.core.events.customevents.combat;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.rey.core.players.combat.CombatTimer;

public class CombatTimerTickEvent extends Event {
	
	private final Player player;
	private CombatTimer timer;
	
	public CombatTimerTickEvent(Player player, CombatTimer timer) {
		this.player = player;
		this.timer = timer;
	}
	
	private static final HandlerList HANDLERS = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
	
	public CombatTimer getTimer() {
		return timer;
	}
	
	public Player getPlayer() {
		return player;
	}

}
