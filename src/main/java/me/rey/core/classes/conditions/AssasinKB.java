package me.rey.core.classes.conditions;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import me.rey.core.classes.ClassCondition;
import me.rey.core.classes.ClassType;
import me.rey.core.events.customevents.CustomKnockbackEvent;
import me.rey.core.players.User;

public class AssasinKB extends ClassCondition {

	public AssasinKB() {
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

}
