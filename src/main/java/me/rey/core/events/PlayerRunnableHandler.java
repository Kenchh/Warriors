package me.rey.core.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.rey.core.Warriors;
import me.rey.core.players.EnergyHandler;
import me.rey.core.players.User;

public class PlayerRunnableHandler extends BukkitRunnable {
	
	private Plugin plugin;
	private EnergyHandler energyHandler = new EnergyHandler();
	
	public PlayerRunnableHandler(Plugin plugin) {
		this.plugin = plugin;
		this.start();
	}
	
	public void start() {
		this.runTaskTimer(plugin, 0, 1);
	}
	
	@Override
	public void run() {
		
		for(Player p : Bukkit.getOnlinePlayers()) {
			
			User user = new User(p);
			
			// ENERGY
			if(!user.getPlayer().isDead()){
				if(user.getEnergy() <= EnergyHandler.MAX_ENERGY && !energyHandler.isEnergyPaused(user.getUniqueId()))
					energyHandler.setEnergy(user.getUniqueId(), user.getEnergy()+0.4);
				
				user.getPlayer().setExp(user.getEnergyExp());
			}
			
			// CLASS UPDATE
			user.updateClassEffects();
			if(Warriors.userCache.containsKey(p)) {
				
				if(user.getWearingClass() == null) {
					Warriors.userCache.remove(p);
					user.resetEffects();
					user.sendMessageWithPrefix("Class", "You took off your armor set.");
					continue;
				}
				
				if(!Warriors.userCache.get(p).equals(user.getWearingClass())) {
					Warriors.userCache.remove(p);
					Warriors.userCache.put(p, user.getWearingClass());
					user.resetEffects();
					user.sendMessageWithPrefix("Class", "You took off your armor set.");
					user.sendMessageWithPrefix("Class", "You equipped &e" + user.getWearingClass().getName() + "&7.");
					user.sendBuildEquippedMessage(user.getWearingClass());
					continue;
				}
				
				
			} else {
				
				if(user.getWearingClass() == null) continue;
				
				Warriors.userCache.put(p, user.getWearingClass());
				user.resetEffects();
				user.sendMessageWithPrefix("Selector", "You equipped &e" + user.getWearingClass().getName() + "&7.");
				user.sendBuildEquippedMessage(user.getWearingClass());
				
			}
			
		}
	}
	
}
