package me.rey.core.utils;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.rey.core.events.customevents.cooldown.CooldownEndEvent;

public abstract class Cooldown {
	
	private Map<Player, Double> cooldowns; 
	private String message, resetMessage;
	private Sound sound;
	private float pitch;
	private Plugin plugin;
	
	public Cooldown(Plugin plugin, String message) {
		this.cooldowns = new HashMap<>();
		this.message = message;
		this.resetMessage = message;
		this.plugin = plugin;

	}
	
	public Sound getSound() {
		return this.sound;
	}
	
	public float getPitch() {
		return this.pitch;
	}
	
	public void setSound(Sound sound, float pitch) {
		this.sound = sound;
		this.pitch = pitch;
	}
	
	public void setMessage(String text) {
		this.message = text;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public void resetMessage() {
		this.message = resetMessage;
	}
	
	public Map<Player, Double> getCooldowns(){
		return this.cooldowns;
	}
	
	public void setCooldownForPlayer(Player player, double cooldown, String message) {
		this.cooldowns.put(player, cooldown);
		
		double ticks = 1;
		new BukkitRunnable() {
			double cd = cooldown;
			@Override
			public void run() {
				
				if(cd <= 0) {
					clearPlayerCooldown(player);
				}
				
				if(!hasCooldown(player)) {
					if(getMessage() != null) {
						player.sendMessage(getMessage());
					}
					
					if(getSound() != null) {
						player.playSound(player.getLocation(), getSound(), 1F, getPitch());
					}
					
					CooldownEndEvent event = new CooldownEndEvent(player);
					Bukkit.getServer().getPluginManager().callEvent(event);
					
					resetMessage();
					this.cancel();
					return;
				}
				cooldowns.put(player, Double.parseDouble(new DecimalFormat("#.#").format(cd).replace(",", ".")));
				cd = cd - (ticks / 20);
			}
		}.runTaskTimer(plugin, 0, (int) ticks);
	}
	
	public void setCooldownForPlayer(Player player, double cooldown) {
		setCooldownForPlayer(player, cooldown, null);
	}
	
	public void clearPlayerCooldown(Player player, String message) {
		if(message != null) {
			player.sendMessage(message);
		}
		this.cooldowns.remove(player);
	}
	
	public void clearPlayerCooldown(Player player) {
		clearPlayerCooldown(player, null);
	}
	
	public double getPlayerCooldown(Player player) {
		return this.hasCooldown(player) ? this.cooldowns.get(player) : 0;
	}
	
	public boolean hasCooldown(Player player) {
		return this.cooldowns.containsKey(player);
	}


}
