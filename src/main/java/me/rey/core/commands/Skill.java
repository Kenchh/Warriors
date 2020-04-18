package me.rey.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.rey.core.players.User;

public class Skill implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		
		if((sender instanceof Player) && command.getName().equalsIgnoreCase("skill")) {
			
			Player p = (Player) sender;
			new User(p).sendListingClassSkills(new User(p).getWearingClass());
			
		}
		
		
		return true;
	}

}
