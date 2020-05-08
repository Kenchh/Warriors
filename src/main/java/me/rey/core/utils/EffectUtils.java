package me.rey.core.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import me.rey.core.Warriors;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.events.customevents.UpdateEvent;
import me.rey.core.players.User;

public class EffectUtils implements Listener {
	
	public static final List<AbilityType> silencedAbilities = Arrays.asList(AbilityType.BOW, AbilityType.SWORD, AbilityType.SPADE, AbilityType.AXE, AbilityType.PASSIVE_A);
	
	private static Set<UUID> silenced = new HashSet<>();
	private static Set<UUID> shocked = new HashSet<>();

	public static Set<UUID> getSilenced(){
		return silenced;
	}
	
	public static Set<UUID> getShocked(){
		return shocked;
	}
	
	public static void silence(UUID player, double seconds) {
		silenced.add(player);
		
		Player p = Bukkit.getServer().getPlayer(player);
		if(p == null || !p.isOnline()) return;
	    p.playSound(p.getLocation(), Sound.BAT_HURT, 0.8F, 0.8F);
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				silenced.remove(player);
				sendMessage(player, Text.format("Effect", "You are no longer silenced."));
			}
			
		}.runTaskLater(Warriors.getInstance(), (int) Math.round(seconds * 20));
	}
	
	public static void unsilence(UUID player) {
		silenced.remove(player);
	}
	
	public static void shock(UUID player, double seconds) {
		shocked.add(player);

		new BukkitRunnable() {
			@Override
			public void run() {
				if(shocked.contains(player)) {
					Player p = Bukkit.getServer().getPlayer(player);
					p.playEffect(EntityEffect.HURT);
				} else {
					this.cancel();
				}
			}
		}.runTaskTimer(Warriors.getInstance(), 0L, 1L);

		new BukkitRunnable() {
			
			@Override
			public void run() {
				shocked.remove(player);
			}
			
		}.runTaskLater(Warriors.getInstance(), (int) Math.round(seconds * 20));
	}
	
	public static void unshock(UUID player) {
		shocked.remove(player);
	}
	
	public static boolean isSilenced(UUID player) {
		return silenced.contains(player);
	}
	
	public static boolean isShocked(UUID player) {
		return shocked.contains(player);
	}
	
	
	/*
	 * SHOCK EFFECT
	 */
	@EventHandler
	public void onUpdate(UpdateEvent e) {
		//TODO:  SHOCK EFFECT
	}
	
	private static void sendMessage(UUID player, String message) {
		Player p = Bukkit.getServer().getPlayer(player);
		if(p == null || !p.isOnline()) return;
		new User(p).sendMessage(message);
	}
}
