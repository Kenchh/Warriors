package me.rey.core.classes.abilities.shaman.sword;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import me.rey.core.Warriors;
import me.rey.core.gui.Gui;
import me.rey.core.items.Throwable;
import me.rey.core.utils.UtilEnt;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.players.User;
import me.rey.core.utils.UtilBlock;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class Tremor extends Ability implements IConstant {
	
	private static HashMap<Player, Rupture> activeTremors = new HashMap<>();

	public Tremor() {
		super(501, "Tremor", ClassType.GREEN, AbilityType.SWORD, 1, 5, 0.00, Arrays.asList(
				"Tremor desc"
				));
		
		this.setIgnoresCooldown(true);
		this.setWhileInAir(false);
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
		if (!activeTremors.containsKey(p)) activeTremors.put(p, new Rupture(p, u, level, distance));

		activeTremors.get(p).setSpeed(Math.min(activeTremors.get(p).speed + (speedIncrement / 20), speedIncrement * 20));
		activeTremors.get(p).move(p.getTargetBlock((Set<Material>) null, distance).getLocation());
		
		return false;
	}

	class Rupture {

		private final int level;
		private final double maxDistance;
		private final Player owner;
		private final User u;
		
		public float speed;
		private Location location;

		public Rupture(Player owner, User u, int level, double maxDistance) {
			this.owner = owner;
			this.u = u;
			this.level = level;
			this.maxDistance = maxDistance;
			
			this.speed = 15F;
			this.location = owner.getLocation();
			this.location.setY(UtilBlock.getHighestClosestAir(owner.getLocation().getBlock()).getY() - 1.0);
		}
		
		public void move(Location to) {

			if(to != null) {
				this.location.setDirection(to.toVector().subtract(this.location.toVector()));
			} else {
				this.location.setDirection(owner.getLocation().toVector().subtract(this.location.toVector()));
			}

			double degree = location.getYaw() + 90D;

			final double divider = 75;
			double x = UtilBlock.getXZCordsFromDegree(this.location, this.speed/divider, degree)[0];
			double y = UtilBlock.getHighestClosestAir(this.location.getBlock()).getY() - 1;
			double z = UtilBlock.getXZCordsFromDegree(this.location, this.speed/divider, degree)[1];

			if(y - location.getY() >= 3 || y - location.getY() <= -3) {
				explode();
				activeTremors.remove(owner);
			} else {
				this.location = new Location(owner.getWorld(), x, y, z);
			}


			play();
		}
		
		public void explode() {

			location.getWorld().playSound(location, Sound.DIG_STONE, 1F, 0.8F);
			for(Location loc : UtilBlock.circleLocations(location, 3, 30)) {
				loc.getWorld().playEffect(loc.getBlock().getLocation(), Effect.STEP_SOUND, loc.getBlock().getRelative(BlockFace.DOWN).getTypeId());
			}

			Location explodeloc = location.clone();

			explodeloc.setY(explodeloc.getY() + 1);

			for(int i=1;i<=10;i++) {
				Throwable item = new Throwable(new Gui.Item(Material.DIRT).setLore(Arrays.asList(i + "")), false);

				explodeloc.setPitch(-75);
				explodeloc.setYaw(360/10 * i + 90);

				Random r = new Random();
				double radd = (double) r.nextInt(20) / 100;

				item.fire(explodeloc, 0.1 + radd, 0.5 + radd);
				Throwable finalItem = item;
				new BukkitRunnable() {
					@Override
					public void run() {
						finalItem.destroy();
					}
				}.runTaskLaterAsynchronously(Warriors.getInstance(), 40L);
			}

			for(Entity e : UtilBlock.getEntitiesInCircle(location, 3)) {
				if(e instanceof LivingEntity) {
					if(e instanceof Player) {
						if(e == owner || u.getTeam().contains(e)) {
							continue;
						}
					}
					UtilEnt.damage(3.5 + 0.5 * level, getName(), (LivingEntity) e, owner);
				}
			}

		}
		
		public void setSpeed(float speed) {
			this.speed = speed;
		}
		
		@SuppressWarnings("deprecation")
		private void play() {
			location.getWorld().playEffect(location.getBlock().getLocation(), Effect.STEP_SOUND, location.getBlock().getRelative(BlockFace.DOWN).getTypeId());				
		}
		
	}

}
