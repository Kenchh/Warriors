package me.rey.core.players.combat;

import java.util.ArrayList;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import me.rey.core.Warriors;
import me.rey.core.utils.Text;

public class DeathMessage {

	PlayerHitCache cache = Warriors.getInstance().getHitCache();
	
	private String dead, killerName, heldItemName;
	private int assists;
	
	public DeathMessage(Player dead, PlayerHit lastBlow, int assists, EntityDamageEvent event) {
		// , String killerName, int assists, String heldItemName
		this.dead = dead.getName();
		this.assists = assists;
		
		this.killerName = lastBlow == null ? null : lastBlow.getDamager();
		this.heldItemName = lastBlow == null ? null : lastBlow.getCause();
		
		ArrayList<PlayerHit> playerCache = cache.getPlayerCache(dead);
		EntityDamageEvent dmg = event;
		String lastCause = lastBlow != null && lastBlow.hasCause() ? lastBlow.getCause() : null;
		this.heldItemName = null;
		if(lastBlow != null) {
			
			LivingEntity killer = lastBlow.getEntityCause();
			killerName = Text.format(killer.getName());
			
			for(PlayerHit hit : playerCache) {
				if(hit.isCausedByPlayer() && hit.getEntityCause() == (LivingEntity) killer)
					killerName = Text.format(((LivingEntity) killer).getName());
			}
					
			if (killer instanceof Player)
				killerName = ((Player) killer).getName();
			
			this.heldItemName = lastCause;
			
		} else {
			killerName = !playerCache.isEmpty() ? playerCache.get(playerCache.size()-1).getDamager() :
				dmg.getCause() == DamageCause.SUICIDE ? null : Text.format(dmg.getCause().name());
		}
		
	}
	
	public String get() {		
		return Text.format("Death", killerName == null  || killerName.trim() == "" ? String.format("&e%s &7has died.", dead) : 
			String.format("&e%s &7was killed by &e%s&7%s", dead, killerName + (assists <= 0 ? "" : " + " + assists),
			heldItemName == null ? "&7." : " using &f" + heldItemName + "&7."));
	}
	
	public String getPlayerName() {
		return dead;
	}

	public void setPlayerName(String dead) {
		this.dead = dead;
	}

	public String getKillerName() {
		return killerName;
	}

	public void setKillerName(String killerName) {
		this.killerName = killerName;
	}

	public String getHeldItemName() {
		return heldItemName;
	}

	public void setHeldItemName(String heldItemName) {
		this.heldItemName = heldItemName;
	}

}
