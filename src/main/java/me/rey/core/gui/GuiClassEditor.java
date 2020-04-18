package me.rey.core.gui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.database.SQLManager;
import me.rey.core.gui.anvil.AnvilGUI;
import me.rey.core.gui.anvil.AnvilGUI.AnvilClickEvent;
import me.rey.core.gui.anvil.AnvilGUI.AnvilSlot;
import me.rey.core.players.User;
import me.rey.core.pvp.Build;
import me.rey.core.utils.Text;

public class GuiClassEditor extends GuiEditable {

	private ClassType classType;
	private Player p;
	private SQLManager sql;
	
	public GuiClassEditor(Warriors plugin, ClassType classType, Player player) {
		super("", 6, plugin);
		
		this.classType = classType;
		this.p = player;
		this.sql = Warriors.getInstance().getSQLManager();
	}
	
	private Player getPlayer() {
		return this.p;
	}
	
	@Override
	public void setup() {
		
		User u = new User(this.getPlayer());
		
		setItem(new GuiItem(new Item(Material.GOLD_BLOCK).setName("&6&lDefault").setGlow(new User(this.getPlayer()).getSelectedBuild(classType) == null)) {
			@Override
			public void onUse(Player player, ClickType type, int slot) {
				if(!getItem(slot).getFromItem().hasGlow()) {
					u.selectBuild(null, classType);
					updateInventory();
				}
			}
		}, 9);
		
		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		
		meta.setOwner(this.getPlayer().getName());
		meta.setDisplayName(Text.color("&8&m---------------"));
		meta.setLore(Arrays.asList(
				Text.color("&r"),
				Text.color(String.format("&7Player: &e%s", this.getPlayer().getDisplayName())),
				Text.color(String.format("&7Loaded Builds: &e%s", u.getBuilds(this.classType).size())),
				Text.color("&r"),
				Text.color("&8&m---------------")
		));
		
		head.setItemMeta(meta);
		
		
		Item emptyBuild = new Item(Material.INK_SACK).setDurability(8).setName("&7Empty Build");
		Item[] buildItems = new Item[] {
		
				new Item(Material.INK_SACK).setDurability(1),
				new Item(Material.INK_SACK).setDurability(11),
				new Item(Material.INK_SACK).setDurability(10),
				new Item(Material.INK_SACK).setDurability(4)
				
		};
		
		
		int max_builds = 4;
		for(int i = 0; i < u.getBuilds(this.classType).size(); i++) {
			
			Build b = u.getBuilds(classType).get(i);
			int position = b.getPosition() <= -1 ? i+1 : b.getPosition();
					
			if(i > (max_builds - 1)) break;
			
			Item item = u.getSelectedBuild(classType) != null && b.getUniqueId().toString().trim().equals(u.getSelectedBuild(classType).getUniqueId().toString().trim())
					? buildItems[position-1].setGlow(true) : buildItems[position-1];
				
			item.addLore("&8&m-------------------").addLore("&r");
			for(AbilityType type : AbilityType.values()) {
				item.addLore("&2" + type.getName() + ": &r" + (b.getAbility(type) == null ? "None" : b.getAbility(type).getName() + " Lvl" + b.getAbilityLevel(type)));
			}
			item.addLore("&r");
			
			setItem(new GuiItem(item.setName("&e" + b.getName())) {
				@Override
				public void onUse(Player player, ClickType type, int slot) {
					if(!item.hasGlow()) {
						u.selectBuild(b, classType);
						
						updateInventory();
					}
				}
				
			}, 2 * (position) + 9);
		}
		
		for(int i = 0; i < max_builds; i++) {
			setItem(new GuiItem(emptyBuild) {
				@Override
				public void onUse(Player player, ClickType type, int slot) {
					if(sql.getPlayerBuilds(player.getUniqueId(), classType).size() == max_builds) return;
				
					int position = (slot - 9) / 2;
					Build def = new Build("", UUID.randomUUID(), position, new HashMap<Ability, Integer>());
					String name = "Build ";
					int number = position;
					
					for(int index = 0; index < max_builds+1; index++) {
						boolean repeated = false;
						for(Build b : u.getBuilds(classType)) {
							if((name + index).equalsIgnoreCase(b.getRawName())) {
								repeated = true;
							}
						}
						if(!repeated) {
							number = number + index;
							break;
						}
					}
					
					def.setName(name + number);
					
					sql.createPlayerBuild(player.getUniqueId(), def, classType);
					u.selectBuild(def, classType);
					
					updateInventory();
				}
			}, 2* (i+1) + 9);
		}
		
		
		setItem(new GuiItem(head) {
			@Override
			public void onUse(Player player, ClickType type, int slot) {
			}
		}, 0);
			
		setItem(new GuiItem(new Item(classType.getHelmet()).setName("&f&l" + classType.getName())) {
			@Override
			public void onUse(Player player, ClickType type, int slot) {
			}
		}, 18);
		
		setItem(new GuiItem(new Item(classType.getChestplate()).setName("&f&l" + classType.getName())) {
			@Override
			public void onUse(Player player, ClickType type, int slot) {
			}
		}, 27);
		
		setItem(new GuiItem(new Item(classType.getLeggings()).setName("&f&l" + classType.getName())) {
			@Override
			public void onUse(Player player, ClickType type, int slot) {
			}
		}, 36);
		
		setItem(new GuiItem(new Item(classType.getBoots()).setName("&f&l" + classType.getName())) {
			@Override
			public void onUse(Player player, ClickType type, int slot) {
				
			}
		}, 45);
		
		setItem(new GuiItem(new Item(Material.BOOK_AND_QUILL).setName("&9Rename")) {
			@Override
			public void onUse(Player player, ClickType type, int slot) {
				
				if(getItem(slot-18) != null) {
					
					for(Build b : new User(player).getBuilds(classType)) {
						String itemName = ChatColor.stripColor(getItem(slot-18).getFromItem().getName());
						
						if(itemName.equals(b.getNameWithoutColors())) {

							AnvilGUI gui = new AnvilGUI(Warriors.getInstance(), u.getPlayer(), new AnvilGUI.AnvilClickEventHandler() {
								
								@Override
								public void onAnvilClick(AnvilClickEvent event) {
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
										
										for(Build query : u.getBuilds(classType)) {
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
					}
					
				}
			}
		}, 29, 31, 33, 35);
		
	}

	@Override
	public void init() {
		
		setItem(new GuiItem(new Item(Material.ANVIL).setName("&7Edit")) {
			@Override
			public void onUse(Player player, ClickType type, int slot) {
				
				if(getItem(slot-9) != null) {
					
					for(Build b : new User(player).getBuilds(classType)) {
						String itemName = ChatColor.stripColor(getItem(slot-9).getFromItem().getName());
						
						if(itemName.equals(b.getNameWithoutColors())) {
							
							GuiClassAbilitiesEditor editor = new GuiClassAbilitiesEditor(player, classType, b);
							editor.setup();
							editor.open(player);
							
						}
						
					}
				}
			}
		}, 20, 22, 24, 26);
		
		
		setItem(new GuiItem(new Item(Material.REDSTONE_BLOCK).setName("&c&lDELETE")) {
			@Override
			public void onUse(Player player, ClickType type, int slot) {
				
				if(getItem(slot-27) != null) {
					
					for(Build b : new User(player).getBuilds(classType)) {
						String itemName= ChatColor.stripColor(getItem(slot-27).getFromItem().getName());
						
						if(itemName.equals(b.getNameWithoutColors())) {
							
							sql.deletePlayerBuild(player.getUniqueId(), b, classType);
							
							updateInventory();
							break;
						}
					}
					
				}
				
			}
		}, 38, 40, 42, 44);
		
	}
	
	

}
