package me.rey.core.events;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import me.rey.core.Warriors;
import me.rey.core.gui.GuiClassEditor;
import me.rey.core.players.User;

public class ClassEditorClickEvent implements Listener {
	
	private GuiClassEditor guiClassEditor;
	
	@EventHandler
	public void onOpenClassPicker(PlayerInteractEvent event) {
		
		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
		
			if(event.getClickedBlock().getType().equals(Material.ENCHANTMENT_TABLE)) {
				
				event.setCancelled(true);
			
				User user = new User(event.getPlayer());
				if(user.getWearingClass() != null) {
					this.guiClassEditor = new GuiClassEditor(Warriors.getInstance(), user.getWearingClass(), user.getPlayer());
					this.guiClassEditor.setup();
					this.guiClassEditor.open(event.getPlayer());
				} else {
					user.sendMessageWithPrefix("Selector", "&cYou do not have a class equipped!");
				}
				
			}	
		}
	}
}
