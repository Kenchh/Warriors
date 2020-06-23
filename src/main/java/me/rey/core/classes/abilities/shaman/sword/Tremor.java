package me.rey.core.classes.abilities.shaman.sword;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.players.User;
import me.rey.core.utils.UtilBlock;

public class Tremor extends Ability implements IConstant {
	
	private static HashMap<Player, Rupture> activeTremors = new HashMap<>();

	public Tremor() {
		super(501, "Tremor", ClassType.GREEN, AbilityType.SWORD, 1, 5, 0.00, Arrays.asList(
				"Tremor desc"
				));
		
		this.setIgnoresCooldown(true);
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		
		if (!p.isBlocking() && activeTremors.containsKey(p)) { 
			activeTremors.get(p).explode();
			activeTremors.remove(p);
		}
		
		if (!p.isBlocking()) return false;
		
		final int distance = 30;
		final float speedIncrement = 1.3F + (0.5F * level);
		if (!activeTremors.containsKey(p)) activeTremors.put(p, new Rupture(p, level, distance));
		
		activeTremors.get(p).setSpeed(Math.min(activeTremors.get(p).speed + (speedIncrement / 20), speedIncrement * 20));
		activeTremors.get(p).move(p.getTargetBlock((Set<Material>) null, distance).getLocation());
		
		return false;
	}

	class Rupture {

		private final int level;
		private final double maxDistance;
		private final Player owner;
		
		public float speed;
		private Location location;
		
		public Rupture(Player owner, int level, double maxDistance) {
			this.owner = owner;
			this.level = level;
			this.maxDistance = maxDistance;
			
			this.speed = 15F;
			this.location = owner.getLocation();
		}
		
		public void move(Location to) {
			
			this.location.setDirection(to.toVector().subtract(this.location.toVector()));
			double degree = location.getYaw() + 90D;
			
			final double divider = 75;
			double x = UtilBlock.getXZCordsFromDegree(this.location, this.speed/divider, degree)[0];
			double z = UtilBlock.getXZCordsFromDegree(this.location, this.speed/divider, degree)[1];
			
			this.location = new Location(owner.getWorld(), x, location.getY(), z);
			
			if (to.getBlock().getLocation().distance(location) > 2F) {
				play(location);
			} else {
				double y = to.getY() + 0.1;
				this.location.setY(y);
				play(location);
			}
			
			
		}
		
		public void explode() {
			Bukkit.broadcastMessage("exploded poof");
		}
		
		public void setSpeed(float speed) {
			this.speed = speed;
		}
		
		@SuppressWarnings("deprecation")
		private void play(Location loc) {
			location.getWorld().playEffect(location.getBlock().getLocation(), Effect.STEP_SOUND, location.getBlock().getRelative(BlockFace.DOWN).getTypeId());				
		}
		
	}

}
