package me.rey.core.effects.repo;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.rey.core.Warriors;
import me.rey.core.effects.Effect;
import me.rey.core.effects.EffectType;
import me.rey.core.effects.SoundEffect;
import me.rey.core.utils.UtilEnt;

public class Bleed extends Effect {
	
	private String cause;
	private LivingEntity damager;

	public Bleed(String cause, LivingEntity damager) {
		super("Bleed", EffectType.BLEED);
		
		this.damager = damager;
		this.cause = cause;
	}
	
	@EventHandler
	public void onHealthRegain(EntityRegainHealthEvent e) {
		if(e.getEntity() instanceof LivingEntity && this.hasEffect((LivingEntity) e.getEntity()))
				if((e.getRegainReason() == RegainReason.SATIATED || e.getRegainReason() == RegainReason.REGEN))
					e.setCancelled(true);
	}

	@Override
	public void onApply(LivingEntity ent, double seconds) {
		BukkitTask runnable = new BukkitRunnable() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				Location loc = ent.getLocation();
				loc.setY(loc.getY()-0.1);
				ent.getWorld().playEffect(loc, org.bukkit.Effect.STEP_SOUND, Material.REDSTONE_BLOCK.getId());
				ent.getWorld().playEffect(loc, org.bukkit.Effect.STEP_SOUND, Material.REDSTONE_WIRE.getId(), 14);
				UtilEnt.damage(2, cause, ent, damager);
			}
			
		}.runTaskTimer(Warriors.getInstance(), 0, 20);
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				runnable.cancel();
				return;
			}
		}.runTaskLater(Warriors.getInstance(), (int) (seconds * 20));
	}

	@Override
	public SoundEffect applySound() {
		return null;
	}

	@Override
	public SoundEffect expireSound() {
		return null;
	}

	@Override
	public String applyMessage() {
		return null;
	}

	@Override
	public String expireMessage() {
		return null;
	}

}
