package me.rey.core.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.database.SQLManager;
import me.rey.core.gui.anvil.AnvilGUI;
import me.rey.core.gui.anvil.AnvilGUI.AnvilClickEvent;
import me.rey.core.gui.anvil.AnvilGUI.AnvilSlot;
import me.rey.core.players.User;
import me.rey.core.pvp.Build;
import me.rey.core.utils.Text;

public class GuiBuildOptions extends GuiEditable {
	
	private Player player;
	private Build b;
	private ClassType classType;
	private SQLManager sql;
	
	public GuiBuildOptions(Player p, Build build, ClassType classType) {
		super("&8Editing: &4" + build.getName(), 3, Warriors.getInstance());
		
		this.player = p;
		this.b = build;
		this.classType = classType;
		this.sql = Warriors.getInstance().getSQLManager();
	}

	@Override
	public void setup() {
		
		User u = new User(player);
		
		/*
		 * RENAME
		 */
		setItem(new GuiItem(new Item(Material.NAME_TAG).setName("&e&lRENAME")) {
			@Override
			public void onUse(Player player, ClickType type, int slot) {
				
				AnvilGUI gui = new AnvilGUI(Warriors.getInstance(), u.getPlayer(), new AnvilGUI.AnvilClickEventHandler() {
					
					@Override
					public void onAnvilClick(AnvilClickEvent event) {
						if(b == null) return;
						event.setWillClose(event.getSlot() == AnvilSlot.OUTPUT);
						event.setWillDestroy(event.getSlot() == AnvilSlot.OUTPUT);
						
						if(event.getSlot() == AnvilSlot.OUTPUT && event.getName() != null && event.getName().trim() != "") {
							
							if(ChatColor.stripColor(Text.color(event.getName().toString())).length() > 10) {
								u.sendMessageWithPrefix("Build", "Invalid name!");
								return;
							}
							
							if(event.getName().trim().equals(b.getRawName().trim())) {
								u.sendMessageWithPrefix("Build", "Invalid name!");
								return;
							}
							
							for(Build query : sql.getPlayerBuilds(u.getUniqueId(), classType)) {
								if(query.getNameWithoutColors().trim().equals(ChatColor.stripColor(Text.color(event.getName().toString())))){
									u.sendMessageWithPrefix("Build", "Invalid name!");
									return;
								}
							}
							
							if(ChatColor.stripColor(Text.color(event.getName().toString())).startsWith("Build ")) {
								u.sendMessageWithPrefix("Build", "Invalid name!");
								return;
							}
							
							String oldName = b.getName();
							Build newBuild = b;
							newBuild.setName(event.getName());
							u.sendMessageWithPrefix("Build", "Sucessfully renamed &e" + oldName + " &7to &e" + newBuild.getName() + "&7!");
							u.editBuild(b, newBuild, classType);
						}
					}
					
				});
				
				Item text = new Item(Material.BOOK_AND_QUILL).setName(b.getName());
				gui.setSlot(AnvilSlot.INPUT_LEFT, text.get());
				
				try {
					gui.open();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}, 11);
		
		/*
		 * DELETE
		 */
		setItem(new GuiItem(new Item(Material.STAINED_CLAY).setDurability(14).setName("&c&lDELETE")) {
			@Override
			public void onUse(Player player, ClickType type, int slot) {
				
				u.sendMessageWithPrefix("Build", "Sucessfully deleted &e" + b.getName() + "&7.");
				sql.deletePlayerBuild(player.getUniqueId(), b, classType);
				player.closeInventory();
				
			}
		}, 15);
		
		/*
		 * EMPTY SLOTS
		 */
		this.fillEmptySlots(new GuiItem(new Item(Material.STAINED_GLASS_PANE).setDurability(15).setName("&r")) {
			@Override
			public void onUse(Player player, ClickType type, int slot) {
				// IGNORE
			}
		});
	}

	@Override
	public void init() {
		// ignore
	}
	
}
