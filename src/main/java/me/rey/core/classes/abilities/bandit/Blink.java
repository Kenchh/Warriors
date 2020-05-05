package me.rey.core.classes.abilities.bandit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import me.rey.core.utils.BlockLocation;
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
import me.rey.core.enums.AbilityFail;
import me.rey.core.events.customevents.AbilityFailEvent;
import me.rey.core.players.User;
import me.rey.core.utils.Utils;

public class Blink extends Ability {
	
	private HashMap<UUID, Location> canDeblink;
	private final int deblinkSeconds = 4;
	private final String deblink = "De-Blink";

	public Blink() {
		super(701, "Blink", ClassType.BLACK, AbilityType.AXE, 1, 4, 12, Arrays.asList(
				"Instantly teleport forwards <variable>3*l+9</variable> (+3) Blocks.",
				"",
				"Using again within <variable>4</variable> seconds De-Blinks,",
				"returning you to your original location.",
				"Cannot be used while Slowed",
				"",
				"Recharge: 12.0 Seconds"
				));
		
		canDeblink = new HashMap<>();
		this.setWhileSlowed(false);
	}

	@Override
	protected boolean execute(User u, final Player p, int level, Object... conditions) {
		Location init = p.getLocation();
		double range = 3*level+9;

		Block b = null;

		if(p.getLocation().getBlock().getType().isSolid() == false) { /* TODO Temporary Condition Solution for non-cubic blocks */

			if (BlockLocation.atBlockGap(p, p.getLocation().getBlock()) == false && BlockLocation.atBlockGap(p, BlockLocation.getBlockAbove(p.getLocation().getBlock())) == false) {
				for (int i = 0; i < range; i++) {

					if (BlockLocation.atBlockGap(p, BlockLocation.getTargetBlock(p, i)) || BlockLocation.atBlockGap(p, BlockLocation.getBlockAbove(BlockLocation.getTargetBlock(p, i)))) {
						b = BlockLocation.getTargetBlock(p, i - 1);
						break;
					}

					if (BlockLocation.getTargetBlock(p, i).getType().isSolid() == false && BlockLocation.getBlockAbove(BlockLocation.getTargetBlock(p, i)).getType().isSolid() == false) {
						b = BlockLocation.getTargetBlock(p, i);
					} else {
						break;
					}
				}
			}
		}

		Location loc = null;

		if(b != null) {
			loc = b.getLocation();
			loc.setX(loc.getX() + 0.5);
			loc.setZ(loc.getZ() + 0.5);

			loc.setYaw(p.getLocation().getYaw());
			loc.setPitch(p.getLocation().getPitch());

		}

		if(loc != null) {
			if(p.getTargetBlock((Set<Material>) null, 5).getType().isSolid()) {

				Block tb = p.getTargetBlock((Set<Material>) null, 5);
				float dir = (float)Math.toDegrees(Math.atan2(p.getLocation().getBlockX() - tb.getX(), tb.getZ() - p.getLocation().getBlockZ()));
				BlockFace face = BlockLocation.getClosestFace(dir);

				if(face == BlockFace.NORTH || face == BlockFace.EAST || face == BlockFace.SOUTH || face == BlockFace.WEST) {
					Location tloc = tb.getLocation();

					if (face == BlockFace.NORTH) {
						tloc.setX(tloc.getX() + 1.35);
						tloc.setZ(tloc.getZ() + 0.5);
					}

					if (face == BlockFace.EAST) {
						tloc.setZ(tloc.getZ() + 1.35);
						tloc.setX(tloc.getX() + 0.5);
					}

					if (face == BlockFace.SOUTH) {
						tloc.setX(tloc.getX() - 0.35);
						tloc.setZ(tloc.getZ() + 0.5);
					}

					if (face == BlockFace.WEST) {
						tloc.setZ(tloc.getZ() - 0.35);
						tloc.setX(tloc.getX() + 0.5);
					}

					tloc.setY(loc.getY());
					tloc.setYaw(p.getLocation().getYaw());
					tloc.setPitch(p.getLocation().getPitch());

					if (tloc.getBlock().getType().isSolid() == false) {
						makeParticlesBetween(p.getLocation(), tloc);
						p.teleport(tloc);
					}
				} else {
					makeParticlesBetween(p.getLocation(), loc);
					p.teleport(loc);
				}

			} else {
				makeParticlesBetween(p.getLocation(), loc);
				p.teleport(loc);
			}
		}

		p.setFallDistance(0);
		p.getWorld().playEffect(p.getLocation(), Effect.BLAZE_SHOOT, 0);
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
	public void onDeblink(AbilityFailEvent e) {
		if(e.getAbility() != this || e.getFail() != AbilityFail.COOLDOWN) return;
		if(!this.canDeblink.containsKey(e.getPlayer().getUniqueId())) return;
		
		Location to = this.canDeblink.get(e.getPlayer().getUniqueId());
		Location from = e.getPlayer().getLocation();
		to.setPitch(from.getPitch());
		to.setYaw(from.getYaw());

		e.getPlayer().getWorld().playEffect(e.getPlayer().getLocation(), Effect.BLAZE_SHOOT, 0);
		this.makeParticlesBetween(from,  to);
		e.getPlayer().teleport(to);
		e.getPlayer().setFallDistance(0);
		this.canDeblink.remove(e.getPlayer().getUniqueId());
		this.sendUsedMessageToPlayer(e.getPlayer(), this.deblink);
		e.setMessageCancelled(true);
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
