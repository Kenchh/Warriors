package me.rey.core.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import me.rey.core.events.customevents.AbilityUseEvent;
import me.rey.core.events.customevents.CustomPlayerInteractEvent;
import me.rey.core.events.customevents.damage.DamageEvent;

public class PlayerInteractChecker implements Listener {
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onInteract(PlayerInteractEvent e) {
		this.callEvent(e.getPlayer());
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onBreak(BlockBreakEvent e) {
		this.callEvent(e.getPlayer());
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPlace(BlockPlaceEvent e) {
		this.callEvent(e.getPlayer());
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onAbility(AbilityUseEvent e) {
		if(e.isCancelled()) return;
		this.callEvent(e.getPlayer());
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onInteract(EntityDamageEvent e) {
		if(e.isCancelled()) return;
		if(e.getEntity() instanceof Player)
			this.callEvent((Player) e.getEntity());
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPickup(PlayerPickupItemEvent e) {
		if(e.isCancelled()) return;
		this.callEvent(e.getPlayer());
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPortal(PlayerPortalEvent e) {
		if(e.isCancelled()) return;
		this.callEvent(e.getPlayer());
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onDrop(PlayerDropItemEvent e) {
		if(e.isCancelled()) return;
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
	
	
	/*
	 * EQUIP ARMOR SOUND
	 */
	@EventHandler
	public void onPlayerInventory(InventoryClickEvent e) {
		if(e.getCurrentItem() == null || e.getCurrentItem().getType().equals(Material.AIR)) return;
		if(e.getSlotType() != SlotType.ARMOR) return;
		
		((Player) e.getWhoClicked()).playSound(((Player) e.getWhoClicked()).getLocation(), Sound.HORSE_ARMOR, 1.0F, 1.0F);
	}

}
