package me.rey.core.classes.abilities.assassin.passive_b;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IDamageTrigger.IPlayerDamagedEntity;
import me.rey.core.effects.repo.Bleed;
import me.rey.core.effects.repo.Shock;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.players.User;

public class BlitzStrikes extends Ability implements IPlayerDamagedEntity {

	private final double bleedCooldown = 4.0;
	
	Set<Player> onCooldown = new HashSet<>();
	
	public BlitzStrikes() {
		super(041, "Blitz Strikes", ClassType.LEATHER, AbilityType.PASSIVE_B, 1, 3, 0.00, Arrays.asList(
				"test desc"
				));
		
		this.setIgnoresCooldown(true);
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		DamageEvent e = ((DamageEvent) conditions[0]);
		double bleedTime = 1+level;
		double shockTime = 1;
		
		if(!this.onCooldown.contains(p)) {
			onCooldown.add(p);
			
			new BukkitRunnable() {
				@Override
				public void run() {
					onCooldown.remove(p);
				}
				
			}.runTaskLaterAsynchronously(Warriors.getInstance(), (int) (20 * this.bleedCooldown));
		}
		
		
		new Shock().apply(e.getDamagee(), shockTime);
		return true;
	}

}
