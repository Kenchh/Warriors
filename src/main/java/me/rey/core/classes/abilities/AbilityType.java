package me.rey.core.classes.abilities;

import org.bukkit.Material;

import me.rey.core.gui.Gui.Item;
import me.rey.core.pvp.ToolType;
import me.rey.core.utils.Text;

public enum AbilityType {
	
	SWORD(true, EventType.RIGHT_CLICK, new Item(ToolType.STANDARD_SWORD.getType()), ToolType.POWER_SWORD, ToolType.STANDARD_SWORD, ToolType.BOOSTER_SWORD),
	AXE(true, EventType.RIGHT_CLICK, new Item(ToolType.STANDARD_AXE.getType()), ToolType.POWER_AXE, ToolType.STANDARD_AXE, ToolType.BOOSTER_AXE),
	SPADE(true, EventType.RIGHT_CLICK, new Item(ToolType.STANDARD_SPADE.getType()), ToolType.POWER_SPADE, ToolType.STANDARD_SPADE, ToolType.BOOSTER_SPADE),
	BOW(EventType.LEFT_CLICK, new Item(ToolType.STANDARD_BOW.getType()), ToolType.STANDARD_BOW),
	PASSIVE_A(EventType.NONE, new Item(Material.INK_SACK).setDurability(1),ToolType.POWER_SWORD, ToolType.POWER_AXE, ToolType.STANDARD_SWORD, ToolType.STANDARD_AXE, ToolType.BOOSTER_SWORD, ToolType.BOOSTER_AXE),
	PASSIVE_B(EventType.NONE, new Item(Material.INK_SACK).setDurability(14), ToolType.POWER_SWORD, ToolType.POWER_AXE, ToolType.STANDARD_SWORD, ToolType.STANDARD_AXE, ToolType.BOOSTER_SWORD, ToolType.BOOSTER_AXE),
	PASSIVE_C(EventType.NONE, new Item(Material.INK_SACK).setDurability(11));
	
	private ToolType[] toolType;
	private EventType eventType;
	private Item icon;
	private boolean supportsBoosters;
	
	AbilityType(boolean supportsBoosters, EventType eventType, Item icon, ToolType... type){
		this.supportsBoosters = supportsBoosters;
		this.toolType = type;
		this.eventType = eventType;
		this.icon = icon;
	}
	
	AbilityType(EventType eventType, Item icon, ToolType... type){
		this.supportsBoosters = false;
		this.toolType = type;
		this.eventType = eventType;
		this.icon = icon;
	}
	
	public boolean supportsBoosters() {
		return supportsBoosters;
	}
	
	public ToolType[] getToolTypes() {
		return this.toolType == null ? new ToolType[]{} : this.toolType;
	}
	
	public Item getIcon() {
		return this.icon;
	}
	
	public EventType getEventType() {
		return eventType == null ? EventType.NONE : eventType;
	}
	
	public String getName() {
		return Text.format(this.name());
	}

	public enum EventType {
		
		NONE,
		RIGHT_CLICK,
		LEFT_CLICK;
		
	}

}
