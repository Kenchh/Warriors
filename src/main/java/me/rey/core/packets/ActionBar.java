package me.rey.core.packets;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.rey.core.utils.Text;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

public class ActionBar extends Packets {

	private String text;
	
	public ActionBar(String text) {
		this.text = text;
	}
	
	@Override
	public void send(LivingEntity entity) {
		final IChatBaseComponent cbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + text + "\"}");
        final PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte) 2);
        this.sendPacket((Player) entity, ppoc);
	}
	
	public static ActionBar getChargingBar(String name, double charge, double maxCharge, String... extraText) {
		int bars = 15;
		String barsString = "";
		
		double mult = bars / maxCharge;
		int toAdd = (int) Math.round((maxCharge - charge) * mult) + 1;

		for(int i = 1; i <= bars; i++) {
			barsString += (i <= toAdd ? ChatColor.GREEN : ChatColor.RED) + ChatColor.BOLD.toString() + "▌";
		}
		
		String newText = "&f&l" + name + " " + barsString + " &r";
		for(String s : extraText) newText += s;
		
		return new ActionBar(Text.color(newText));
	}
	
	public static ActionBar getChargingBar(String name, double percentage, String... extraText) {
		int bars = 15;
		String barsString = "";
		
		int toAdd = (int) Math.round(bars * percentage / 100);

		for(int i = 1; i <= bars; i++) {
			barsString += (i <= toAdd ? ChatColor.GREEN : ChatColor.RED) + ChatColor.BOLD.toString() + "▌";
		}
		
		String newText = "&f&l" + name + " " + barsString + " &r";
		for(String s : extraText) newText += s;
		
		return new ActionBar(Text.color(newText));
	}
}
