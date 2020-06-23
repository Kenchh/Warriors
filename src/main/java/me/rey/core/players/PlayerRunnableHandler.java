package me.rey.core.players;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.rey.core.energy.EnergyHandler;
import me.rey.core.events.customevents.update.EnergyUpdateEvent;
import me.rey.core.events.customevents.update.UpdateEvent;

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
		
		UpdateEvent updateEvent = new UpdateEvent();
		Bukkit.getServer().getPluginManager().callEvent(updateEvent);
		
		for(Player p : Bukkit.getOnlinePlayers()) {
			
			User user = new User(p);
			
			// ENERGY
			if(!user.getPlayer().isDead()) {
				EnergyUpdateEvent e = new EnergyUpdateEvent(EnergyHandler.INCREMENT, p, user.getEnergy(), energyHandler);
				Bukkit.getServer().getPluginManager().callEvent(e);
				
				double toSet = user.getEnergy();
				if(!energyHandler.isEnergyPaused(user.getUniqueId()) && user.getEnergy() <= energyHandler.getCapacity(user.getUniqueId())
						&& !energyHandler.isEnergyPaused(user.getUniqueId())) {
					
					toSet += (e.getIncrement() * energyHandler.getSpeed(user.getUniqueId()));
				}
				energyHandler.setEnergy(user.getUniqueId(), toSet);
				
				user.getPlayer().setExp(user.getEnergyExp());

				energyHandler.resetSpeed(user.getUniqueId());
				energyHandler.resetCapacity(user.getUniqueId());
			}
			// END
			
		}
	}
	
}
