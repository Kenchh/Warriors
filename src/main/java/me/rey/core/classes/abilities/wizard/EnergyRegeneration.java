package me.rey.core.classes.abilities.wizard;

import java.util.Arrays;

import org.bukkit.entity.Player;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.energy.EnergyHandler;
import me.rey.core.energy.IEnergyEditor;
import me.rey.core.events.customevents.EnergyUpdateEvent;
import me.rey.core.players.User;

public class EnergyRegeneration extends Ability implements IConstant, IEnergyEditor {
	
	EnergyHandler energyHandler = new EnergyHandler();

	public EnergyRegeneration() {
		super(104, "Energy Regeneration", ClassType.GOLD, AbilityType.PASSIVE_C, 1, 5, 0.00, Arrays.asList(
				"Your energy now recovers with",
				"a <variable>20*l</variable>% (+20%) speed bonus.", "",
				"Max of 100% speed bonus."
				));
		this.setIgnoresCooldown(true);
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		EnergyUpdateEvent e = (EnergyUpdateEvent) conditions[0];
		e.addExtraSpeed(level * 0.2);
		return true;
	}

}
