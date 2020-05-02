package me.rey.core.classes.abilities.wizard;

import java.util.Arrays;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.classes.abilities.IConstant.ITogglable;
import me.rey.core.classes.abilities.IDamageTrigger;
import me.rey.core.energy.EnergyHandler;
import me.rey.core.enums.State;
import me.rey.core.events.customevents.CustomKnockbackEvent;
import me.rey.core.events.customevents.DamageEvent;
import me.rey.core.events.customevents.DamagedByEntityEvent;
import me.rey.core.events.customevents.UpdateEvent;
import me.rey.core.players.User;

public class Void extends Ability implements IConstant, ITogglable, IDamageTrigger {

	private EnergyHandler handler = new EnergyHandler();
	private final double energyPerSecond = 6;
	
	public Void() {
		super(106, "Void", ClassType.GOLD, AbilityType.PASSIVE_A, 1, 3, 0.00, Arrays.asList(
				"You enter a state of void where",
				"all incoming damage is now reduced",
				"by <variable>1+l</variable> (+1).", "",
				"You received Slowness 3 and Invisibility.",
				"Energy: <variable>6</variable> Per Second"
				));
		
		this.setIgnoresCooldown(true);
		this.setEnergyCost(energyPerSecond / 20); // PER TICK
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		Object arg = conditions[0];
	
		if(arg != null && arg instanceof UpdateEvent) {
			
			// Consuming energy
			if(!this.getEnabledPlayers().contains(p.getUniqueId())) return false;
			handler.togglePauseEnergy(State.ENABLED, p.getUniqueId());
			
			return true;
		}
		
		if(arg != null && (arg instanceof DamageEvent || arg instanceof DamagedByEntityEvent)) {
			// Getting damaged entity, whether they're using void or not (at least one person in the event is using void & is player, so we're making sure)
			LivingEntity damaged = arg instanceof DamageEvent ? ((DamageEvent) arg).getDamagee() : ((DamagedByEntityEvent) arg).getDamagee();
			
			// Checking if damaged is player and is using void
			if(!(damaged instanceof Player)) return false;
			if(!(this.getEnabledPlayers().contains(((Player) damaged).getUniqueId()))) return false;
			
			// Scaling damage down
			if(arg instanceof DamageEvent) {
				((DamageEvent) arg).addMod(-1 - level);
			} else {
				((DamagedByEntityEvent) arg).addMod(-1 - level);
			}
			return false;
		}
		
		return true;
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if(!(e.getEntity() instanceof Player)) return;
		if(!this.getEnabledPlayers().contains(((Player) e.getEntity()).getUniqueId())) return;
		
		if(e.getCause() == DamageCause.FALL)
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onKB(CustomKnockbackEvent e) {
		if(!(e.getDamagee() instanceof Player)) return;
		if(!this.getEnabledPlayers().contains(((Player) e.getDamagee()).getUniqueId())) return;
		
		e.setCancelled(true);
	}

	@Override
	public boolean off(Player p) {
		handler.togglePauseEnergy(State.DISABLED, p.getUniqueId());
		p.removePotionEffect(PotionEffectType.INVISIBILITY);
		p.removePotionEffect(PotionEffectType.SLOW);
		return true;
	}

	@Override
	public boolean on(Player p) {
		handler.togglePauseEnergy(State.ENABLED, p.getUniqueId());
		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 100000, 2, false, false));
		p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 100000, 2, false, false));
		return true;
	}

}
