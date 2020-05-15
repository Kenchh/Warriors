package me.rey.core.events.customevents.ability;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.rey.core.classes.abilities.Ability;
import me.rey.core.enums.AbilityFail;

public class AbilityFailEvent extends Event implements Cancellable{
	
	private final Player player;
	private final Ability ability;
	private boolean isCancelled;
	private final int level;
	private boolean message;
	private AbilityFail fail;
	
	public AbilityFailEvent(AbilityFail fail, Player player, Ability ability, int level) {
		this.fail = fail;
		this.ability = ability;
		this.player = player;
		this.level = level;
		this.isCancelled = false;
		this.message = false;
	}
	
	private static final HandlerList HANDLERS = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
	
	public AbilityFail getFail() {
		return this.fail;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public int getLevel() {
		return level;
	}
	
	public Ability getAbility() {
		return ability;
	}
	
	public void setMessageCancelled(boolean message) {
		this.message = message;
	}
	
	public boolean isMessageCancelled() {
		return message;
	}

	@Override
	public boolean isCancelled() {
		return this.isCancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.isCancelled = cancelled;
	}

}
