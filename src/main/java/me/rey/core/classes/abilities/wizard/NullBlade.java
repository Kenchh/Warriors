package me.rey.core.classes.abilities.wizard;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IDamageTrigger;
import me.rey.core.classes.abilities.IDamageTrigger.IPlayerDamagedEntity;
import me.rey.core.events.customevents.DamageEvent;
import me.rey.core.players.User;
import me.rey.core.pvp.ToolType;

public class NullBlade extends Ability implements IPlayerDamagedEntity {

	public NullBlade() {
		super(102, "Null Blade", ClassType.GOLD, AbilityType.PASSIVE_B, 1, 3, 0.00, Arrays.asList(
				"Your attacks suck the life from",
				"opponents, restoring <variable>2*l+4</variable> (+2) energy."
				));
		
		this.setIgnoresCooldown(true);
		this.setInLiquid(true);
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		DamageEvent e = (DamageEvent) conditions[0];
		
		if(p.getItemInHand() == null || p.getItemInHand().getType().equals(Material.AIR)) return false;
		Material type = p.getItemInHand().getType();
		boolean used = false;
		
		for(ToolType tool : ToolType.values()) {
			if(!tool.getType().equals(type)) continue;
			
			new User(e.getDamager()).addEnergy(2*level  + 4);
		}
		
		return used;
	}

}
