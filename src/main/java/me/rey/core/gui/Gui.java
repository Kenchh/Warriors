package me.rey.core.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;

import me.rey.core.items.Glow;
import net.md_5.bungee.api.ChatColor;

public abstract class Gui implements Listener {
	
	Inventory inventory;
	
	private String name;
	private int rows, size;
	private Plugin plugin;
    private Map<Integer, GuiItem> events = new HashMap<Integer, GuiItem>();
    private InventoryType type;
	
	public Gui(String name, int rows, Plugin plugin) {
		this.name = name;
		this.rows = rows;
		this.size = rows*9;
		this.plugin = plugin;
		this.create();
	}
	
	public Gui(InventoryType type, String name, int rows, Plugin plugin) {
		this.type = type;
		this.name = name;
		this.rows = rows;
		this.size = rows*9-1;
		this.plugin = plugin;
		this.create();
	}
	
	private void create() {
		inventory = Bukkit.createInventory(null, 9*rows, ChatColor.translateAlternateColorCodes('&', name));
		if(type != null) inventory = Bukkit.createInventory(null, type);
		
		Bukkit.getPluginManager().registerEvents(this, plugin);
		
		init();
	}
	
	
	public abstract void init();
	
	protected void addItem(GuiItem guiItem) {
		if(this.getInventory().firstEmpty() == -1) return;
		setItem(guiItem, this.getInventory().firstEmpty());
	}
	
	protected void removeItem(int slot, int... slots) {
		for(int query : slots) {
			if(!events.containsKey(query)) continue;
			events.remove(query);
			this.inventory.setItem(query, new ItemStack(Material.AIR));
		}
		
		if(events.containsKey(slot)) {
			events.remove(slot);
			this.inventory.setItem(slot, new ItemStack(Material.AIR));
		}
	}
	
	protected void setItem(GuiItem item, int slot, int... slots) {
		for(int query : slots) {
			if(events.containsKey(query)) continue;
			this.events.put(query, item);
			this.inventory.setItem(query, item.get());
		}
		
		if(events.containsKey(slot)) return;
		this.events.put(slot, item);
		this.inventory.setItem(slot, item.get());
		
	}
	
	protected void fillEmptySlots(GuiItem guiItem) {
		for(int i = 0; i < this.getInventory().getSize() - 1; i++) {
			this.addItem(guiItem);
		}
	}
	
	public void open(Player player) {
		player.openInventory(this.inventory);
	}

	public Inventory getInventory() {
		return inventory;
	}

	public String getName() {
		return name;
	}

	public int getRows() {
		return rows;
	}

	public int getSize() {
		return size;
	}
	
	public GuiItem getItem(int slot) {
		if(events.containsKey(slot)) {
			return events.get(slot);
		}
		return null;
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();

	
		if(e.getInventory().equals(this.inventory)) {
			e.setCancelled(true);
			if(!this.events.containsKey(e.getSlot())) return;
			
			if(e.getClickedInventory() == null) return;
	        if(e.getClickedInventory().getItem(e.getSlot()) == null) return;
	        if(e.getView().getBottomInventory() == null) return;
	        if((e.getClickedInventory().getHolder() instanceof Player)) return;
			
			GuiItem item = this.events.get(e.getSlot());
			item.onUse(player, e.getClick(), e.getSlot());	
		}
	}
	
	public static class Item {
	     
		private boolean glow;
        private Material material;
        private int data;
        private int amount = 1;
        private int durability = 0;
        private String name;
        private List<String> lore;
        private ItemStack item;
        private Map<Enchantment, Integer> enchantements;

     
        public Item(Material material){
            this.material = material;
            this.lore = new ArrayList<>();
            this.enchantements = new HashMap<Enchantment, Integer>();
            this.glow = false;
        }
     
        public Item setName(String name){
            this.name = ChatColor.translateAlternateColorCodes('&', name);
            return this;
        }
     
        public Item setLore(List<String> lore){
        	List<String> newLore = new ArrayList<String>();
        	for(String line : lore) {
        		newLore.add(ChatColor.translateAlternateColorCodes('&', line));
        	}
            this.lore = newLore;
            return this;
        }
     
        public Item addLore(String lore){
            this.lore.add(ChatColor.translateAlternateColorCodes('&', lore));
            return this;
        }
        
        public Item setDefaultLore(String... lore) {
        	this.addLore("&8&m-------------------");
        	this.addLore("&r");
        	for(String query : lore) {
        		this.addLore(query);
        	}
        	this.addLore("&r");
        	this.addLore("&8&m-------------------");
        	return this;
        }
     
        public Item setAmount(int amount){
            this.amount = amount;
            return this;
        }
        
        public Item setDurability(int durability) {
        	this.durability = durability;
        	return this;
        }
        
        public Item setGlow(boolean glow) {
        	this.glow = glow;
        	return this;
        }
        
        public boolean hasGlow() {
        	return this.get().containsEnchantment(new Glow(255));
        }
     
        public Item addEnchantment(Enchantment enchantment, int level){
            if(this.enchantements.containsKey(enchantment))this.enchantements.remove(enchantment);
            this.enchantements.put(enchantment, level);
            return this;
        }
        
        public String getName() {
        	return name != null ? name : "";
        }
     
        @SuppressWarnings("deprecation")
        public ItemStack get(){
            ItemStack item = new ItemStack(material, amount, (byte) durability);
            if(this.item != null)item = this.item.clone();
            item.setData(new MaterialData(material, (byte)data));
            ItemMeta meta = item.getItemMeta();
            if(name != null)meta.setDisplayName(name);
            if(lore != null && !lore.isEmpty())meta.setLore(lore);
            if(glow) {
            	Glow glow = new Glow(255);
            	meta.addEnchant(glow, 1, true);
            }
            if(enchantements != null && !enchantements.isEmpty()){
                for(Enchantment enchant : enchantements.keySet()){
                    item.addEnchantment(enchant, enchantements.get(enchant));
                }
            }
            item.setItemMeta(meta);
            return item;
        }
     
    }
 
    public static abstract class GuiItem {
     
        private ItemStack itemStack;
        private Item item;
     
        
        public GuiItem(Item item){
            this.itemStack = item.get();
            this.item = item;
        }
        
        public GuiItem(ItemStack item) {
            this.itemStack = item;
            this.item = new Item(item.getType());
        }
     
        
        public ItemStack get() {
            return itemStack;
        }
        
        public Item getFromItem() {
        	return item;
        }
     
        
        public abstract void onUse(Player player, ClickType type, int slot);
     
    }
	
	
}
