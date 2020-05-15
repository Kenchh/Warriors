package me.rey.core.combat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
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
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.google.common.collect.ImmutableMap;

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
import net.minecraft.server.v1_8_R3.AttributeModifier;

public class DamageHandlerEvents implements Listener {

	private final long HIT_DELAY = 400;
	PlayerHitCache cache = Warriors.getInstance().getHitCache();
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onDamage(EntityDamageByEntityEvent e) {
		if(!(e.getEntity() instanceof LivingEntity)) return;
		
		/*
		 * HIT DELAY
		 */
		if(CombatManager.timeAgo((LivingEntity) e.getEntity()) <= HIT_DELAY) {
			e.setCancelled(true);
			return;
		}
	
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
		
		// RAW DAMAGE
		ItemStack hold = damager instanceof LivingEntity && hitType == HitType.MELEE ? ((LivingEntity) damager).getEquipment().getItemInHand() : null;
		double rawDamage = this.getDamage(hold, hitType);
		if(damager instanceof Player) e.setDamage(rawDamage);
		
		/*
		 * CALL DAMAGE EVENT
		 */
		if(damager instanceof Player) {
				
			Player playerDamager = (Player) damager;
			
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
		
		// CALCULATE EFECTS
		this.calcEffects(e);		
		
		// CALCULATING FINAL DAMAGE ON ARMOR
		this.calcArmor(e);
		
		if(!e.isCancelled()) {
			
			/*
			 * CUSTOM DAMAGE
			 */
			if(hitType == HitType.MELEE) {
				e.setCancelled(true);
				((LivingEntity) e.getEntity()).setHealth(Math.max(0, Math.min(((LivingEntity) e.getEntity()).getHealth() - e.getDamage(),
						((LivingEntity) e.getEntity()).getMaxHealth())));
				CombatManager.resetTime((LivingEntity) e.getEntity());
			}
			
			/*
			 * DISPLAY SOUNDS
			 */
			this.playEntitySound((LivingEntity) e.getEntity());
			
			
			/*
			 * KNOCKBACK
			 */
			double multiplier = 1;
			if(damageEvent != null)
				multiplier = damageEvent.getKnockbackMult();
			
			CustomKnockbackEvent kbEvent = new CustomKnockbackEvent(damagee, damager, e.getDamage(), multiplier);
			Bukkit.getServer().getPluginManager().callEvent(kbEvent);
			
			if(!kbEvent.isCancelled())
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
		this.calcArmor(e);
		this.calcEffects(e);
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
	
	private void calcArmor(EntityDamageEvent e) {
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
	
	private void calcEffects(EntityDamageEvent e) {
		
		Map<PotionEffectType, Double> damager = ImmutableMap.of(
				PotionEffectType.INCREASE_DAMAGE, 1.00, // STRENGTH
				PotionEffectType.WEAKNESS, -1.00 // WEAKNESS
				);
		
		Map<PotionEffectType, Double> damagee = ImmutableMap.of(
				PotionEffectType.DAMAGE_RESISTANCE, -1.00 // RESISTANCE
				);
		
		/*
		 * DAMAGER POTION EFFECTS
		 */
		if(e instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) e).getDamager() instanceof LivingEntity) {
			LivingEntity ent = (LivingEntity) ((EntityDamageByEntityEvent) e).getDamager();
			
			if(!ent.getActivePotionEffects().isEmpty()) {
				Map<PotionEffectType, Integer> types = new HashMap<>();
				ent.getActivePotionEffects().forEach((effect) -> types.put(effect.getType(), effect.getAmplifier()));
				
				damager.forEach((effect, dmg) -> {
					if(types.containsKey(effect))
						e.setDamage(e.getDamage() + (dmg * (types.get(effect) + 1)));
				});
			}
		}
		
		/*
		 * DAMAGEE POTION EFFECTS
		 */
		LivingEntity ent = (LivingEntity) e.getEntity();
		
		if(!ent.getActivePotionEffects().isEmpty()) {
			Map<PotionEffectType, Integer> types = new HashMap<>();
			ent.getActivePotionEffects().forEach((effect) -> types.put(effect.getType(), effect.getAmplifier()));
			
			damagee.forEach((effect, dmg) -> {
				if(types.containsKey(effect))
					e.setDamage(e.getDamage() + (dmg * (types.get(effect) + 1)));
			});
		}
	}
	
	private void kb(Entity entity, Entity hitter, double damage, double multiplier) {
		
		damage += 3; 
		if (damage < 2.0D) damage = 2.0D;
		damage = Math.log10(damage);
		
		Vector trajectory = entity.getLocation().toVector().subtract(hitter.getLocation().toVector()).multiply(multiplier);
		trajectory.multiply(0.05D * damage * 2D);
		trajectory.setY(Math.abs(trajectory.getY()));
		
		UtilVelocity.velocity(entity,
		          trajectory, 0.2D + trajectory.length() * 0.8D, false, 0.0D, Math.abs(0.2D * damage), 0.4D + 0.04D * damage, true);
	}
	
	private void playEntitySound(LivingEntity damagee) {
	    Sound sound;
	    float pitch = 0.8F + (float)(0.4000000059604645D * Math.random());
	    float volume = 1.5F + (float)(0.5D * Math.random());
	    
	    switch(damagee.getType()) {
	    case BAT:sound = Sound.BAT_HURT; break;
	    case BLAZE: sound = Sound.BLAZE_HIT; break;
	    case CAVE_SPIDER: sound = Sound.SPIDER_IDLE; break;
	    case CHICKEN: sound = Sound.CHICKEN_HURT; break;
	    case COW: sound = Sound.COW_HURT; break;
	    case CREEPER: sound = Sound.CREEPER_HISS; break;
	    case ENDER_DRAGON: sound = Sound.ENDERDRAGON_GROWL; break;
	    case ENDERMAN: sound = Sound.ENDERMAN_HIT; break;
	    case GHAST: sound = Sound.GHAST_SCREAM; break;
	    case GIANT: sound = Sound.ZOMBIE_HURT; break;
	    case IRON_GOLEM: sound = Sound.IRONGOLEM_HIT; break;
	    case MAGMA_CUBE: sound = Sound.MAGMACUBE_JUMP; break;
	    case MUSHROOM_COW: sound = Sound.COW_HURT; break;
	    case OCELOT: sound = Sound.CAT_MEOW; break;
	    case PIG: sound = Sound.PIG_IDLE; break;
	    case PIG_ZOMBIE: sound = Sound.ZOMBIE_HURT; break;
	    case SHEEP: sound = Sound.SHEEP_IDLE; break;
	    case SILVERFISH: sound = Sound.SILVERFISH_HIT; break;
	    case SKELETON: sound = Sound.SKELETON_HURT; break;
	    case SLIME: sound = Sound.SLIME_ATTACK; break;
	    case SNOWMAN: sound = Sound.STEP_SNOW; break;
	    case SPIDER: sound = Sound.SPIDER_IDLE; break;
	    case WITHER: sound = Sound.WITHER_HURT; break;
	    case WOLF: sound = Sound.WOLF_HURT; break;
	    case ZOMBIE: sound = Sound.ZOMBIE_HURT; break;
	    default:
	    	sound = Sound.HURT_FLESH;
	    	if(damagee instanceof Player && new User((Player) damagee).getWearingClass() != null){
				ClassType type = new User((Player) damagee).getWearingClass();
				sound = type.getSound().getSound();
				pitch = type.getSound().getPitch();
				volume = 1.0f;
			}
	    	break;
	    }
	    
	    damagee.getWorld().playSound(damagee.getLocation(), sound, volume, pitch);
	    damagee.playEffect(EntityEffect.HURT);
	}
	
	private double getDamage(ItemStack hold, HitType hitType) {
		final double baseDmg = 1D;
		
		if(hold != null && !hold.getType().equals(Material.AIR)) {
			
			for(ToolType toolType : ToolType.values()) {
				if(toolType.getHitType() != hitType) continue;
				if(!toolType.getType().equals(hold.getType())) continue;
				
				return toolType.getDamage();
			}
			
			net.minecraft.server.v1_8_R3.ItemStack item = CraftItemStack.asNMSCopy(hold);
			Iterator<AttributeModifier> attackDmg = item.B().get("generic.attackDamage").iterator();;
			return attackDmg.hasNext() ? attackDmg.next().d() : baseDmg;
		}
		
		return baseDmg;
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
