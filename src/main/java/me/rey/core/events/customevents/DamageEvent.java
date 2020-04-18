package me.rey.core.events.customevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import me.rey.core.players.PlayerHit;
import me.rey.core.utils.Utils;

public class DamageEvent extends Event implements Cancellable {
	
	private final Player damager;
	private final LivingEntity damagee;
	private double damage;
	private boolean isCancelled;
	private EntityDamageByEntityEvent event;
	private ItemStack item;
	private PlayerHit hit;
	
	public DamageEvent(EntityDamageByEntityEvent event, Player damager, LivingEntity damagee, double damage, ItemStack item) {
		this.event = event;
		this.damager = damager;
		this.damagee = damagee;
		this.damage = damage;
		this.isCancelled = false;
		this.item = item;
		this.hit = null;
	}
	
	private static final HandlerList HANDLERS = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
	
	public Player getDamager() {
		return damager;
	}
	
	public LivingEntity getDamagee() {
		return damagee;
	}
	
	public double getOriginalDamage() {
		return damage;
	}
	
	public double getDamage() {
		return event.getDamage();
	}
	
	public void addMod(double damage) {
		event.setDamage(event.getDamage() + damage);
	}

	@Override
	public boolean isCancelled() {
		return this.isCancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.isCancelled = cancelled;
		this.event.setCancelled(cancelled);
	}
	
	public PlayerHit getHit() {
		if(hit != null) return hit;
		if(!(this.getDamagee() instanceof Player)) return null;
		Utils.updateItem(item);
		ItemStack hold = item.clone();
	
		return new PlayerHit((Player) this.getDamagee(), (LivingEntity) this.getDamager(), this.getDamage(), hold);
	}
	
	public void setHit(PlayerHit hit) {
		this.hit = hit;
	}

}
