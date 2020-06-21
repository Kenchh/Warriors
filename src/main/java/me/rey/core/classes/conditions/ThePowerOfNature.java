package me.rey.core.classes.conditions;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import me.rey.core.classes.ClassCondition;
import me.rey.core.classes.ClassType;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.events.customevents.update.EnergyUpdateEvent;
import me.rey.core.players.User;
import me.rey.core.pvp.ToolType.HitType;

public class ThePowerOfNature extends ClassCondition {

	public ThePowerOfNature() {
		super(ClassType.GREEN);
	}

	@Override
	protected void execute(User user, Player player) {
		// IGNORE
	}
	
	@EventHandler
	public void onDamage(DamageEvent e) {
		if(!e.getHitType().equals(HitType.MELEE)) return;
		if(new User(e.getDamager()).getWearingClass() != this.getClassType()) return;
		
		new User(e.getDamager()).addEnergy(5D);
	}
	
	@EventHandler
	public void onEnergyUpdate(EnergyUpdateEvent e) {
		if(new User(e.getPlayer()).getWearingClass() != this.getClassType()) return;
		
		e.setIncrement(8D / 20);
	}
	
}
