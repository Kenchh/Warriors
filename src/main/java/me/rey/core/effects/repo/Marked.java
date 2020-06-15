package me.rey.core.effects.repo;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.rey.core.Warriors;
import me.rey.core.effects.Effect;
import me.rey.core.effects.EffectType;
import me.rey.core.effects.SoundEffect;
import me.rey.core.events.customevents.combat.DamageEvent;

public class Marked extends Effect {
	
	private static Map<LivingEntity, Double> marked = new HashMap<>();
	private double extraDamage;

	public Marked(double extraDamage) {
		super("Mark", EffectType.MARK);
		
		this.extraDamage = extraDamage;
	}

	@Override
	public void onApply(LivingEntity ent, double seconds) {
		marked.put(ent, extraDamage);
		
		/*
		 * BLOCK EFFECTS
		 */
		Class<? extends Effect> clazz = this.getClass();
		BukkitTask runnable = new BukkitRunnable() {	
			
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				if(!Effect.hasEffect(clazz, ent)) this.cancel();
				Location loc = ent.getEyeLocation().clone();
				loc.setY(loc.getY()-0.4);
				ent.getWorld().playEffect(loc, org.bukkit.Effect.STEP_SOUND, Material.COAL_BLOCK.getId());
				ent.getWorld().playEffect(loc, org.bukkit.Effect.STEP_SOUND, Material.OBSIDIAN.getId());
			}
			
		}.runTaskTimer(Warriors.getInstance(), 0, 20);
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				runnable.cancel();
				marked.remove(ent);
				return;
			}
		}.runTaskLater(Warriors.getInstance(), (int) (seconds * 20));
	}
	
	@EventHandler
	public void onDamageEnt(DamageEvent e) {
		System.out.println(1);
		if(!marked.containsKey(e.getDamagee())) return;
		
		System.out.println(2);
		double extraDmg = marked.get(e.getDamagee());
		marked.remove(e.getDamagee());
		
		e.addMod(extraDmg);
		this.expireForcefully(e.getDamagee());
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
		return this.defaultExpireMessage;
	}

}
