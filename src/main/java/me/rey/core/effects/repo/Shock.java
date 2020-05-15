package me.rey.core.effects.repo;

import org.bukkit.EntityEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.rey.core.Warriors;
import me.rey.core.effects.Effect;
import me.rey.core.effects.EffectType;
import me.rey.core.effects.SoundEffect;

public class Shock extends Effect {

	public Shock() {
		super("Shock", EffectType.SHOCK);
	}

	@Override
	public void onApply(LivingEntity ent, double seconds) {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				if(!hasEffect(ent) || ent == null || (ent instanceof Player && !((Player) ent).isOnline())) {
					this.cancel();
					return;
				}
				
				ent.playEffect(EntityEffect.HURT);
			}
			
		}.runTaskTimer(Warriors.getInstance(), 0, 2);
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
