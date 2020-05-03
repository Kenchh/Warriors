package me.rey.core.classes.abilities;

import org.bukkit.entity.Player;

public interface IConstant {
	
	
	public interface ITogglable {
		
		public boolean off(Player p);
		public boolean on(Player p);
		
	}
	
	public interface IDroppable {
		
	}
}
