package me.rey.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.rey.core.classes.ClassType;
import me.rey.core.utils.Text;
import me.rey.core.utils.Utils;

public class Equip implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		
		if((sender instanceof Player) && command.getName().equalsIgnoreCase("equip")) {
			
			Player p = (Player) sender;
			if(!p.isOp()) {
				p.sendMessage(Text.color("&cOnly operators can do that!"));
				return true;
			}
			
			if(args.length != 1) {
				p.sendMessage(Text.color("&cUsage: /equip <class>"));
				return true;
			}
			
			String name = args[0];
			ClassType classType = null;
			String allNames = Text.color("&cValid classes: ");
			for(ClassType type : ClassType.values()) {
				allNames += ", " + type.getName();
				if(type.getName().equalsIgnoreCase(name))
					classType = type;
			}
			
			if(classType == null) {
				p.sendMessage(allNames.replaceFirst(", ", ""));
				return true;
			}
			
			ItemStack helmet = Utils.updateItem(classType.getHelmet().get()), chestplate = Utils.updateItem(classType.getChestplate().get());
			ItemStack boots = Utils.updateItem(classType.getBoots().get()), leggings = Utils.updateItem(classType.getLeggings().get());
			
			p.getInventory().setHelmet(helmet);
			p.getInventory().setChestplate(chestplate);
			p.getInventory().setLeggings(leggings);
			p.getInventory().setBoots(boots);
			
			p.sendMessage(Text.color("&aEquipped: &6" + classType.getName()));
		}
		
		
		return true;
	}

}
