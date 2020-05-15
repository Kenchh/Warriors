package me.rey.core.events.customevents.combat;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;

import me.rey.core.players.combat.DeathMessage;
import me.rey.core.players.combat.PlayerHit;

public class DeathEvent extends Event {

	private final Player dead;
	private final PlayerHit lastBlow;
	private boolean cancelDeathMessage;
	private DeathMessage deathMessage;
	
	public DeathEvent(Player dead, PlayerHit lastBlow, int assists, EntityDamageEvent event) {
		this.dead = dead;
		this.lastBlow = lastBlow;
		this.cancelDeathMessage = false;
		this.deathMessage = null;
		this.deathMessage = new DeathMessage(dead, lastBlow, assists, event);
	}
	
	private static final HandlerList HANDLERS = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
	
	public PlayerHit getLastHit() {
		return lastBlow;
	}
	
	public Player getPlayer() {
		return dead;
	}
	
	public void cancelDeathMessage(boolean cancel) {
		this.cancelDeathMessage = cancel;
	}
	
	public boolean isDeathMessageCanceled() {
		return cancelDeathMessage;
	}
	
	public DeathMessage getDeathMessage() {
		return this.deathMessage;
	}

}
