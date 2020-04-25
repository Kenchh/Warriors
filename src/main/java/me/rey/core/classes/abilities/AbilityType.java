package me.rey.core.classes.abilities;

import org.bukkit.Material;

import me.rey.core.gui.Gui.Item;
import me.rey.core.pvp.ToolType;
import me.rey.core.utils.Text;

public enum AbilityType {
	
	SWORD(EventType.RIGHT_CLICK, new Item(ToolType.STANDARD_SWORD.getType()), ToolType.POWER_SWORD, ToolType.STANDARD_SWORD, ToolType.BOOSTER_SWORD),
	AXE(EventType.RIGHT_CLICK, new Item(ToolType.STANDARD_AXE.getType()), ToolType.POWER_AXE, ToolType.STANDARD_AXE, ToolType.BOOSTER_AXE),
	BOW(EventType.LEFT_CLICK, new Item(ToolType.STANDARD_BOW.getType()), ToolType.STANDARD_BOW),
	PASSIVE_A(EventType.CONSTANT, new Item(Material.INK_SACK).setDurability(1),ToolType.POWER_SWORD, ToolType.POWER_AXE, ToolType.STANDARD_SWORD, ToolType.STANDARD_AXE, ToolType.BOOSTER_SWORD, ToolType.BOOSTER_AXE),
	PASSIVE_B(EventType.CONSTANT, new Item(Material.INK_SACK).setDurability(14), ToolType.POWER_SWORD, ToolType.POWER_AXE, ToolType.STANDARD_SWORD, ToolType.STANDARD_AXE, ToolType.BOOSTER_SWORD, ToolType.BOOSTER_AXE),
	PASSIVE_C(EventType.CONSTANT, new Item(Material.INK_SACK).setDurability(11));
	
	private ToolType[] toolType;
	private EventType eventType;
	private Item icon;
	
	AbilityType(EventType eventType, Item icon, ToolType... type){
		this.toolType = type;
		this.eventType = eventType;
		this.icon = icon;
	}
	
	public ToolType[] getToolTypes() {
		return this.toolType == null ? new ToolType[]{} : this.toolType;
	}

	public EventType getEventType() {
		return this.eventType;
	}
	
	public Item getIcon() {
		return this.icon;
	}
	
	public String getName() {
		return Text.format(this.name());
	}
	
	public enum EventType {
		
		DROP_ITEM(),
		RIGHT_CLICK(),
		LEFT_CLICK(),
		CONSTANT();

	}


}
