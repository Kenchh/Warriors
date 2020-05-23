package me.rey.core.packets;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.rey.core.utils.ChargingBar;
import me.rey.core.utils.Text;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;

public class Title extends Packets {
	
	public static int maxChargeBars = 20;

	private String title, subtitle;
	private int fadeIn, stay, fadeOut;
	
	public Title(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		this.subtitle = subtitle;
		this.title = title == null || title.equals("") ? Text.color("&r") : title;
		this.fadeIn = fadeIn;
		this.stay = stay;
		this.fadeOut = fadeOut;
	}
	
	@Override
	public void send(LivingEntity entity) {
		
		PacketPlayOutTitle time = new PacketPlayOutTitle(EnumTitleAction.TIMES, ChatSerializer.a("{\"text\":\"" + subtitle + "\"}"), fadeIn, stay, fadeOut);
		((CraftPlayer) entity).getHandle().playerConnection.sendPacket(time);
		
		if(title != "") {
			PacketPlayOutTitle packet = new PacketPlayOutTitle(EnumTitleAction.TITLE, 
					ChatSerializer.a("{\"text\":\"" + title + "\"}"));
			
			this.sendPacket((Player) entity, packet);
		}
		
		if(subtitle != "") {
			PacketPlayOutTitle packet = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, 
					ChatSerializer.a("{\"text\":\"" + subtitle + "\"}"));
			
			this.sendPacket((Player) entity, packet);
		}
	}
	
	public static Title getChargingBar(String title, ChargingBar bar) {
		return new Title(Text.color(title), bar.setChar('|').getBarString(), 0, 4, 0);
	}

}
