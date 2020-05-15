package me.rey.core.effects;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.rey.core.Warriors;
import me.rey.core.effects.EffectType.Applyable;
import me.rey.core.effects.repo.Shock;
import me.rey.core.effects.repo.Silence;
import me.rey.core.utils.Text;

public abstract class Effect implements Listener, Applyable {
	
	public static final Effect SILENCE = new Silence();
	public static final Effect SHOCK = new Shock();
	
	protected Set<Player> players;
	protected final String defaultApplyMessage, defaultExpireMessage;
	
	private EffectType type;
	
	public Effect(String name, EffectType type) {
		this.type = type;
		this.players = new HashSet<>();
		
		this.defaultApplyMessage = "You are now " + name.toLowerCase() + (name.endsWith("e") ? "d" : "ed")+ ".";
		this.defaultExpireMessage = "You are no longer " + name.toLowerCase() + (name.endsWith("e") ? "d" : "ed")+ ".";
		
		Bukkit.getServer().getPluginManager().registerEvents(this, Warriors.getInstance());
	}
	
	@EventHandler
	public void onLog(PlayerQuitEvent e) {
		if(hasEffect(e.getPlayer())) this.expireForcefully(e.getPlayer());
	}
	
	public boolean hasEffect(Player p) {
		return players.contains(p);
	}
	
	public EffectType getType() {
		return type;
	}
	
	public void expireForcefully(Player p) {
		if(this.players.contains(p))
			this.players.remove(p);
	}
	
	public void apply(Player p, double seconds) {
		if(p == null) return;
		
		this.players.add(p);
		this.onApply(p, seconds);
		
		if(this.applySound() != null) this.applySound().play(p);
		if(this.applyMessage() != null) p.sendMessage(Text.format("Effect", this.applyMessage()));;
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				players.remove(p);
				
				if(expireSound() != null) expireSound().play(p);
				if(expireMessage() != null) p.sendMessage(Text.format("Effect", expireMessage()));
			}
			
		}.runTaskLater(Warriors.getInstance(), (int) Math.round(seconds * 20));
	}

}
