package me.rey.core.effects;

import org.bukkit.entity.Player;

public enum EffectType {
	
	SILENCE, BLEED, SHOCK;
	
	public interface Applyable {
	
		void onApply(Player p, double seconds);
		
		SoundEffect applySound();
		SoundEffect expireSound();
		
		String applyMessage();
		String expireMessage();
		
	}
	
}
