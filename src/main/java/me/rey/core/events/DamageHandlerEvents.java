package me.rey.core.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import me.rey.core.Warriors;
import me.rey.core.events.customevents.AbilityUseEvent;
import me.rey.core.events.customevents.DamageEvent;
import me.rey.core.players.PlayerHit;
import me.rey.core.players.PlayerHitCache;
import me.rey.core.pvp.ToolType;
import me.rey.core.pvp.ToolType.HitType;
import me.rey.core.utils.Text;

public class DamageHandlerEvents implements Listener {

	PlayerHitCache cache = Warriors.getInstance().getHitCache();
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onDamage(EntityDamageByEntityEvent e) {
		if(!(e.getEntity() instanceof LivingEntity)) return;
		
		HitType hitType = HitType.MELEE;
		Entity damager = e.getDamager();
		if(damager instanceof Projectile && ((Projectile) damager).getShooter() != null && ((Projectile) damager).getShooter() instanceof LivingEntity) {
			damager = (LivingEntity) ((Projectile) damager).getShooter();
			hitType = HitType.RANGED;
		}
		
		if(!(damager instanceof Player) && (e.getEntity() instanceof Player) && (damager instanceof LivingEntity)) {
			String name = Text.format(((LivingEntity) damager).getName());
			cache.addToPlayerCache((Player) e.getEntity(), new PlayerHit((Player) e.getEntity(), name, e.getDamage(), null));
			return;
		}
		
		if(!(damager instanceof Player)) return;
			
		Player playerDamager = (Player) damager;
		ItemStack hold = playerDamager.getItemInHand();
		if(hold != null && !hold.getType().equals(Material.AIR)) {
			
			for(ToolType toolType : ToolType.values()) {
				if(toolType.getHitType() != hitType) continue;
				if(!toolType.getType().equals(hold.getType())) continue;
				
				e.setDamage(toolType.getDamage());
			}
		}
		
		DamageEvent damageEvent = new DamageEvent(e, playerDamager, (LivingEntity) e.getEntity(), e.getDamage(), hold);
		Bukkit.getServer().getPluginManager().callEvent(damageEvent);
		
		if(damageEvent.isCancelled())
			e.setCancelled(true);
		
		playerDamager.setLevel((int) Math.round(e.getDamage()));
		playerDamager.setExp(0);

		// ADDING TO THEIR HIT CACHE IF THEY'RE A PLAYER
		if(!(e.getEntity() instanceof Player) || damageEvent.isCancelled()) return;
		cache.addToPlayerCache((Player) e.getEntity(), damageEvent.getHit());
	}
	
	@EventHandler
	public void onAnyDamage(EntityDamageEvent e) {
		if(e instanceof EntityDamageByEntityEvent) return;
		if(!(e.getEntity() instanceof Player)) return;
		
		Player target = (Player) e.getEntity();
		String damager = Text.format(e.getCause().name());
		
		cache.addToPlayerCache(target, new PlayerHit(target, damager, e.getDamage(), null));
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onAbilityUse(AbilityUseEvent e) {
		if(!e.isCancelled())
			cache.startCombatTimer(e.getPlayer());
	}
	
	@EventHandler
	public void onDamage(DamageEvent e) {
		if(e.isCancelled()) return;
		
		if(!(e.getDamagee() instanceof Player)) return;
		cache.startCombatTimer(e.getDamager());
		cache.startCombatTimer((Player) e.getDamagee());
	}

}
