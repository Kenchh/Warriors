package me.rey.core.classes.abilities.bandit.passive_a;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant.IDroppable;
import me.rey.core.enums.AbilityFail;
import me.rey.core.events.customevents.AbilityFailEvent;
import me.rey.core.events.customevents.UpdateEvent;
import me.rey.core.players.User;
import me.rey.core.utils.Utils;

public class Recall extends Ability implements IDroppable {

	private static HashMap<UUID, ArrayList<Location>> positionSaved = new HashMap<>();
	private double maxSecondsToSave;
	private final double recallSeconds = 4.0;
	
	public Recall() {
		super(132, "Recall", ClassType.BLACK, AbilityType.PASSIVE_A, 1, 3, 25, Arrays.asList(
				"Travel back in time to the",
				"last 4 seconds while instantly",
				"healing for <variable>6+l</variable> (+1) health points.", "",
				"Use this ability while shifting to",
				"trigger Secondary Recall and",
				"teleport back <variable>1.75+(0.25*l)</variable> (+0.25) seconds while",
				"healing for <variable>2.25+(0.25*l)</variable> (+0.25) health points.","",
				"Recharge: <variable>27-(2*l)</variable> (-2) Seconds"
				));
		
		this.setWhileSlowed(false);
		this.maxSecondsToSave = Math.max(recallSeconds + 1, 2.25 + (0.25 * this.getMaxLevel()));
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		return recall(u, p, level, conditions != null && conditions.length > 0 && (boolean) conditions[0] == true ? true : false);
	}
	
	@EventHandler
	public void positionSaving(UpdateEvent e) {
		
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(!(new User(p).isUsingAbility(this))) return;
			
			ArrayList<Location> already = positionSaved.get(p.getUniqueId()) != null ? positionSaved.get(p.getUniqueId()) : new ArrayList<>();
			Location loc = p.getLocation().clone();
			
			if(already.size() > (maxSecondsToSave / (1.00 / 20.00)+5))
				already.remove(0);
			already.add(loc);
			positionSaved.put(p.getUniqueId(), already);
		}
	}
	
	@EventHandler
	public void onSecondaryRecall(AbilityFailEvent e) {
		if(e.getAbility() == this && e.getFail() == AbilityFail.SLOWED) {
			e.setMessageCancelled(true);
			this.run(e.getPlayer(), null, true, true);
		}
	}
	
	private boolean recall(User u, Player p, int level, boolean forceSecondary) {
		boolean isShifting = p.isSneaking() || forceSecondary;
		double toHeal = isShifting ? 1.75 * (0.25 * level) : 6 + level;
		double longAgoTeleport = isShifting ? 2.25 + (0.25 * level) : 4;
		
		// teleporting
		ArrayList<Location> saved = positionSaved.get(p.getUniqueId()) != null ? positionSaved.get(p.getUniqueId()) : new ArrayList<>();
		int index = (int) ((saved.size()-1) - longAgoTeleport / (1.00 / 20.00));
		Location toTeleport = saved == null || saved.isEmpty() ? p.getLocation() : saved.get(index >= saved.size() || index < 0 ? 0 : (int) index);
		Location origin = p.getLocation().clone();
		toTeleport.setYaw(p.getLocation().getYaw());
		toTeleport.setPitch(p.getLocation().getPitch());
		p.teleport(toTeleport);
		p.setFallDistance(-10);
		
		// Health
		p.setHealth(Math.min(p.getHealth() + toHeal, p.getMaxHealth()));
		
		// Particles & Sound
		this.makeParticlesBetween(origin, toTeleport);
		for(int i = 0; i < 2; i ++) {
			p.getWorld().playSound(origin, Sound.ENDERMAN_TELEPORT, 1, 1);
			p.getWorld().playSound(toTeleport, Sound.ENDERMAN_TELEPORT, 1, 1);			
		}
		
		this.sendUsedMessageToPlayer(p, isShifting ? "Secondary " + this.getName() : this.getName());
		this.setCooldown(!isShifting ? 27 - (2 * level) : 12 - level);
		return true;
	}
	
	/*
	 * PARTICLES
	 */
	private void makeParticlesBetween(Location init, Location loc) {
		Vector pvector = Utils.getDirectionBetweenLocations(init, loc);
		for(double i = 1; i <= init.distance(loc); i += 0.2) {
			pvector.multiply(i);
			init.add(pvector);
			Location toSpawn = init.clone();
			toSpawn.setY(toSpawn.getY() + 0.5);
			init.getWorld().spigot().playEffect(toSpawn, Effect.PORTAL, 0, 0, 0F, 0.3F, 0F, 0F, 10, 50);
			init.subtract(pvector);
			pvector.normalize();
		}
	}

}
