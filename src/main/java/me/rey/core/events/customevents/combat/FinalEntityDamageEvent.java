package me.rey.core.events.customevents.combat;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import me.rey.core.pvp.ToolType.HitType;

public class FinalEntityDamageEvent extends CustomDamageEvent {

	public FinalEntityDamageEvent(HitType hitType, LivingEntity damager, LivingEntity damagee, double damage, ItemStack item) {
		super(hitType, damager, damagee, damage, item);
	}
	
}
