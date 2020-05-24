package me.rey.core.items.custom;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.rey.core.items.Consumable;

public class MushroomSoup extends Consumable {
	
	private final double regenSeconds = 4;

	public MushroomSoup() {
		super(700, "Mushroom Soup", Material.MUSHROOM_SOUP, true);
	}

	@Override
	protected boolean ConsumeItem(Player p) {
		int hunger = p.getFoodLevel() + 4;

		if (hunger < 0) hunger = 0;
		p.setFoodLevel(Math.min(20, hunger));

		p.getWorld().playSound(p.getLocation(), Sound.EAT, 1.0F, 1.0F);
		p.getWorld().playEffect(p.getLocation(), Effect.STEP_SOUND, 39);
		p.getWorld().playEffect(p.getLocation(), Effect.STEP_SOUND, 40);
		
		this.clearEffect(PotionEffectType.REGENERATION, p);
		p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, (int) Math.floor(regenSeconds * 20), 1));
		return true;
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
