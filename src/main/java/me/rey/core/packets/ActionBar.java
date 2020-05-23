package me.rey.core.packets;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.rey.core.utils.ChargingBar;
import me.rey.core.utils.Text;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

public class ActionBar extends Packets {

	private String text;
	public static int maxChargeBars = 15;
	
	public ActionBar(String text) {
		this.text = text;
	}
	
	@Override
	public void send(LivingEntity entity) {
		final IChatBaseComponent cbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + text + "\"}");
        final PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte) 2);
        this.sendPacket((Player) entity, ppoc);
	}
	
	public static ActionBar getChargingBar(String name, ChargingBar bar, String... extraText) {
		
		String newText = (name.equals("") ? "": "&f&l" + name + " ") + bar.getBarString() + " &r";
		for(String s : extraText) newText += s;
		
		return new ActionBar(Text.color(newText));
	}
	
}
