package me.rey.core.combat;

import java.util.HashMap;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

public class CombatManager {
	
	public static HashMap<LivingEntity, Long> hits = new HashMap<>();
	
	public static long timeAgo(LivingEntity ent) {
		return hits.containsKey(ent) ? System.currentTimeMillis() - hits.get(ent): System.currentTimeMillis();
	}
	
	public static void resetTime(LivingEntity ent) {
		if(hits.containsKey(ent))
			hits.replace(ent, System.currentTimeMillis());
		else
			hits.put(ent, System.currentTimeMillis());
	}
	
	@EventHandler
	public void onDeath(EntityDeathEvent e) {
		if(!(e.getEntity() instanceof LivingEntity)) return;
		
		LivingEntity ent = (LivingEntity) e.getEntity();
		if(hits.containsKey(ent)) hits.remove(ent);
	}
}
