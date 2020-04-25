package me.rey.core.classes.abilities.knight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.players.User;

public class HoldPosition extends Ability {

	private ArrayList<UUID> players;
	
	public HoldPosition() {
		super(301, "Hold Position", ClassType.IRON, AbilityType.AXE, 1, 5, 18, Arrays.asList(
				"Hold your position, gaining",
				"Protection 4, Slow 4 and no",
				"knockback for <variable>l+3</variable> (+1) seconds.",
				"",
				"Recharge: <variable>2*l+16</variable> (+2) Seconds"
				));
		
		this.setWhileSlowed(true);
		this.players = new ArrayList<>();
	}

	@Override
	protected boolean execute(User u, final Player p, int level, Object... conditions) {
		int seconds = level + 3, cooldown = 2 * level + 16;
		
		p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, seconds * 20, 3));
		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, seconds * 20, 3));
		p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, seconds * 20, 200, false, false));
		
		this.players.add(p.getUniqueId());
		new BukkitRunnable() {
			
			@Override
			public void run() {
				players.remove(p.getUniqueId());
			}
		}.runTaskLater(Warriors.getInstance(), seconds * 20);
		
		this.sendUsedMessageToPlayer(p, this.getName());
		this.setCooldown(cooldown);
		return true;
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if(!(e.getEntity() instanceof Player)) return;
		if(!this.players.contains(((Player )e.getEntity()).getUniqueId())) return;
		
		if(e.getCause().equals(DamageCause.FALL))
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onVelocityEVent(PlayerVelocityEvent e) {
		if(!this.players.contains(e.getPlayer().getUniqueId())) return;
		
		e.setCancelled(true);
	}

}
