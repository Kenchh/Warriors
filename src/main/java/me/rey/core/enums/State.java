package me.rey.core.enums;

import org.bukkit.ChatColor;

import me.rey.core.utils.Text;

public enum State {

	ENABLED(ChatColor.GREEN),
	DISABLED(ChatColor.RED),
	PAUSED(ChatColor.YELLOW);
	
	private ChatColor color;
	
	State(ChatColor color){
		this.color = color;
	}
	
	public String getName() {
		return Text.color(color + Text.formatName(this.name()));
	}
	
}
