package me.rey.core.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import me.rey.core.events.customevents.AbilityUseEvent;
import me.rey.core.events.customevents.CustomPlayerInteractEvent;
import me.rey.core.events.customevents.damage.DamageEvent;

public class PlayerInteractChecker implements Listener {
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		this.callEvent(e.getPlayer());
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		this.callEvent(e.getPlayer());
	}
	
	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		this.callEvent(e.getPlayer());
	}
	
	@EventHandler
	public void onAbility(AbilityUseEvent e) {
		this.callEvent(e.getPlayer());
	}
	
	@EventHandler
	public void onInteract(EntityDamageEvent e) {
		if(e.getEntity() instanceof Player)
			this.callEvent((Player) e.getEntity());
	}
	
	@EventHandler
	public void onPickup(PlayerPickupItemEvent e) {
		this.callEvent(e.getPlayer());
	}
	
	@EventHandler
	public void onPortal(PlayerPortalEvent e) {
		this.callEvent(e.getPlayer());
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		this.callEvent(e.getPlayer());
	}
	
	@EventHandler
	public void onPlayerHit(DamageEvent e) {
		this.callEvent(e.getDamager());
	}
	
	private void callEvent(Player p) {
		CustomPlayerInteractEvent event = new CustomPlayerInteractEvent(p);
		Bukkit.getServer().getPluginManager().callEvent(event);
	}

}
