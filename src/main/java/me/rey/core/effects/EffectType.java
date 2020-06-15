package me.rey.core.effects;

import org.bukkit.entity.LivingEntity;

public enum EffectType {
	
	SILENCE, BLEED, SHOCK, MARK;
	
	public interface Applyable {
	
		void onApply(LivingEntity ent, double seconds);
		
		SoundEffect applySound();
		SoundEffect expireSound();
		
		String applyMessage();
		String expireMessage();
		
	}
	
}
