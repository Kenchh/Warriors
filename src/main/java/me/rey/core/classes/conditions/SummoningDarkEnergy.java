package me.rey.core.classes.conditions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import me.rey.core.classes.ClassCondition;
import me.rey.core.classes.ClassType;
import me.rey.core.events.customevents.combat.DeathEvent;
import me.rey.core.events.customevents.update.EnergyUpdateEvent;
import me.rey.core.players.User;

public class SummoningDarkEnergy extends ClassCondition {

	public SummoningDarkEnergy() {
		super(ClassType.GOLD);
	}

	@Override
	protected void execute(User user, Player player) {
		// INGORE
	}
	
	@EventHandler
	public void onKill(DeathEvent e) {
		if (e.getLastHit() == null || !e.getLastHit().isCausedByPlayer()) return;
		Player hitter = Bukkit.getServer().getPlayer(e.getLastHit().getDamager());
		if (!new User(hitter).getWearingClass().equals(this.getClassType())) return;
		
		new User(hitter).addEnergy(50D);
	}
	
	@EventHandler
	public void onEnergyUpdate(EnergyUpdateEvent e) {
		if(!new User(e.getPlayer()).getWearingClass().equals(this.getClassType())) return;
		
		e.setIncrement(10D / 20);
	}

}
