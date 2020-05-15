package me.rey.core.classes.conditions;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.rey.core.classes.ClassCondition;
import me.rey.core.classes.ClassType;
import me.rey.core.events.customevents.combat.CustomKnockbackEvent;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.players.User;

public class Vigour extends ClassCondition {

	public Vigour() {
		super(ClassType.BLACK);
	}

	@Override
	protected void execute(User user, Player player) {
		// IGNORE
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onCustomKB(CustomKnockbackEvent e) {
		if(!(e.getDamager() instanceof Player)) return;
		if(new User((Player) e.getDamager()).getWearingClass() != this.getClassType()) return;

		e.setCancelled(true);
	}
	
	@EventHandler
	public void onStrength(DamageEvent e) {
		if(new User(e.getDamager()).getWearingClass() != this.getClassType()) return;
		
		e.getDamager().addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 5, 0, false, false));
	}

}
