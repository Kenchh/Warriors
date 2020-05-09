package me.rey.core.packets;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public abstract class Packets {
	
	Class<?> PACKET = getNMSClass("Packet");
	Class<?> CHATCOMPONENT = getNMSClass("IChatBaseComponent");
	Class<?> TITLEPACKET = getNMSClass("PacketPlayOutTitle");
	
	public abstract void send(LivingEntity entity);
	
	public void sendPacket(Player player, Object packet) {
		try {
			Object handle = player.getClass().getMethod("getHandle").invoke(player);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			playerConnection.getClass().getMethod("sendPacket", PACKET).invoke(playerConnection, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Class<?> getNMSClass(String name){
		try {
			return Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + name);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

}
