package me.rey.core.classes.abilities.bandit;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.events.customevents.CustomPlayerInteractEvent;
import me.rey.core.events.customevents.UpdateEvent;
import me.rey.core.players.User;
import me.rey.core.utils.Utils;

public class HiddenAssault extends Ability implements IConstant {
	
	private Set<UUID> shifting = new HashSet<>(), loading = new HashSet<UUID>();

	public HiddenAssault() {
		super(705, "Hidden Assault", ClassType.BLACK, AbilityType.PASSIVE_A, 1, 3, 0.00, Arrays.asList(
				"Shifting for <variable>3.5-(0.5*l)</variable> seconds allows",
				"you to become completely invisible.", "",
				"You become visible when you unshift."
				));
		
		this.setIgnoresCooldown(true);
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		boolean isShifting = p.isSneaking(), isInLiquid = isInLiquid(p);
		double requiredShiftTime = 3.5 - (0.5 * level);
	
		if(!isShifting || isInLiquid) {
			if(this.isUsing(p))
				this.remove(p);
			return false;
		}
		
		if(isShifting && !this.shifting.contains(p.getUniqueId()) && !loading.contains(p.getUniqueId())) {
			loading.add(p.getUniqueId());
			
			BukkitTask task = new BukkitRunnable() {
				@Override
				public void run() {
					if(loading.contains(p.getUniqueId()) && !shifting.contains(p.getUniqueId())) {
						shifting.add(p.getUniqueId());
						loading.remove(p.getUniqueId());
						p.removePotionEffect(PotionEffectType.INVISIBILITY);
						sendAbilityMessage(p, "You are now invisible.");
						p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 100000, 1, false, false));
						Utils.hidePlayer(p);
						
						Location loc = p.getEyeLocation();
						loc.setY(loc.getY() + 1);
						p.getWorld().spigot().playEffect(loc, Effect.LARGE_SMOKE, 0, 0, 0.1F, 0.3F, 0.1F, 0F, 50, 50);
					}
				}
			}.runTaskLater(Warriors.getInstance(), (int) (requiredShiftTime * 20));
			
			new BukkitRunnable() {
				@Override
				public void run() {
					if(!p.isSneaking() || isInLiquid(p)) {
						loading.remove(p.getUniqueId());
						shifting.remove(p.getUniqueId());
						task.cancel();
						this.cancel();
						return;
					} else if (!loading.contains(p.getUniqueId())) {
						task.cancel();
						this.cancel();
					}
					
				}
			}.runTaskTimer(Warriors.getInstance(), 0, 1);
			
		}
		
		if(isShifting && this.shifting.contains(p.getUniqueId())) {
			Utils.hidePlayer(p);
		}
		
		return true;
	}
	
	@EventHandler
	public void hidingPlayers(UpdateEvent e) {
		for(UUID uuid : shifting) {
			Player p = Bukkit.getServer().getPlayer(uuid);
			if(p == null || !p.isOnline()) {
				this.shifting.remove(uuid);
				continue;
			}
			
			if(!p.isSneaking() || isInLiquid(p)) {
				this.sendNoLongerInvis(p);
				continue;
			}
		}
	}
	
	@EventHandler
	public void onPlayerHit(CustomPlayerInteractEvent e) {
		if(this.isUsing(e.getPlayer()))
			this.remove(e.getPlayer());
	}
	
	private void remove(Player p) {
		if(this.isUsing(p)) {
			if(this.shifting.contains(p.getUniqueId()))
				this.sendNoLongerInvis(p);
			this.shifting.remove(p.getUniqueId());
			this.loading.remove(p.getUniqueId());
		}
	}
	
	private boolean isInLiquid(Player p) {
		return p.getLocation().getBlock() != null && p.getLocation().getBlock().isLiquid();
	}
	
	private void sendNoLongerInvis(Player p) {
		Utils.showPlayer(p);
		this.shifting.remove(p.getUniqueId());
		this.loading.remove(p.getUniqueId());
		p.removePotionEffect(PotionEffectType.INVISIBILITY);
		sendAbilityMessage(p, "You are no longer invisible.");
	}
	
	private boolean isUsing(Player p) {
		return this.shifting.contains(p.getUniqueId()) || this.loading.contains(p.getUniqueId());
	}
}