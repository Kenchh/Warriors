package me.rey.core.classes.abilities.assassin.bow;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IBowPreparable;
import me.rey.core.effects.repo.Marked;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.players.User;

public class Premonition extends Ability implements IBowPreparable {
	
	private Set<UUID> prepared = new HashSet<>(), shot = new HashSet<UUID>();

	public Premonition() {
		super(021, "Premonition", ClassType.LEATHER, AbilityType.BOW, 1, 3, 10, Arrays.asList(
				"When hit, a player will take",
				"<variable>4+(1.75*l)</variable> more damage on the next",
				"melee attack.", "",
				"Recharge: <variable>10-l</variable> Seconds"
				));
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		
		if(conditions.length == 1 && conditions[0] instanceof PlayerInteractEvent) {
			this.prepare(p);
			return true;
		}
		
		if(conditions.length == 1 && conditions[0] instanceof DamageEvent) {
			DamageEvent e = (DamageEvent) conditions[0];
			
			Marked effect = new Marked(4 + (1.75 * level));
			effect.expireForcefully(e.getDamagee());
			effect.apply(e.getDamagee(), 7.0D);
		}
		
		return false;
	}
	
	@Override
	public boolean prepare(Player player) {
		return prepared.add(player.getUniqueId());
	}
	
	@Override
	public boolean isPrepared(Player player) {
		return prepared.contains(player.getUniqueId());
	}
	
	@Override
	public boolean unprepare(Player player) {
		return prepared.remove(player.getUniqueId());
	}

	@Override
	public boolean shoot(Player player) {
		return shot.add(player.getUniqueId());
	}

	@Override
	public boolean hasShot(Player player) {
		return shot.contains(player.getUniqueId());
	}

	@Override
	public boolean unshoot(Player player) {
		return shot.remove(player.getUniqueId());
	}
	

}
