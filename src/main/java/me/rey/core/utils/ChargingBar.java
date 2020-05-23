package me.rey.core.utils;

import net.md_5.bungee.api.ChatColor;

public class ChargingBar {
	
	public static final int TITLE_BARS = 20;
	public static final int ACTIONBAR_BARS = 15;
	
	char barChar;
	int maxBars, chargeBars;
	
	public ChargingBar(int maxBars, double charge, double maxCharge) {
		this.maxBars = maxBars;
		this.chargeBars = Math.max(0, Math.min(maxBars, (int) Math.round((maxCharge - charge) * maxBars / maxCharge) + 1));
		
		this.barChar = '▌';
	}
	
	public ChargingBar(int maxBars, double percentage) {
		this.maxBars = maxBars;
		this.chargeBars = Math.max(0, Math.min(maxBars, (int) Math.round(maxBars * percentage / 100)));
		
		this.barChar = '▌';
	}
	
	public char getChar() {
		return barChar;
	}
	
	public ChargingBar setChar(char barChar) {
		this.barChar = barChar;
		return this;
	}
	
	public int getChargeBars() {
		return chargeBars;
	}
	
	public int getMaxBars() {
		return maxBars;
	}
	
	public ChargingBar setMaxBars(int maxBars) {
		this.maxBars = maxBars;
		return this;
	}
	
	public ChargingBar setChargeBars(int chargeBars) {
		this.chargeBars = chargeBars;
		return this;
	}
	
	public int getCharged() {
		return chargeBars;
	}
	
	public String getBarString() {
		String toSend = "";
		
		for(int i = 1; i <= maxBars; i++) toSend += (i <= chargeBars ? ChatColor.GREEN : ChatColor.RED) + ChatColor.BOLD.toString() + this.barChar;
		
		return toSend;
	}

}
