package me.rey.core.classes.conditions;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassCondition;
import me.rey.core.classes.ClassType;
import me.rey.core.players.User;

public class ArcaneRepair extends ClassCondition {

	private Set<UUID> inCombat;
	private int delay = 8, regenInterval = 5, maxHearts = 3;
	private double heartsPerRegen = 1;
	
	public ArcaneRepair() {
		super(ClassType.GOLD);
		
		this.inCombat = new HashSet<>();
	}

	@Override
	protected void execute(User u, Player p) {
		if(u.isInCombat()) {
			inCombat.add(u.getUniqueId());
		} else if (!u.isInCombat() && inCombat.contains(u.getUniqueId())){
			inCombat.remove(u.getUniqueId());
			
			double timerInterval = 0.1;
			new BukkitRunnable() {
				
				double seconds = 0;
				int regened = 0;
				
				@Override
				public void run() {
					if(u.isInCombat() || regened == maxHearts || p.getHealth() == p.getMaxHealth()) {
						this.cancel();
						return;
					}
					
					seconds = Double.parseDouble(String.format("%.1f", seconds));
					
					if((double) delay <= (double) seconds) {
						for(int i = 0; i < (maxHearts * regenInterval); i+=regenInterval) {
							if(seconds-delay != (double) i) continue;
							regened++;
							
							if(!p.isDead()) {
								p.setHealth(Math.min(20, p.getHealth()+(heartsPerRegen*2)));
								p.getWorld().spigot().playEffect(p.getEyeLocation(), Effect.HEART, 0, 0, 0F, 0F, 0.5F, 0F, 1, 10);
							}
						}
					}
					
					seconds = seconds + timerInterval;
				}
				
			}.runTaskTimer(Warriors.getInstance(), 0, (int) (timerInterval * 20));
			
		}
		
	}
	
}
