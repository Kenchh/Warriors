package me.rey.core.events;

import java.util.HashMap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.players.User;
import me.rey.core.pvp.Build;

public class EquipClassEvent implements Listener {
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Warriors.userCache.remove(e.getPlayer());
		if(new User(e.getPlayer()).getWearingClass() != null) {
			User user = new User(e.getPlayer());
			Warriors.userCache.put(e.getPlayer(), user.getWearingClass());
			user.sendMessageWithPrefix("Class", "You equipped &e" + new User(e.getPlayer()).getWearingClass().getName() + "&7.");
			
			user.sendBuildEquippedMessage(user.getWearingClass());
		}
		
		Warriors.buildCache.remove(e.getPlayer());
		HashMap<ClassType, Build> selectedBuilds = new HashMap<>();
		for(ClassType ct : ClassType.values()) {
			if(new User(e.getPlayer()).getSelectedBuild(ct) != null) {
				selectedBuilds.put(ct, new User(e.getPlayer()).getSelectedBuild(ct));
			}
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Warriors.userCache.remove(e.getPlayer());
		Warriors.buildCache.remove(e.getPlayer());
	}

}
