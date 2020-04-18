package me.rey.core.classes.abilities;

import me.rey.core.events.customevents.DamageEvent;

public interface DamageTrigger {
	
	public boolean damageTrigger(DamageEvent e, int level);
	
}
