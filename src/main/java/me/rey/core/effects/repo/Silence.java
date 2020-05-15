package me.rey.core.effects.repo;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.effects.Effect;
import me.rey.core.effects.EffectType;
import me.rey.core.effects.SoundEffect;

public class Silence extends Effect {

	public static final List<AbilityType> silencedAbilities = Arrays.asList(AbilityType.BOW, AbilityType.SWORD, AbilityType.SPADE, AbilityType.AXE, AbilityType.PASSIVE_A);
	public static final SoundEffect SOUND = new SoundEffect(Sound.BAT_HURT, 0.8F).setVolume(0.8F);
	
	public Silence() {
		super("Silence", EffectType.SILENCE);
	}

	@Override
	public SoundEffect applySound() {
		return SOUND;
	}

	@Override
	public SoundEffect expireSound() {
		return null;
	}

	@Override
	public String applyMessage() {
		return null;
	}

	@Override
	public String expireMessage() {
		return this.defaultExpireMessage;
	}

	@Override
	public void onApply(LivingEntity ent, double seconds) {
		return;
	}

}
