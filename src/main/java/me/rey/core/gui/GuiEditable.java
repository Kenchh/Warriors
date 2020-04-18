package me.rey.core.gui;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.Plugin;

public abstract class GuiEditable extends Gui {

	public GuiEditable(String name, int rows, Plugin plugin) {
		super(name, rows, plugin);
	}
	
	public GuiEditable(InventoryType type, String name, int rows, Plugin plugin) {
		super(type, name, rows, plugin);
	}
	
	public abstract void setup();
	
	public abstract void init();
	
	public void updateInventory() {
		
		for(int i = 0; i < this.getSize(); i++) {
			this.removeItem(i);
		}
		
		this.setup();
		this.init();
		
	}

}
