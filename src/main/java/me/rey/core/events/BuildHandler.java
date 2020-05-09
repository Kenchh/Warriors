package me.rey.core.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.rey.core.Warriors;
import me.rey.core.players.User;

public class BuildHandler implements Listener {
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {

		e.getPlayer().setWalkSpeed(0.2f);

		Warriors.userCache.remove(e.getPlayer());
		if(new User(e.getPlayer()).getWearingClass() != null) {
			User user = new User(e.getPlayer());
			Warriors.userCache.put(e.getPlayer(), user.getWearingClass());
			user.sendMessageWithPrefix("Class", "You equipped &e" + new User(e.getPlayer()).getWearingClass().getName() + "&7.");
			
			user.sendBuildEquippedMessage(user.getWearingClass());
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Warriors.userCache.remove(e.getPlayer());
	}

}
