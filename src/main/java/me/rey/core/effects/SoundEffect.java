package me.rey.core.effects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundEffect {
	
	// PREMADE SOUNDS
	public static final SoundEffect TANK_A = new SoundEffect(Sound.BLAZE_HIT, 0.7f);
	public static final SoundEffect TANK_B = new SoundEffect(Sound.BLAZE_HIT, 0.9f);
	public static final SoundEffect TANK_C = new SoundEffect(Sound.BLAZE_HIT, 0.5f);
	public static final SoundEffect SOFT = new SoundEffect(Sound.SHOOT_ARROW, 2.0f);
	public static final SoundEffect MILD_TANK_A = new SoundEffect(Sound.ITEM_BREAK, 1.4f);
	public static final SoundEffect MILD_TANK_B = new SoundEffect(Sound.ITEM_BREAK, 1.8f);
	
	
	private Sound sound;
	private float pitch, volume;
	
	public SoundEffect(Sound sound, float pitch) {
		this.pitch = pitch;
		this.sound = sound;
		this.volume = 1F;
	}
	
	public float getVolume() {
		return volume;
	}
	
	public Sound getSound() {
		return sound;
	}
	
	public float getPitch() {
		return pitch;
	}
	
	public SoundEffect setVolume(float volume) {
		this.volume = volume;
		return this;
	}
	
	public SoundEffect setSound(Sound sound) {
		this.sound = sound;
		return this;
	}
	
	public SoundEffect setPitch(float pitch) {
		this.pitch = pitch;
		return this;
	}
	
	public void play(Location loc) {
		loc.getWorld().playSound(loc, this.getSound(), 1.0F, this.getPitch());
	}
	
	public void play(Player player) {
		player.playSound(player.getLocation(), this.getSound(), 1.0F, this.getPitch());
	}
	
	public static void playCustomSound(Location loc, String sound, float volume, float pitch) {
		for(Player p : Bukkit.getOnlinePlayers()) {
			p.playSound(loc, sound, volume, pitch);
		}
		return;
	}

}
