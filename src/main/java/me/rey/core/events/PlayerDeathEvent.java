package me.rey.core.events;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.rey.core.Warriors;
import me.rey.core.events.customevents.DeathEvent;
import me.rey.core.players.PlayerHit;
import me.rey.core.players.PlayerHitCache;
import me.rey.core.players.User;
import me.rey.core.utils.Text;

public class PlayerDeathEvent implements Listener {

	PlayerHitCache cache = Warriors.getInstance().getHitCache();
	
	@EventHandler
	public void onPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent e) {
		
		final Player player = e.getEntity();
		// AUTO-RESPAWN
		Bukkit.getScheduler().scheduleSyncDelayedTask(Warriors.getInstance(), () -> player.spigot().respawn(), 1L);
		
		if(Warriors.deathMessagesEnabled) {
			
			int assists = cache.getAssists(player);
			ArrayList<PlayerHit> playerCache = cache.getPlayerCache(player);
			PlayerHit lastBlow = cache.getLastBlow(player);
			
			e.setDeathMessage(null);
			DeathEvent deathEvent = new DeathEvent(player, lastBlow, assists, player.getLastDamageCause());
			Bukkit.getServer().getPluginManager().callEvent(deathEvent);
			
			if(!deathEvent.isDeathMessageCanceled())
				Bukkit.broadcastMessage(Text.color(deathEvent.getDeathMessage().get()));
			
			// SENIDNG THEM THEIR DEATH SUMMARY
			int index = 1;
			for(PlayerHit hit : playerCache) {
				String cause = hit.hasCause() ? hit.getCause() : null;
								
				new User(player).sendMessage(String.format("&2#%s: &e%s &7[&e%s&7] &7[&a%s&7] &7[&a%s Seconds Prior&7]&r", index, hit.getDamager(),
						String.format("%.1f", hit.getDamage()), cause == null ? "None" : cause, hit.getLongAgo(System.currentTimeMillis())));
				
				index++;
			}
			
			cache.clearPlayerCache(player);
			
		}
		
	}
	

}
