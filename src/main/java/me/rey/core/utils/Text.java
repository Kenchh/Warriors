package me.rey.core.utils;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.ChatColor;

public class Text {
	
	public static String format(String prefix, String message) {
		prefix = ChatColor.translateAlternateColorCodes('&', String.format("&9%s »&r", prefix));
		message = ChatColor.translateAlternateColorCodes('&', "&7" + message);
		return (prefix + " " + message);
	}
	
	public static String color(String text) {
		return ChatColor.translateAlternateColorCodes('&', text);
	}
	
    public static void log(Plugin plugin, String msg) {
        msg = ChatColor.translateAlternateColorCodes('&', "&d[" + plugin.getName() + "]&r " + msg);
        Bukkit.getConsoleSender().sendMessage(msg);
    }

    public static void debug(Plugin plugin, String msg) {
        log(plugin, "&7[&eDEBUG&7]&r" + msg);
    }
    
    public static String format(String text) {
		String[] name = text.replaceAll("_", " ").toLowerCase().split(" ");
		StringBuilder message = new StringBuilder();
		for(String s : name) {
			s = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
			message.append(s).append(" ");
		}
		return message.toString().trim();
    }
    
    public static boolean hasColor(String text) {
    	return !text.equals(ChatColor.stripColor(text));
    }
    
    public static boolean isInteger(String s) {
    	try {
    		Integer.parseInt(s);
    		return true;
    	} catch (Exception e) {
    		return false;
    	}
    }
    
    public static String calc(String s) {
    	String[] parts = s.split("(?=[/*+-])|(?<=[/*+-])");
		
		double result = Double.parseDouble(parts[0]);
		for(int z=1; z < parts.length; z += 2) {
			String op = parts[z];
			double val = Double.parseDouble(parts[z+1]);
			switch(op) {
			case "*":
				result *= val;
				break;
			case "/":
				result /= val;
				break;
			case "+":
				result += val;
				break;
			case "-":
				result -= val;
				break;
			}
		}
		
		DecimalFormat dF = new DecimalFormat("0.#");
		return dF.format(Double.valueOf(result + ""));
    }

}
