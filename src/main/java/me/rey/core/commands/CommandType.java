package me.rey.core.commands;

public enum CommandType {
	
	HELP("Help");
	
	private String identifier;
	
	CommandType(String identifier) {
		this.identifier = identifier;
	}
	
	public String getName() {
		return this.identifier;
	}
}
