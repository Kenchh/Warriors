package me.rey.core.classes.conditions;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.rey.core.classes.ClassCondition;
import me.rey.core.classes.ClassType;
import me.rey.core.players.User;
import me.rey.core.utils.UtilBlock;

public class Balanced extends ClassCondition {
	
	PotionEffect STRENGTH = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 15, 0);
	PotionEffect RESISTANCE = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 15, 0);

	public Balanced() {
		super(ClassType.IRON);
	}

	@Override
	protected void execute(User user, Player player) {

		int playerCount = 0;
		for(Entity e : UtilBlock.getEntitiesInCircle(player.getLocation(), 5F)) {
			
			if(!(e instanceof Player)) continue;
			
			Player ent = (Player) e;
			if(user.getTeam().contains(ent)) continue;

			if(player != ent)
			playerCount++;
		}
		
		if(playerCount == 1) {

			// GIVE STRENGTH
			player.addPotionEffect(STRENGTH);
			player.addPotionEffect(RESISTANCE);
	
		}
		
	}

}
