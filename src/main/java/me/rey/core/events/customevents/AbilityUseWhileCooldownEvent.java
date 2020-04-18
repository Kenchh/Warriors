package me.rey.core.events.customevents;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.rey.core.classes.abilities.Ability;

public class AbilityUseWhileCooldownEvent extends Event implements Cancellable{
	
	private final Player player;
	private final Ability ability;
	private boolean isCancelled;
	private final int level;
	private boolean cancelMessage;
	
	public AbilityUseWhileCooldownEvent(Player player, Ability ability, int level) {
		this.ability = ability;
		this.player = player;
		this.level = level;
		this.isCancelled = false;
		this.cancelMessage = false;
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
	
	public Ability getAbility() {
		return ability;
	}

	public int getLevel() {
		return level;
	}
	
	@Override
	public boolean isCancelled() {
		return this.isCancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.isCancelled = cancelled;
	}
	
	public void cancelMessage(boolean cancel) {
		this.cancelMessage = cancel;
	}
	
	public boolean isMessageCancelled() {
		return this.cancelMessage;
	}
}
