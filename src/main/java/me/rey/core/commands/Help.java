package me.rey.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.rey.core.Warriors;

public class Help implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		
		if((sender instanceof Player) && command.getName().equalsIgnoreCase("help")) {
		
			Player p = (Player) sender;
			Warriors.guiHelp.open(p);
			
		}
		
		
		return true;
	}
	
}
