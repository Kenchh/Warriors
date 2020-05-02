package me.rey.core.classes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.rey.core.classes.abilities.ninja.Leap;
import me.rey.core.gui.Gui.Item;
import me.rey.core.pvp.Build;
import me.rey.core.pvp.Build.DefaultBuild;
import me.rey.core.utils.Utils;

public enum ClassType {
	
	LEATHER(28, "Assassin",
			new DefaultBuild(new Leap().setTempDefaultLevel(4)),
			Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS
	),
	
	CHAIN(36, "Berserker",
			new DefaultBuild(),
			Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS
	),
	
	GOLD(34, "Sibyl",
			new DefaultBuild(),
			Material.GOLD_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS
	),
	
	IRON(50, "Knight",
			new DefaultBuild(),
			Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS
	),
	
	DIAMOND(48, "Brute",
			new DefaultBuild(),
			Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS
	),
	
	BANDIT(30, "Bandit",
			new DefaultBuild(),
			Utils.getColoredArmor(Material.LEATHER_HELMET, 0, 0, 255), Utils.getColoredArmor(Material.LEATHER_CHESTPLATE, 0, 0, 255),
			Utils.getColoredArmor(Material.LEATHER_LEGGINGS, 0, 0, 255), Utils.getColoredArmor(Material.LEATHER_BOOTS, 0, 0, 255),
			new PotionEffect(PotionEffectType.SPEED, 20 * 100000, 1)
			);
	
	private String name;
	private Item helmet, chestplate, leggings, boots;
	private PotionEffect[] effects;
	private DefaultBuild defaultBuild;
	private double health;
	
	ClassType(double health, String name, DefaultBuild defaultBuild, Material helmet, Material chestplate, Material leggings, Material boots, PotionEffect... effects){
		this.health = health;
		this.name = name;
		this.helmet = new Item(helmet);
		this.chestplate = new Item(chestplate);
		this.leggings = new Item(leggings);
		this.boots = new Item(boots);
		this.effects = effects;
		this.defaultBuild = defaultBuild;
	}
	
	ClassType(double health, String name, DefaultBuild defaultBuild, ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots, PotionEffect... effects){
		this.health = health;
		this.name = name;
		this.helmet = new Item(helmet).setName(null);
		this.chestplate = new Item(chestplate).setName(null);
		this.leggings = new Item(leggings).setName(null);
		this.boots = new Item(boots).setName(null);
		this.effects = effects;
		this.defaultBuild = defaultBuild;
	}
	
	public double getHealth() {
		return health;
	}

	public String getName() {
		return name;
	}

	public Item getHelmet() {
		return helmet;
	}

	public Item getChestplate() {
		return chestplate;
	}

	public Item getLeggings() {
		return leggings;
	}

	public Item getBoots() {
		return boots;
	}
	
	public Item[] getArmor() {
		return new Item[] {this.getHelmet(), this.getChestplate(), this.getLeggings(), this.getBoots()};
	}
	
	public PotionEffect[] getEffects() {
		return effects;
	}

	public Build getDefaultBuild() {
		return this.defaultBuild;
	}
	
}
