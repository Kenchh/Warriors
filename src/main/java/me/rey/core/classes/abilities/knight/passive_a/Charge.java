package me.rey.core.classes.abilities.knight.passive_a;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant.IDroppable;
import me.rey.core.classes.abilities.IDamageTrigger.IPlayerDamagedEntity;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.players.User;

public class Charge extends Ability implements IDroppable, IPlayerDamagedEntity {
	
	Set<LivingEntity> onCharge = new HashSet<>();

	public Charge() {
		super(331, "Charge", ClassType.IRON, AbilityType.PASSIVE_A, 1, 3, 9, Arrays.asList(
				"Dropping your sword allows you to", 
				"gain Speed II for <variable>3+l</variable> Seconds.",
				"",
				"If you hit someone while you have speed",
				"you deal no KB, but inflict Slow 4",
				"for <variable>2+(0.5*l)</variable> Seconds.", "",
				"Recharge: <variable>9-(0.75*l)</variable> Seconds"
				));
		
			this.setIgnoresCooldown(true);
		}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		
		if(conditions != null && conditions.length > 0 && conditions[0] instanceof DamageEvent) {
			DamageEvent e = (DamageEvent) conditions[0];
			
			if(!onCharge.contains(p)) return false;
		
			e.getDamagee().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) Math.round(2 + (0.5 * level)), 3, false, false));
			e.setKnockbackMult(0.0);
			return true;
		}
		
		if(this.hasCooldown(p)) return false;
		
		double chargeSeconds = 3 + level;
		p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) Math.round(chargeSeconds * 20), 1, false, false));
		this.onCharge.add(p);
		new BukkitRunnable() {
			@Override
			public void run() {
				onCharge.remove(p);
			}
		}.runTaskLater(Warriors.getInstance(), (int) Math.round(chargeSeconds));

		this.setCooldown(9 - (0.75 - level) );
		this.applyCooldown(p);
		
		return true;
	}

}
