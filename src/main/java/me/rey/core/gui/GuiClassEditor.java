package me.rey.core.gui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.common.primitives.Ints;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.database.SQLManager;
import me.rey.core.players.User;
import me.rey.core.pvp.Build;
import me.rey.core.utils.Text;

public class GuiClassEditor extends GuiEditable {

	private ClassType classType;
	private Player p;
	private SQLManager sql;
	
	private final int[] buildPositions = {2, 4, 6, 8, 30, 32, 34};
	
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
		
		int column = 2;
		for(int i = 0; i < this.getRows(); i++) {
			
			setItem(new GuiItem(new Item(Material.STAINED_GLASS_PANE).setDurability(15).setName("&r")) {
				@Override
				public void onUse(Player player, ClickType type, int slot) {
					// ignore
					
				}
			}, column - 1 + (9 * i));
		}
		
		setItem(new GuiItem(new Item(Material.QUARTZ).setName("&6&lDefault").setGlow(new User(this.getPlayer()).getSelectedBuild(classType) == null)) {
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
				Text.color(String.format("&7Loaded Builds: &e%s", this.sql.getPlayerBuilds(u.getUniqueId(), classType).size())),
				Text.color("&r"),
				Text.color("&8&m---------------")
		));
		
		head.setItemMeta(meta);
		
		
		Item emptyBuild = new Item(Material.INK_SACK).setDurability(8).setName("&7Empty Build");
		Item[] buildItems = new Item[] {
		
				new Item(Material.INK_SACK).setDurability(1),
				new Item(Material.INK_SACK).setDurability(11),
				new Item(Material.INK_SACK).setDurability(14),
				new Item(Material.INK_SACK).setDurability(10),
				new Item(Material.INK_SACK).setDurability(12),
				new Item(Material.INK_SACK).setDurability(6),
				new Item(Material.INK_SACK).setDurability(5)
				
		};
		

		for(int i = 0; i < this.sql.getPlayerBuilds(u.getUniqueId(), classType).size(); i++) {
			
			Build b = this.sql.getPlayerBuilds(u.getUniqueId(), classType).get(i);
			int position = b.getPosition() <= -1 ? i+1 : b.getPosition();
					
			if(i > buildPositions.length - 1) break;
			
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
				
			}, buildPositions[position-1]);
			
			this.setEditItem(u, b, buildPositions[position-1]+9, buildPositions[position-1]);
			this.setOptionsItem(u, b, buildPositions[position-1]+9*2);
		}
		
		for(int i = 0; i < buildPositions.length; i++) {
			
			this.setEditItem(u, null, buildPositions[i]+9, buildPositions[i]);
			this.setOptionsItem(u, null, buildPositions[i]+9*2);
			
			setItem(new GuiItem(emptyBuild) {
				@Override
				public void onUse(Player player, ClickType type, int slot) {
					createBuild(player, slot);
				}
			}, buildPositions[i]);
			
		}
		
		
		setItem(new GuiItem(head) {
			@Override
			public void onUse(Player player, ClickType type, int slot) {
			}
		}, 0);
			
		setItem(new GuiItem(classType.getHelmet().setName("&f&l" + classType.getName())) {
			@Override
			public void onUse(Player player, ClickType type, int slot) {
			}
		}, 18);
		
		setItem(new GuiItem(classType.getChestplate().setName("&f&l" + classType.getName())) {
			@Override
			public void onUse(Player player, ClickType type, int slot) {
			}
		}, 27);
		
		setItem(new GuiItem(classType.getLeggings().setName("&f&l" + classType.getName())) {
			@Override
			public void onUse(Player player, ClickType type, int slot) {
			}
		}, 36);
		
		setItem(new GuiItem(classType.getBoots().setName("&f&l" + classType.getName())) {
			@Override
			public void onUse(Player player, ClickType type, int slot) {
				
			}
		}, 45);
		
	}

	@Override
	public void init() {
		// IGNORE
	}
	
	private Build createBuild(Player player, int slot) {
		User u = new User(player);
		if(sql.getPlayerBuilds(player.getUniqueId(), classType).size() >= buildPositions.length) return u.getSelectedBuild(classType);
		
		int position = Ints.indexOf(buildPositions, slot) + 1;
		Build def = new Build("", UUID.randomUUID(), position, new HashMap<Ability, Integer>());
		String name = "Build ";
		int number = position;
		
		for(int index = 0; index <= buildPositions.length; index++) {
			boolean repeated = false;
			for(Build b : sql.getPlayerBuilds(u.getUniqueId(), classType).getArray()) {
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
		return u.getSelectedBuild(classType);
	}
	
	private void setOptionsItem(User u, Build b, int slot) {
		setItem(new GuiItem(new Item(Material.BOOK_AND_QUILL).setName("&7Options")) {
			@Override
			public void onUse(Player player, ClickType type, int slot) {
				
				if(b != null) {
					GuiBuildOptions gui = new GuiBuildOptions(u.getPlayer(), b, classType);
					gui.setup();
					gui.open(u.getPlayer());
				}
							
			}
		}, slot);
	}
	
	private void setEditItem(User u, Build b, int slot, int buildSlot) {
		
		setItem(new GuiItem(new Item(Material.ANVIL).setName("&7Edit: &e" + (b == null ? "None" : b.getName()))) {
			@Override
			public void onUse(Player player, ClickType type, int slot) {
				
				Build toEdit = b;
				if(toEdit == null) toEdit = createBuild(player, buildSlot);
						
				GuiClassAbilitiesEditor editor = new GuiClassAbilitiesEditor(player, classType, toEdit);
				editor.setup();
				editor.open(u.getPlayer());
							
			}
		}, slot);
	}
	
	

}
