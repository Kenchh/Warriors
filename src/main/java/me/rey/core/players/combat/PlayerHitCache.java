package me.rey.core.players.combat;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import me.rey.core.Warriors;
import me.rey.core.utils.References;

public class PlayerHitCache implements Listener {
	
	private Warriors plugin;
	private HashMap<Player, ArrayList<PlayerHit>> playerHitCache;
	private HashMap<Player, CombatTimer> combatTimers;
	public final int secondsToDelete = References.COMBAT_TIMER;
	
	public PlayerHitCache(Warriors plugin) {
		this.plugin = plugin;
		this.playerHitCache = new HashMap<>();
		this.combatTimers = new HashMap<>();
	}
	
	public ArrayList<PlayerHit> getPlayerCache(Player player){
		if(!playerHitCache.containsKey(player))
			return new ArrayList<>();
		return playerHitCache.get(player);
	}
	
	public void addToPlayerCache(Player target, PlayerHit hit) {
		ArrayList<PlayerHit> currentHits = getPlayerCache(target);

		// Adding current damage if the damager is the same in other hits
		boolean isAlreadyIn = false;
		for(int i = 0; i < currentHits.size(); i++) {
			PlayerHit query = currentHits.get(i);
			
			if(query.getDamager().equals(hit.getDamager())) {
				query.addDamage(hit.getDamage());
				query.setCause(hit.getCause());
				query.setTimeIssued(hit.getTimeIssued());
				isAlreadyIn = true;
			}
		}
		
		// saving
		if(!isAlreadyIn) {
			currentHits.add(hit);
			updateCache(target, currentHits);
		}
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				
				for(int i = 0; i < getPlayerCache(target).size(); i++) {
					PlayerHit query = getPlayerCache(target).get(i);
					
					if(query.getDamager().equals(hit.getDamager()) && query.getDamage() == hit.getDamage()) {
						ArrayList<PlayerHit> clone = getPlayerCache(target);
						clone.remove(i);
						updateCache(target, clone);
						this.cancel();
						return;
					}
				}
				
				this.cancel();
				
			}
			
		}.runTaskLater(plugin, secondsToDelete * 20);
		
		
	}
	
	public void startCombatTimer(Player target) {
		if(hasCombatTimer(target) && this.getCombatTimer(target).getRemaining(System.currentTimeMillis()) > 0)
			this.getCombatTimer(target).cancel();
		this.combatTimers.put(target, new CombatTimer(target));
		CombatTimer start = this.getCombatTimer(target).init();
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				if(getCombatTimer(target) != null && getCombatTimer(target).getTimeIssued() == start.getTimeIssued())
					combatTimers.remove(target);
				this.cancel();
				
			}
			
		}.runTaskLater(plugin, secondsToDelete * 20);
		
	}
	
	public CombatTimer getCombatTimer(Player target) {
		return hasCombatTimer(target) ? this.combatTimers.get(target) : null;
	}
	
	public boolean hasCombatTimer(Player target) {
		return this.combatTimers.containsKey(target);
	}
	
	private void updateCache(Player target, ArrayList<PlayerHit> cache) {
		this.playerHitCache.put(target, cache);
	}
	
	public void clearPlayerCache(Player target) {
		if(playerHitCache.containsKey(target))
			playerHitCache.remove(target);
	}
	
	public PlayerHit getLastBlow(Player target) {
		for (int i = getPlayerCache(target).size(); i-- > 0; ) {
			PlayerHit query = getPlayerCache(target).get(i);
			if(!query.isCausedByPlayer()) continue;
			return query;
		}
		return null;
	}

	public int getAssists(Player player) {
		int assists = 0;
		for(PlayerHit hit : getPlayerCache(player)) {
			if(!hit.isCausedByPlayer()) continue;
			
			assists++;
		}
		
		return assists > 0 ? assists - 1 : 0;
	}
	
}
