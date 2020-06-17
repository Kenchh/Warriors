package me.rey.core.classes.abilities.knight.passive_b;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IDamageTrigger.IPlayerDamagedEntity;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.players.User;

public class Advantage extends Ability implements IPlayerDamagedEntity {

	private final double expireFirstHit = 5;
	private static Set<Player> hits = new HashSet<>();
	
	public Advantage() {
		super(343, "Advantage", ClassType.IRON, AbilityType.PASSIVE_A, 1, 3, 10.00, Arrays.asList(
				"Your first melee attack inflicts",
				"Weakness I for <variable>2.5+l</variable> Seconds.","",
				"Recharge: <variable>10-l</variable> Seconds"
				));
		this.setIgnoresCooldown(true);
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		DamageEvent e = (DamageEvent) conditions[0];
		if(hits.contains(p) || !(e.getDamagee() instanceof Player)) return false;
		
		double weaknessTime = 2.5+level;
		PotionEffect weakness = new PotionEffect(PotionEffectType.WEAKNESS, (int) Math.round(20 * weaknessTime), 0, false, false);
		e.getDamagee().addPotionEffect(weakness);
		
		hits.add(p);
		new BukkitRunnable() {
			@Override
			public void run() {
				hits.remove(p);
			}
		}.runTaskLater(Warriors.getInstance(), (int) Math.round(20 * expireFirstHit));
		
		return true;
	}

}
