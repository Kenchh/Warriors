package me.rey.core.classes.abilities.bandit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.events.customevents.AbilityUseEvent;
import me.rey.core.events.customevents.DamageEvent;
import me.rey.core.events.customevents.UpdateEvent;
import me.rey.core.players.User;
import me.rey.core.utils.Utils;

public class HiddenAssault extends Ability implements IConstant {
	
	private HashMap<UUID, Double> shifting = new HashMap<>();

	public HiddenAssault() {
		super(705, "Hidden Assault", ClassType.BLACK, AbilityType.PASSIVE_A, 1, 3, 0.00, Arrays.asList(
				"Shifting for <variable>3.5-(0.5*l)</variable> seconds allows",
				"you to become completely invisible.", "",
				"You become visible when you unshift."
				));
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		boolean isShifting = p.isSneaking();
	
		if(!isShifting) {
			System.out.println("not shifting");
			this.shifting.remove(p.getUniqueId());
			return false;
		}
		
		double timeShifting = shifting.containsKey(p.getUniqueId()) ? shifting.get(p.getUniqueId()) : 0.00;
		System.out.println(timeShifting + " shifting");
		
		if(timeShifting == 0.05)
			this.sendAbilityMessage(p, "You are now invisible.");
		
		double INCREMENT = 1 / 20;
		shifting.put(p.getUniqueId(), timeShifting + INCREMENT);
		
		return true;
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent e) {
		for(UUID uuid : shifting.keySet()) {
			Player p = Bukkit.getServer().getPlayer(uuid);
			if(p == null || !p.isOnline()) {
				this.shifting.remove(uuid);
				continue;
			}
			
			Utils.hidePlayer(p);
		}
	}

	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if(!(e.getEntity() instanceof Player)) return;
		if(!this.shifting.containsKey(((Player) e.getEntity()).getUniqueId())) return;
		
		this.sendNoLongerInvis(((Player) e.getEntity()));
	}
	
	@EventHandler
	public void onPlayerHit(DamageEvent e) {
		if(!this.shifting.containsKey(e.getDamager().getUniqueId())) return;
		
		this.sendNoLongerInvis(e.getDamager());
	}
	
	@EventHandler
	public void onAbility(AbilityUseEvent e) {
		if(!this.shifting.containsKey(e.getPlayer().getUniqueId())) return;
		
		this.sendNoLongerInvis(e.getPlayer());
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(!this.shifting.containsKey(e.getPlayer().getUniqueId())) return;
		
		this.sendNoLongerInvis(e.getPlayer());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		if(this.shifting.containsKey(e.getPlayer().getUniqueId()))
			this.sendNoLongerInvis(e.getPlayer());
	}
	
	private void sendNoLongerInvis(Player p) {
		Utils.showPlayer(p);
		this.shifting.remove(p.getUniqueId());
		p.removePotionEffect(PotionEffectType.INVISIBILITY);
		sendAbilityMessage(p, "You are no longer invisible.");
	}
}
