package me.rey.core.events.customevents.damage;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.rey.core.Warriors;
import me.rey.core.players.PlayerHitCache;
import me.rey.core.pvp.ToolType.HitType;

public class DamageEvent extends CustomDamageEvent {

	private PlayerHitCache cache = Warriors.getInstance().getHitCache();
	
	public DamageEvent(HitType hitType, Player damager, LivingEntity damagee, double damage, ItemStack item) {
		super(hitType, damager, damagee, damage, item);
	}
	
	@Override
	public Player getDamager() {
		return (Player) this.damager;
	}
	
	public void storeCache() {
		// ADDING TO THEIR HIT CACHE IF THEY'RE A PLAYER
		if(damagee instanceof Player && !this.isCancelled())
			cache.addToPlayerCache((Player) damagee, this.getHit());
	}

}
