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
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.events.customevents.AbilityUseEvent;
import me.rey.core.events.customevents.DamageEvent;
import me.rey.core.players.PlayerHit;
import me.rey.core.players.PlayerHitCache;
import me.rey.core.players.User;
import me.rey.core.pvp.ToolType;
import me.rey.core.pvp.ToolType.HitType;
import me.rey.core.utils.Text;

public class DamageHandlerEvents implements Listener {

	PlayerHitCache cache = Warriors.getInstance().getHitCache();
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onDamage(EntityDamageByEntityEvent e) {
		if(!(e.getEntity() instanceof LivingEntity)) return;
		
		/*
		 * SETTING ENTITY TO SHOOTER
		 */
		HitType hitType = HitType.MELEE;
		Entity damager = e.getDamager();
		if(damager instanceof Projectile && ((Projectile) damager).getShooter() != null && ((Projectile) damager).getShooter() instanceof LivingEntity) {
			damager = (LivingEntity) ((Projectile) damager).getShooter();
			hitType = HitType.RANGED;
		}
		
		/*
		 * ADDING TO HIT CACHE
		 */
		if(!(damager instanceof Player) && (e.getEntity() instanceof Player) && (damager instanceof LivingEntity)) {
			String name = Text.format(((LivingEntity) damager).getName());
			cache.addToPlayerCache((Player) e.getEntity(), new PlayerHit((Player) e.getEntity(), name, e.getDamage(), null));
		}
		
		/*
		 * TOOL DAMAGE
		 */
		if(damager instanceof Player) {
				
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
	
			
			/*
			 * SCALING DAMAGE FOR KITS
			 */
			
			// ADDING TO THEIR HIT CACHE IF THEY'RE A PLAYER
			if(e.getEntity() instanceof Player && !damageEvent.isCancelled())
				cache.addToPlayerCache((Player) e.getEntity(), damageEvent.getHit());
			
		}
		
		// ARMOR VALUES
		this.calcDamage(e);
		
		/*
		 * KNOCKBACK
		 */
		if(e.isCancelled()) return;
		kb(e.getEntity(), e.getDamager(), e.getDamage());
	}
	
	@EventHandler
	public void onAnyDamage(EntityDamageEvent e) {
		/*
		 * CACHE
		 */
		if(e instanceof EntityDamageByEntityEvent) return;
		if(!(e.getEntity() instanceof Player)) return;
		if(e.isCancelled()) return;
		
		Player target = (Player) e.getEntity();
		String damager = Text.format(e.getCause().name());
		
		cache.addToPlayerCache(target, new PlayerHit(target, damager, e.getDamage(), null));
		
		// armor values
		this.calcDamage(e);
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onAbilityUse(AbilityUseEvent e) {
		if(!e.isCancelled())
			cache.startCombatTimer(e.getPlayer());
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onDamage(DamageEvent e) {
		if(e.isCancelled()) return;
		
		if(!(e.getDamagee() instanceof Player)) return;
		cache.startCombatTimer(e.getDamager());
		cache.startCombatTimer((Player) e.getDamagee());
	}
	
	private void calcDamage(EntityDamageEvent e) {
		/*
		 * ARMOR VALUES
		 */
		if(e.isCancelled()) return; 
		if(e.getEntity() instanceof Player) {
			ClassType wearing = new User((Player) e.getEntity()).getWearingClass();
			e.setDamage(DamageModifier.ARMOR, 0);
			if(wearing != null) {
				Player entity = (Player) e.getEntity();
				double damage = (entity.getMaxHealth() / wearing.getHealth()) * e.getDamage();
				e.setDamage(damage);
			}
		}
	}
	
	private void kb(Entity entity, Entity hitter, double damage) {
		
		if(entity instanceof Player) {
			long difference = cache.getLastBlow((Player) entity).getTimeIssued() - System.currentTimeMillis();
			if(difference < 400) return;
		}
		
		damage += 3;
		if (damage < 2.0D) damage = 2.0D;
		damage = Math.log10(damage);
		
		Vector trajectory = entity.getLocation().toVector().subtract(hitter.getLocation().toVector());
		trajectory.multiply(0.05D * damage);
		trajectory.setY(Math.abs(trajectory.getY()) * 0.8D);
       
		entity.setVelocity(trajectory.multiply(0.3D + trajectory.length() * 0.05D));
	   
	}
	
//    @EventHandler
//    public void onKB(CustomKnockbackEvent e) {
//        double knockback = e.getDamage();
//        if (e.getDamager() instanceof Player) {
//            Player player = (Player) e.getDamager();
//            if (player.isSprinting()) {
//                if (e.d.getCause() == DamageCause.ENTITY_ATTACK) {
//                    knockback += 3;
//                }
//            }
//        }
//        if (knockback < 2.0D) knockback = 2.0D;
//        knockback = Math.log10(knockback);
//
//        e.setDamage(knockback);
//    }
//
//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onFinalKB(CustomKnockbackEvent e) {
//        Vector trajectory = UtilVelocity.getTrajectory2d(e.getDamager(), e.getDamagee());
//        trajectory.multiply(0.8D * e.getDamage());
//        trajectory.setY(Math.abs(trajectory.getY()));
//
//        UtilVelocity.velocity(e.getDamagee(),
//                trajectory, 0.3D + trajectory.length() * 0.8D, false, 0.0D, Math.abs(0.2D * e.getDamage()), 0.4D + 0.04D * e.getDamage(), true);
//    }


}
