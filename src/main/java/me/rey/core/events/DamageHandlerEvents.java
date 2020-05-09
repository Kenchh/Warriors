package me.rey.core.events;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
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
import me.rey.core.events.customevents.CustomKnockbackEvent;
import me.rey.core.events.customevents.damage.CustomDamageEvent;
import me.rey.core.events.customevents.damage.DamageEvent;
import me.rey.core.events.customevents.damage.DamagedByEntityEvent;
import me.rey.core.players.PlayerHit;
import me.rey.core.players.PlayerHitCache;
import me.rey.core.players.User;
import me.rey.core.pvp.ToolType;
import me.rey.core.pvp.ToolType.HitType;
import me.rey.core.utils.Text;
import me.rey.core.utils.UtilVelocity;

public class DamageHandlerEvents implements Listener {

	PlayerHitCache cache = Warriors.getInstance().getHitCache();
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onDamage(EntityDamageByEntityEvent e) {
		if(!(e.getEntity() instanceof LivingEntity)) return;
		CustomDamageEvent damageEvent = null;
		
		/*
		 * SETTING ENTITY TO SHOOTER
		 */
		HitType hitType = HitType.MELEE;
		Entity damager = e.getDamager(), damagee = e.getEntity();;
		if(damager instanceof Projectile && ((Projectile) damager).getShooter() != null && ((Projectile) damager).getShooter() instanceof LivingEntity) {
			damager = (LivingEntity) ((Projectile) damager).getShooter();
			hitType = e.getDamager() instanceof Arrow ? HitType.ARCHERY : HitType.OTHER;
		}
		
		/*
		 * ADDING TO HIT CACHE
		 */
		if(!(damager instanceof Player) && (e.getEntity() instanceof Player) && (damager instanceof LivingEntity)) {
			String name = Text.format(((LivingEntity) damager).getName());
			cache.addToPlayerCache((Player) e.getEntity(), new PlayerHit((Player) e.getEntity(), name, e.getDamage(), null));
		}
		
		/*
		 * TOOL DAMAGE && CALL DAMAGE EVENT
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
			
			damageEvent = new DamageEvent(e, hitType, playerDamager, (LivingEntity) damagee, e.getDamage(), hold);
			Bukkit.getServer().getPluginManager().callEvent(damageEvent);
			
			if(damageEvent.isCancelled())
				e.setCancelled(true);
			
			playerDamager.setLevel((int) Math.round(e.getDamage()));
			
			// ADDING TO THEIR HIT CACHE IF THEY'RE A PLAYER
			if(damagee instanceof Player && !damageEvent.isCancelled())
				cache.addToPlayerCache((Player) e.getEntity(), damageEvent.getHit());
			
		}
		
		/*
		 * ENTITY DAMAGED PLAYER
		 */
		
		if(damagee instanceof Player && damageEvent == null  && damager instanceof LivingEntity) {
			damageEvent = new DamagedByEntityEvent(e, hitType, (LivingEntity) damager, (Player) damagee, e.getDamage(), ((LivingEntity) damagee).getEquipment().getItemInHand());
			Bukkit.getServer().getPluginManager().callEvent(damageEvent);
			
			if(damageEvent.isCancelled())
				e.setCancelled(true);
		}
		
		// ARMOR VALUES
		this.calcDamage(e);
		
		if(!e.isCancelled()) {
			
			/*
			 * CUSTOM DAMAGE
			 */
			if(hitType == HitType.MELEE) {
				e.setCancelled(true);
				((LivingEntity) e.getEntity()).setHealth(Math.max(0, Math.min(((LivingEntity) e.getEntity()).getHealth() - e.getDamage(),
						((LivingEntity) e.getEntity()).getMaxHealth())));
			}
			
			/*
			 * DISPLAY SOUNDS
			 */
			if(((LivingEntity) e.getEntity() instanceof Player && new User((Player) e.getEntity()).getWearingClass() != null)){
				Player p = (Player) e.getEntity();
				ClassType type = new User(p).getWearingClass();
				p.getWorld().playSound(p.getLocation(), type.getSound().getSound(), 1.0f, type.getSound().getPitch());
			} else {
				((LivingEntity) e.getEntity()).getWorld().playSound(((LivingEntity) e.getEntity()).getLocation(), Sound.HURT_FLESH, 1.0f, 1.0f);
			}
			
			((LivingEntity) e.getEntity()).playEffect(EntityEffect.HURT);
			
			/*
			 * KNOCKBACK
			 */
			double multiplier = 1;
			if(damageEvent != null)
				multiplier = damageEvent.getKnockbackMult();
			
			CustomKnockbackEvent kbEvent = new CustomKnockbackEvent(damagee, damager, e.getDamage(), multiplier);
			Bukkit.getServer().getPluginManager().callEvent(kbEvent);
			
			if(kbEvent.isCancelled())
				return;
			
			kb(kbEvent.getDamagee(), kbEvent.getDamager(), kbEvent.getDamage(), kbEvent.getMult());
		}
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
		if(e.getEntity() instanceof Player) {
			ClassType wearing = new User((Player) e.getEntity()).getWearingClass();
			
			if(e.isApplicable(DamageModifier.ARMOR))
				e.setDamage(DamageModifier.ARMOR, 0);
			
			if(wearing != null) {
				Player entity = (Player) e.getEntity();
				double damage = (entity.getMaxHealth() / wearing.getHealth()) * e.getDamage();
				e.setDamage(damage);
			}
		}
	}
	
	private void kb(Entity entity, Entity hitter, double damage, double multiplier) {
		
		damage += 3; 
		if (damage < 2.0D) damage = 2.0D;
		damage = Math.log10(damage);
		
		Vector trajectory = entity.getLocation().toVector().subtract(hitter.getLocation().toVector()).multiply(multiplier);
		trajectory.multiply(0.05D / (13 / damage));
		trajectory.setY(Math.abs(trajectory.getY()));
		
		UtilVelocity.velocity(entity,
		          trajectory, 0.2D + trajectory.length() * 0.8D, false, 0.0D, Math.abs(0.2D * damage), 0.4D + 0.04D * damage, true);
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
