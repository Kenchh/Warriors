package me.rey.core.players;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.rey.core.utils.Text;

public class PlayerHit {

	// NonNull
	private final Player damagee;
	private final String damager;
	private long timeIssued;
	private double damage;
	
	// Possibly Null
	private String cause;
	private LivingEntity damagerEntity;
	
	public PlayerHit(Player damagee, String damager, double damage, String cause) {
		this.damagee = damagee;
		this.damager = damager;
		this.damage = damage;
		this.cause = cause;
		
		this.timeIssued = System.currentTimeMillis();
	}
	
	public PlayerHit(Player damagee, LivingEntity damagerEntity, double damage, ItemStack item){
		this.damagee = damagee;
		this.damager = damagerEntity.getName();
		this.damage = damage;
		this.damagerEntity = damagerEntity;
		
		this.cause = formatItemName(item);
		this.timeIssued = System.currentTimeMillis();
	}
	
	public Player getPlayer() {
		return damagee;
	}

	public String getDamager() {
		return damager;
	}

	public double getDamage() {
		return damage;
	}
	
	public void addDamage(double toAdd) {
		this.damage += toAdd;
	}
	
	public boolean hasCause() {
		return cause != null;
	}

	public String getCause() {
		return cause;
	}
	
	public void setCause(String cause) {
		this.cause = cause;
	}
	
	public void setCause(ItemStack item) {
		this.cause = formatItemName(item);
	}
	
	public boolean isCausedByPlayer() {
		return (damagerEntity != null) && (damagerEntity instanceof Player);
	}
	
	public LivingEntity getEntityCause() {
		return damagerEntity;
	}
	
	public double getLongAgo(long currentTime) {
		return (currentTime-timeIssued)/1000;
	}
	
	public void setTimeIssued(long time) {
		this.timeIssued = time;
	}
	
	public long getTimeIssued() {
		return timeIssued;
	}
	
	private String formatItemName(ItemStack item) {
		boolean hasDisplayName = item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName(); 
		String itemName = null;
		if(hasDisplayName) {
			itemName = item.getItemMeta().getDisplayName();
		} else if (item != null && item.getType() == Material.AIR) {
			itemName = null;
		} else if (!hasDisplayName && item != null) {
			itemName = Text.format(item.getType().name());
		}
		
		return itemName == null ? "Fists" : hasDisplayName ? itemName : Text.format(itemName);
	}
	
}
