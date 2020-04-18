package me.rey.core.events;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class UseSoupEvent implements Listener {
	
	double seconds = 3.5;
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if(e.getItem() == null || e.getItem().getType().equals(Material.AIR)) return;
		ItemStack item = e.getItem();
		
		if(!item.getType().equals(Material.MUSHROOM_SOUP)) return;
		Player p = e.getPlayer();
		
		this.clearEffect(PotionEffectType.REGENERATION, p);
		item.setAmount(item.getAmount()-1);
		p.setItemInHand(item);
		p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, (int) Math.floor(seconds * 20), 1));
		p.getWorld().spigot().playEffect(p.getEyeLocation(), Effect.STEP_SOUND, 0, 282, 0, 0, 0, 0, 1, 100);
	}
	
	private void clearEffect(PotionEffectType e, Player p) {
		for(PotionEffect pe : p.getActivePotionEffects()) {
			if(pe.getType().equals(e)) {
				p.removePotionEffect(e);
				return;
			}
		}
	}

}
