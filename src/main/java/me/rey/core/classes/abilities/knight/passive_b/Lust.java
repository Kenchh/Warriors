package me.rey.core.classes.abilities.knight.passive_b;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.events.customevents.combat.DeathEvent;
import me.rey.core.players.User;
import me.rey.core.players.combat.PlayerHit;

public class Lust extends Ability {

	public Lust() {
		super(341, "Lust", ClassType.IRON, AbilityType.PASSIVE_B, 1, 3, 0.00, Arrays.asList(
				"After getting a kill on another player",
				"you receive a Regeneration effect",
				"for <variable>5+l</variable> (+1) Seconds."
				));

		this.setIgnoresCooldown(true);
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		double seconds = 5 + level;
		PotionEffect regen = new PotionEffect(PotionEffectType.REGENERATION, (int) Math.round(20 * seconds), 0, false, false);

		p.addPotionEffect(regen);
		return true;
	}

	@EventHandler
	public void onDeath(DeathEvent e) {
		PlayerHit hit = e.getLastHit();
		if (hit != null && hit.isCausedByPlayer()) {
			Player p = Bukkit.getServer().getPlayer(hit.getDamager());
			if(p == null || !p.isOnline()) return;
			
			User u = new User(p);
			
			if(!u.isUsingAbility(this)) return;
			super.run(u.getPlayer(), null, true);
		}
	}

}
