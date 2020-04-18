package me.rey.core.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;

import me.rey.core.commands.CommandType;
import me.rey.core.players.User;
import me.rey.core.utils.References;

public class GuiHelp extends Gui {

	public GuiHelp(Plugin plugin) {
		super("", 6, plugin);
	}

	@Override
	public void init() {
        
		String link = "Link: &e";
		
        setItem(new GuiItem(new Item(Material.NETHER_STAR).setName("&aInformation")
        		.setDefaultLore("&7Welcome to the main help GUI!", "&7Here you will find a big list of", "&7stuff you can do.")) {
            @Override
            public void onUse(Player player, ClickType type, int slot) {
            	// Nothing.
            }
        }, 19);
        
        
        setItem(new GuiItem(new Item(Material.BEACON).setName("&cWebsite")
        		.setDefaultLore("&7You will be prompted to open a", "&7link to open our online &e&lwebsite&7.")) {
            @Override
            public void onUse(Player player, ClickType type, int slot) {
                new User(player).sendMessageWithPrefix(CommandType.HELP, link + References.WEBSITE);
                player.closeInventory();
            }
        }, 22);
        

        setItem(new GuiItem(new Item(Material.DIAMOND).setName("&bOnline Store")
        		.setDefaultLore("&7You will be prompted to open a", "&7link to open our online &3&lstore&7.")) {
            @Override
            public void onUse(Player player, ClickType type, int slot) {
                new User(player).sendMessageWithPrefix(CommandType.HELP, link + References.STORE);
                player.closeInventory();
            }
        }, 23);
        
        
        setItem(new GuiItem(new Item(Material.REDSTONE).setName("&7&oComing soon...")) {
            @Override
            public void onUse(Player player, ClickType type, int slot) {
                // NOTHING
            }
        }, 24);
        

        setItem(new GuiItem(new Item(Material.REDSTONE_BLOCK).setName("&c&lRETURN TO HUB")) {
            @Override
            public void onUse(Player player, ClickType type, int slot) {
                player.performCommand("hub");
                player.closeInventory();
            }
        }, 28);
        
        
        setItem(new GuiItem(new Item(Material.EYE_OF_ENDER).setName("&9Discord")
        		.setDefaultLore("&7You will be prompted to open a", "&7link to join our &9&ldiscord &7server.")) {
            @Override
            public void onUse(Player player, ClickType type, int slot) {
                new User(player).sendMessageWithPrefix(CommandType.HELP, link + References.DISCORD_INVITE);
                player.closeInventory();
            }
        }, 31);
        
        
        setItem(new GuiItem(new Item(Material.IRON_SWORD).setName("&6About Warriros")
        		.setDefaultLore("&7You will be prompted to open a", "&7link to view the online page for", "&7our &f&ldocumentation&7.")) {
            @Override
            public void onUse(Player player, ClickType type, int slot) {
                new User(player).sendMessageWithPrefix(CommandType.HELP, link + References.ABOUT_WARRIORS);
                player.closeInventory();
            }
        }, 32);
        
        
        setItem(new GuiItem(new Item(Material.REDSTONE).setName("&7&oComing soon...")) {
            @Override
            public void onUse(Player player, ClickType type, int slot) {
                // Nothing
            }
        }, 33);
        
        fillEmptySlots(new GuiItem(new Item(Material.STAINED_GLASS_PANE).setDurability(7).setName("&r")) {
			@Override
			public void onUse(Player player, ClickType type, int slot) {
				// Nothing
			}
        });
        
	}

}
