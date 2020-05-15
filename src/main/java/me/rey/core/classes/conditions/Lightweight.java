package me.rey.core.classes.conditions;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import me.rey.core.classes.ClassCondition;
import me.rey.core.classes.ClassType;
import me.rey.core.events.customevents.combat.CustomKnockbackEvent;
import me.rey.core.players.User;

public class Lightweight extends ClassCondition {

	public Lightweight() {
		super(ClassType.LEATHER);
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
	public void onFall(EntityDamageEvent e) {
		if(!(e.getEntity() instanceof Player)) return;
		if(new User((Player) e.getEntity()).getWearingClass() != this.getClassType()) return;
		if(e.getCause() != DamageCause.FALL) return;
				
		e.setDamage(Math.max(e.getDamage() - 4.00, 0.00));

		if(e.getDamage() <= 0.00)
			e.setCancelled(true);
	}

}
