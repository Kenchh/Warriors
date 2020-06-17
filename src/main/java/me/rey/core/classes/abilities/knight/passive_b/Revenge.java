package me.rey.core.classes.abilities.knight.passive_b;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IDamageTrigger;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.events.customevents.combat.DamagedByEntityEvent;
import me.rey.core.players.User;

public class Revenge extends Ability implements IDamageTrigger {
	
	private final double cooldownSecs = 5D;
	private static HashMap<Player, BukkitTask> toDealDMG = new HashMap<>();
	private static Set<Player> onCooldown = new HashSet<>();

	public Revenge() {
		super(344, "Revenge", ClassType.IRON, AbilityType.PASSIVE_B, 1, 3, 0.00, Arrays.asList(
				"After taking damage from an enemy,",
				"your next melee attack will deal <variable>1.5+(0.5*l)</variable>",
				"extra damage if you hit within <variable>1.5+(0.5*l)</variable> Seconds."
				));
		this.setIgnoresCooldown(true);
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		
		if(conditions[0] instanceof DamageEvent) {
			
			if(!toDealDMG.containsKey(p)) return false;
			DamageEvent e = (DamageEvent) conditions[0];
			double extraDamage = 1.5 + 0.5 * level;
			e.addMod(extraDamage);
			
			toDealDMG.remove(p);
			onCooldown.add(p);
			new BukkitRunnable() {
				@Override
				public void run() {
					onCooldown.remove(e.getDamagee());
				}
			}.runTaskLater(Warriors.getInstance(), (int) Math.round(20 * cooldownSecs));
			
			return true;
		}
		
		if(conditions[0] instanceof DamagedByEntityEvent) {
			DamagedByEntityEvent e = (DamagedByEntityEvent) conditions[0];
			if(!(e.getDamager() instanceof Player) || onCooldown.contains(e.getDamagee())) return false;
			
			BukkitTask task = new BukkitRunnable() {
				@Override
				public void run() {
					toDealDMG.remove(e.getDamagee());
				}
			}.runTaskLater(Warriors.getInstance(), (int) Math.round(20 * (1.5D + (0.5*level))));
	
			if(toDealDMG.containsKey(e.getDamagee())) {
				toDealDMG.get(e.getDamagee()).cancel();
				toDealDMG.replace(e.getDamagee(), task);
			} else {
				toDealDMG.put(e.getDamagee(), task);
			}

			return false;
		}
		
		return false;
	}

}
