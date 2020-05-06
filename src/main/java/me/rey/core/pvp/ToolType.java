package me.rey.core.pvp;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.rey.core.gui.Gui.Item;
import me.rey.core.utils.Text;
import net.md_5.bungee.api.ChatColor;

public enum ToolType {
	
	POWER_SWORD(7, "&fPower Sword", Material.DIAMOND_SWORD, HitType.MELEE),
	POWER_AXE(7, "&fPower Axe", Material.DIAMOND_AXE, HitType.MELEE),
	POWER_SPADE(7, "&fPower Spade", Material.DIAMOND_SPADE, HitType.MELEE),
	BOOSTER_SWORD(true, 6, "&fBooster Sword", Material.GOLD_SWORD, HitType.MELEE),
	BOOSTER_AXE(true, 6, "&fBooster Axe", Material.GOLD_AXE, HitType.MELEE),
	BOOSTER_SPADE(true, 6, "&fBooster Spade", Material.GOLD_SPADE, HitType.MELEE),
	STANDARD_SWORD(6, "&fStandard Sword", Material.IRON_SWORD, HitType.MELEE),
	STANDARD_AXE(6, "&fStandard Axe", Material.IRON_AXE, HitType.MELEE),
	STANDARD_SPADE(6, "&fStandard Spade", Material.IRON_SPADE, HitType.MELEE),
	STANDARD_BOW(5, "&fStandard Bow", Material.BOW, HitType.ARCHERY);

	private int damage;
	private String name;
	private Material item;
	private HitType hitType;
	private boolean booster;
	
	ToolType(boolean isBooster, int damage, String name, Material item, HitType hitType){
		this.booster = isBooster;
		this.damage = damage;
		this.hitType = hitType;
		this.name = ChatColor.translateAlternateColorCodes('&', name);
		this.item = item;
	}
	
	ToolType(int damage, String name, Material item, HitType hitType){
		this.booster = false;
		this.damage = damage;
		this.hitType = hitType;
		this.name = ChatColor.translateAlternateColorCodes('&', name);
		this.item = item;
	}
	
	public boolean isBooster() {
		return booster;
	}
	
	public HitType getHitType() {
		return hitType;
	}
	
	public int getDamage() {
		return damage;
	}
	public String getName() {
		return Text.color(name);
	}
	public Material getType() {
		return item;
	}
	public ItemStack getItemStack() {
		return new Item(getType()).setName("&r&f" + this.getName()).setAmount(1).get();
	}
	
	public enum HitType {
		
		MELEE("Melee"), ARCHERY("Archery"), OTHER("Other");
		
		private String name;
		
		HitType(String name){
			this.name = name;
		}
		
		public String getName() {
			return Text.color(name);
		}
		
	}
	
}
