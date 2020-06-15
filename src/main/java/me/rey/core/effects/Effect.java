package me.rey.core.effects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.rey.core.Warriors;
import me.rey.core.effects.EffectType.Applyable;
import me.rey.core.utils.Text;

public abstract class Effect implements Listener, Applyable {
	
	private static HashMap<Class<? extends Effect>, Set<LivingEntity>> players = new HashMap<>();
	private static Effect[] CUSTOM_EFFECTS = new Effect[0];
	private static final PotionEffectType[] BAD_EFFECTS = {
			PotionEffectType.BLINDNESS,
			PotionEffectType.CONFUSION,
			PotionEffectType.HARM,
			PotionEffectType.HUNGER,
			PotionEffectType.POISON,
			PotionEffectType.SLOW,
			PotionEffectType.SLOW_DIGGING,
			PotionEffectType.WEAKNESS,
			PotionEffectType.WITHER
	};
	
	protected final String defaultApplyMessage, defaultExpireMessage;
	
	private EffectType type;
	
	public Effect(String name, EffectType type) {
		if(!players.containsKey(this.getClass())) players.put(this.getClass(), new HashSet<>());
		this.type = type;
		
		this.defaultApplyMessage = "You are now " + name.toLowerCase() + (name.endsWith("e") ? "d" : "ed")+ ".";
		this.defaultExpireMessage = "You are no longer " + name.toLowerCase() + (name.endsWith("e") ? "d" : "ed")+ ".";
		
		Bukkit.getServer().getPluginManager().registerEvents(this, Warriors.getInstance());
		
		Effect[] copy = CUSTOM_EFFECTS.clone();
		boolean found = false;
		for(Effect eff : CUSTOM_EFFECTS)
			if(eff.getType().equals(type))
				found = true;
		
		if(!found) {
			CUSTOM_EFFECTS = new Effect[CUSTOM_EFFECTS.length+1];
			for(int i = 0; i < copy.length; i++)
				CUSTOM_EFFECTS[i] = copy[i];
			
			CUSTOM_EFFECTS[copy.length] = this;
		}
	}
	
	@EventHandler
	public void onLog(PlayerQuitEvent e) {
		if(hasEffect(this.getClass(), e.getPlayer())) this.expireForcefully(e.getPlayer());
	}
	
	public static boolean hasEffect(Class<? extends Effect> clazz, LivingEntity ent) {
		return players.containsKey(clazz) && players.get(clazz).contains(ent);
	}
	
	public EffectType getType() {
		return type;
	}
	
	public void expireForcefully(LivingEntity p) {
		if(players.get(this.getClass()).contains(p))
			players.get(this.getClass()).remove(p);
	}
	
	public void apply(LivingEntity ent, double seconds) {
		if(ent == null) return;
		
		players.get(this.getClass()).add(ent);
		this.onApply(ent, seconds);
		
		if(this.applySound() != null && ent instanceof Player) this.applySound().play((Player) ent);
		if(this.applyMessage() != null && ent instanceof Player) ent.sendMessage(Text.format("Effect", this.applyMessage()));;
		
		Class<? extends Effect> clazz = this.getClass();
		new BukkitRunnable() {
			@Override
			public void run() {
				if(!players.get(clazz).contains(ent)) return;
				players.get(clazz).remove(ent);
				
				if(expireSound() != null && ent instanceof Player) expireSound().play((Player) ent);
				if(expireMessage() != null) ent.sendMessage(Text.format("Effect", expireMessage()));
			}
			
		}.runTaskLater(Warriors.getInstance(), (int) Math.round(seconds * 20));
	}

	public static void clearAllEffects(LivingEntity ent, List<Class<? extends Effect>> exclude) {
		List<PotionEffectType> active = new ArrayList<>();
		for(PotionEffectType type : BAD_EFFECTS)
			ent.getActivePotionEffects().forEach((e) -> {
				if(e.getType().equals(type))
					active.add(type);
			});
		
		for(PotionEffectType act : active)
			ent.removePotionEffect(act);
		
		if(exclude == null || exclude.size() < 1) return;
		for(Effect eff : CUSTOM_EFFECTS)
			if(hasEffect(eff.getClass(), ent) && !exclude.contains(eff.getClass()))
				eff.expireForcefully(ent);
	}

}
