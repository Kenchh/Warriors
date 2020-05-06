package me.rey.core.events.customevents.damage;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import me.rey.core.pvp.ToolType.HitType;

public class DamageEvent extends CustomDamageEvent {
	
	public DamageEvent(EntityDamageByEntityEvent event, HitType hitType, Player damager, LivingEntity damagee, double damage, ItemStack item) {
		super(event, hitType, damager, damagee, damage, item);
	}
	
	@Override
	public Player getDamager() {
		return (Player) this.damager;
	}
	

}
