package me.rey.core.classes;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.rey.core.classes.abilities.ninja.Leap;
import me.rey.core.pvp.Build;
import me.rey.core.pvp.Build.DefaultBuild;

public enum ClassType {
	
	LEATHER("Ninja",
			new DefaultBuild(new Leap().setTempDefaultLevel(4)),
			Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
			new PotionEffect(PotionEffectType.SPEED, 1000000, 1)
	),
	
	CHAIN("Marksman",
			new DefaultBuild(),
			Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS
	),
	
	GOLD("Wizard",
			new DefaultBuild(),
			Material.GOLD_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS
	),
	
	IRON("Knight",
			new DefaultBuild(),
			Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS
	),
	
	DIAMOND("Brute",
			new DefaultBuild(),
			Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS
	);
	
	private String name;
	private Material helmet, chestplate, leggings, boots;
	private PotionEffect[] effects;
	private DefaultBuild defaultBuild;
	
	ClassType(String name, DefaultBuild defaultBuild, Material helmet, Material chestplate, Material leggings, Material boots, PotionEffect... effects){
		this.name = name;
		this.helmet = helmet;
		this.chestplate = chestplate;
		this.leggings = leggings;
		this.boots = boots;
		this.effects = effects;
		this.defaultBuild = defaultBuild;
	}

	public String getName() {
		return name;
	}

	public Material getHelmet() {
		return helmet;
	}

	public Material getChestplate() {
		return chestplate;
	}

	public Material getLeggings() {
		return leggings;
	}

	public Material getBoots() {
		return boots;
	}
	
	public Material[] getArmor() {
		return new Material[] {this.getHelmet(), this.getChestplate(), this.getLeggings(), this.getBoots()};
	}
	
	public PotionEffect[] getEffects() {
		return effects;
	}

	public Build getDefaultBuild() {
		return this.defaultBuild;
	}
	
}
