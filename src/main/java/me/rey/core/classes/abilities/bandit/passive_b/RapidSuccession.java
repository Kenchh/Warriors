package me.rey.core.classes.abilities.bandit.passive_b;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IDamageTrigger;
import me.rey.core.events.customevents.damage.DamageEvent;
import me.rey.core.players.User;

public class RapidSuccession extends Ability implements IDamageTrigger {

	private static HashMap<Player, String> hits = new HashMap<>();
	private final double INCREASE = 1;
	private final double EXPIRE = 3.00;
	
	public RapidSuccession() {
		super(142, "Rapid Succession", ClassType.BLACK, AbilityType.PASSIVE_B, 1, 2, 0.00, Arrays.asList(
				"Your damage gets boosted by",
				"a factor of 1 every time you",
				"hit your enemy.", "",
				"It does not stack over enemies.",
				"and has a maximum of <variable>l*1</variable> (+1) damage.",
				"The boost expires after 3 seconds",
				"out of combat."
				));
		
		this.setIgnoresCooldown(true);
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		DamageEvent e = (DamageEvent) conditions[0];
		UUID found = e.getDamagee().getUniqueId();
		
		if(hits.containsKey(p)) {
			String[] KeyAndValue = hits.get(p).split(";");
			UUID stacked = UUID.fromString(KeyAndValue[0]);
			double dmg = Double.parseDouble(KeyAndValue[1]);
			
			if (found.equals(stacked)) {
				e.addMod(dmg);
				addToHits(p, found, Math.min(dmg+1, level));
			} else {
				addToHits(p, found, INCREASE);
			}
				
		} else {
			addToHits(p, found, INCREASE);
		}
		
		return true;
	}
	
	private void addToHits(Player p, UUID uuid, double dmg) {
		String s = uuid.toString() + ";" + dmg;
		BukkitTask remove = new BukkitRunnable() {			
			
			@Override
			public void run() {
				if(hits.containsKey(p)) {
					hits.remove(p, s + ";" + this.getTaskId());
				}
			}
		}.runTaskLater(Warriors.getInstance(), (int) (20 * EXPIRE));;
		String copy = s + ";" + remove.getTaskId();
		
		if(hits.containsKey(p))
			hits.replace(p, copy);
		else
			hits.put(p, copy);
	}

}
