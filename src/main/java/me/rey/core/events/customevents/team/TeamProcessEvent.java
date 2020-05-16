package me.rey.core.events.customevents.team;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TeamProcessEvent extends Event {
	
	private static final HandlerList HANDLERS = new HandlerList();
	
	private Set<Player> teammates;
	private Player player;
	
	public TeamProcessEvent(Player player, Player... teammates) {
		this.player = player;
		this.teammates = teammates != null && teammates.length >=1 ? new HashSet<Player>(Arrays.asList(teammates)) : new HashSet<>();
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
	
	public Set<Player> getTeammates(){
		return teammates;
	}
	
	public void addTeammate(Player player) {
		this.teammates.add(player);
	}
	
	public void removeTeammate(Player player) {
		this.removeTeammate(player);
	}
}
