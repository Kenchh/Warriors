package me.rey.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.rey.core.combat.CombatManager;
import me.rey.core.events.customevents.damage.CustomDamageEvent;
import me.rey.core.events.customevents.damage.DamageEvent;
import me.rey.core.events.customevents.damage.DamagedByEntityEvent;
import me.rey.core.players.PlayerHit;
import me.rey.core.pvp.ToolType.HitType;

public class UtilEnt {

	public static boolean isGrounded(Entity ent) {
	    if (ent instanceof CraftEntity) {
	      return ((CraftEntity)ent).getHandle().onGround;
	    }
	    return ent.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isSolid();
	}
	
	public static void damage(double damage, String cause, LivingEntity damagee, LivingEntity damager) {
		
		CustomDamageEvent event = null;
		if(damager instanceof Player) {
			event = new DamageEvent(HitType.OTHER, (Player) damager, damagee, damage, null);
			
			if(damagee instanceof Player) {
				event.setHit(new PlayerHit((Player) damagee, ((Player) damager).getName(), damage, cause));
				((DamageEvent) event).storeCache();
			}
			
		} else if (!(damager instanceof Player) && damagee instanceof Player) {
			event = new DamagedByEntityEvent(HitType.OTHER, damager, (Player) damagee, damage, null);
			((DamagedByEntityEvent) event).storeCache();
		}
		
		boolean allow = true;
		if(event != null) {
			Bukkit.getServer().getPluginManager().callEvent(event);
			if(event.isCancelled()) allow = false;
		}
		
		if(allow) {
			damagee.setHealth(Math.max(0, Math.min(damagee.getHealth() - damage, damagee.getMaxHealth())));
			CombatManager.resetTime(damagee);
		}
	}
	
}
