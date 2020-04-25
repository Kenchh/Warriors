package me.rey.core.classes.abilities.ninja;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.events.customevents.AbilityUseWhileCooldownEvent;
import me.rey.core.players.User;
import me.rey.core.utils.Utils;

public class Blink extends Ability {
	
	private HashMap<UUID, Location> canDeblink;
	private final int deblinkSeconds = 4;
	private final String deblink = "De-Blink";

	public Blink() {
		super(3, "Blink", ClassType.LEATHER, AbilityType.AXE, 1, 4, 12, Arrays.asList(
				"Instantly teleport forwards <variable>3*l+9</variable> (+3) Blocks.",
				"",
				"Using again within <variable>4</variable> seconds De-Blinks,",
				"returning you to your original location.",
				"Cannot be used while Slowed",
				"",
				"Recharge: 12.0 Seconds"
				));
		
		canDeblink = new HashMap<>();
	}

	@Override
	protected boolean execute(User u, final Player p, int level, Object... conditions) {
		Location loc = p.getLocation();
		Location init = loc.clone();
		double range = 3*level+9;
		
		Location targetBlock = p.getTargetBlock((Set <Material>) null, (int) range).getLocation();
		double distance = init.distance(targetBlock);
		if(range > distance){
			range = distance - 0.7;
		}
		
		
		// TELEPORTING
		Vector direction = loc.getDirection();
		direction.normalize();
		direction.multiply(range);
		loc.add(direction);
		
		Block above = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY()+1, loc.getBlockZ());
		Block inside = loc.getBlock();
		if (isAir(above)) {
			loc.setY(loc.getY()-1);
		}
		
		if(isFloor(inside)) {
			loc.setY(loc.getY()+5);
		}

		p.teleport(loc);
		p.setFallDistance(0);
		this.makeParticlesBetween(init, loc);
		
		this.sendUsedMessageToPlayer(p, this.getName());
		
		// ADDING TO DEBLINK USER LIST
		this.canDeblink.put(p.getUniqueId(), init);
		new BukkitRunnable() {
			
			@Override
			public void run() {
				// TRYING TO REMOVE IF HE HASN'T DEBLINKED
				if(canDeblink.containsKey(p.getUniqueId())) {
					canDeblink.remove(p.getUniqueId());
				}
				
			}
			
		}.runTaskLater(Warriors.getInstance(), deblinkSeconds * 20);
		return true;
	}
	
	@EventHandler
	public void onDeblink(AbilityUseWhileCooldownEvent e) {
		if(e.getAbility() != this) return;
		if(!this.canDeblink.containsKey(e.getPlayer().getUniqueId())) return;
		
		Location to = this.canDeblink.get(e.getPlayer().getUniqueId());
		Location from = e.getPlayer().getLocation();
		to.setPitch(from.getPitch());
		to.setYaw(from.getYaw());
		
		this.makeParticlesBetween(from,  to);
		e.getPlayer().teleport(to);
		e.getPlayer().setFallDistance(0);
		this.canDeblink.remove(e.getPlayer().getUniqueId());
		this.sendUsedMessageToPlayer(e.getPlayer(), this.deblink);
		e.cancelMessage(true);
	}
	
	private void makeParticlesBetween(Location init, Location loc) {
		Vector pvector = Utils.getDirectionBetweenLocations(init, loc);
		for(double i = 1; i <= init.distance(loc); i += 0.2) {
			pvector.multiply(i);
			init.add(pvector);
			Location toSpawn = init.clone();
			toSpawn.setY(toSpawn.getY() + 0.5);
			init.getWorld().spigot().playEffect(toSpawn, Effect.LARGE_SMOKE, 0, 0, 0F, 0F, 0F, 0F, 3, 100);
			init.subtract(pvector);
			pvector.normalize();
		}
	}
	
	private boolean isFloor(Block block) {
		Block above = block.getRelative(BlockFace.UP);
		if(isAir(above))
			return true;
		return false;
	}
	
	private boolean isAir(Block block) {
		if(block == null || block.getType().equals(Material.AIR))
			return true;
		return false;
	}

}
