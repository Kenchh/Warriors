package me.rey.core.utils;

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
		return ChatColor.translateAlternateColorCodes('&', text.replaceAll("&s", "&e").replaceAll("&r", "&7").replaceAll("&q", "&c&l").replaceAll("&w", "&a&l"));
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
    
    public static double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }
    
    public static String formatName(String text) {
		String[] name = text.replaceAll("_", " ").toLowerCase().split(" ");
		StringBuilder message = new StringBuilder();
		for(String s : name) {
			s = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
			message.append(s).append(" ");
		}
		return message.toString().trim();
    }
    

}
