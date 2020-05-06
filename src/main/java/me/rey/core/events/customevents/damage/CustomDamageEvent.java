package me.rey.core.events.customevents.damage;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import me.rey.core.players.PlayerHit;
import me.rey.core.pvp.ToolType.HitType;
import me.rey.core.utils.Utils;

public abstract class CustomDamageEvent extends Event implements Cancellable {

	protected final LivingEntity damager;
	protected final LivingEntity damagee;
	protected double damage, knockbackMult;
	protected boolean isCancelled;
	protected EntityDamageByEntityEvent event;
	protected ItemStack item;
	protected PlayerHit hit;
	protected HitType hitType;
	
	public CustomDamageEvent(EntityDamageByEntityEvent event, HitType hitType, LivingEntity damager, LivingEntity damagee, double damage, ItemStack item) {
		this.event = event;
		this.hitType = hitType;
		this.damager = damager;
		this.damagee = damagee;
		this.damage = damage;
		this.isCancelled = false;
		this.item = item;
		this.knockbackMult = 1;
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
	
	public HitType getHitType() {
		return hitType;
	}
	
	public LivingEntity getDamager() {
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
		event.setDamage(Math.max(0, event.getDamage() + damage));
	}
	
	public double getKnockbackMult() {
		return knockbackMult;
	}
	
	public void setKnockbackMult(double mult) {
		this.knockbackMult = mult;
	}
	
	@Override
	public boolean isCancelled() {
		return isCancelled;
	}
	
	@Override
	public void setCancelled(boolean cancel) {
		this.isCancelled = cancel;
	}
	
	public PlayerHit getHit() {
		if(hit != null) return hit;
		if(!(this.getDamagee() instanceof Player)) return null;
		Utils.updateItem(this.item);
		ItemStack hold = this.item.clone();
	
		PlayerHit toReturn;
		if(hitType != HitType.ARCHERY)
			toReturn = new PlayerHit((Player) this.getDamagee(), (LivingEntity) this.getDamager(), this.getDamage(), hold);
		else
			toReturn = new PlayerHit((Player) this.getDamagee(), ((LivingEntity) this.getDamager()).getName(), this.getDamage(), HitType.ARCHERY.getName());
		return toReturn;
	}
	
	public void setHit(PlayerHit hit) {
		this.hit = hit;
	}
	
}
