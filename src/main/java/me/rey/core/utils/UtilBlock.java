package me.rey.core.utils;

import java.util.HashMap;

import me.rey.core.events.customevents.block.CustomBlockPlaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class UtilBlock {

	public static void replaceBlock(CustomBlockPlaceEvent.PlaceCause cause, Block old, Block replace) {
		CustomBlockPlaceEvent event = new CustomBlockPlaceEvent(cause, old, replace);
	}

	public static Block getTargetBlock(Player player, int range) {
		org.bukkit.Location loc = player.getEyeLocation();
		Vector dir = loc.getDirection().normalize();

		Block b = null;

		for (int i = 0; i <= range; i++) {

			b = loc.add(dir).getBlock();
		}
		return b;
	}

	public static boolean atBlockGap(Player p, Block block) {

		/*
		 * Sketch https://imgur.com/a/H1BUrBQ
		 */

		double yaw = p.getLocation().getYaw();
		double angle = Math.toRadians(yaw);

		/* South - West */
		if (angle >= 0 + 0.3 && angle <= Math.PI / 2 - 0.3
				|| angle >= -2 * Math.PI + 0.3 && angle <= -3 * (Math.PI / 2) - 0.3) {

			org.bukkit.Location locAtX = block.getLocation();
			locAtX.setX(locAtX.getX() - 1);

			org.bukkit.Location locAtZ = block.getLocation();
			locAtZ.setZ(locAtZ.getZ() + 1);

			if (locAtX.getBlock().getType().isSolid() && locAtZ.getBlock().getType().isSolid()) {
				return true;
			}

		}

		/* North - West */
		if (angle >= Math.PI / 2 + 0.3 && angle <= Math.PI - 0.3
				|| angle >= -3 * (Math.PI / 2) + 0.3 && angle <= -Math.PI - 0.3) {

			org.bukkit.Location locAtX = block.getLocation();
			locAtX.setX(locAtX.getX() - 1);

			org.bukkit.Location locAtZ = block.getLocation();
			locAtZ.setZ(locAtZ.getZ() - 1);

			if (locAtX.getBlock().getType().isSolid() && locAtZ.getBlock().getType().isSolid()) {
				return true;
			}

		}

		/* North - East */
		if (angle >= Math.PI + 0.3 && angle <= 3 * (Math.PI / 2) - 0.3
				|| angle >= -Math.PI + 0.3 && angle <= -1 * (Math.PI / 2) - 0.3) {

			org.bukkit.Location locAtX = block.getLocation();
			locAtX.setX(locAtX.getX() + 1);

			org.bukkit.Location locAtZ = block.getLocation();
			locAtZ.setZ(locAtZ.getZ() - 1);

			if (locAtX.getBlock().getType().isSolid() && locAtZ.getBlock().getType().isSolid()) {
				return true;
			}

		}

		/* South - East */
		if (angle >= 3 * (Math.PI / 2) + 0.3 && angle <= 2 * Math.PI - 0.3
				|| angle >= -1 * (Math.PI / 2) + 0.3 && angle <= 0 - 0.3) {

			org.bukkit.Location locAtX = block.getLocation();
			locAtX.setX(locAtX.getX() + 1);

			org.bukkit.Location locAtZ = block.getLocation();
			locAtZ.setZ(locAtZ.getZ() + 1);

			if (locAtX.getBlock().getType().isSolid() && locAtZ.getBlock().getType().isSolid()) {
				return true;
			}

		}

		return false;

	}

	public static Block getBlockUnderneath(Block b) {
		org.bukkit.Location loc = new org.bukkit.Location(b.getWorld(), b.getLocation().getX(),
				b.getLocation().getY() - 1.0, b.getLocation().getZ());
		Block bu = Bukkit.getWorld(b.getWorld().getName()).getBlockAt(loc);
		return bu;
	}

	public static Block getBlockAbove(Block b) {
		org.bukkit.Location loc = new org.bukkit.Location(b.getWorld(), b.getLocation().getX(),
				b.getLocation().getY() + 1.0, b.getLocation().getZ());
		Block ba = Bukkit.getWorld(b.getWorld().getName()).getBlockAt(loc);
		return ba;
	}

	public static BlockFace getClosestFace(float direction) {

		direction = direction % 360;

		if (direction < 0)
			direction += 360;

		direction = Math.round(direction / 90);

		switch ((int) direction) {

		case 0:
			return BlockFace.WEST;
		case 1:
			return BlockFace.NORTH;
		case 2:
			return BlockFace.EAST;
		case 3:
			return BlockFace.SOUTH;
		default:
			return BlockFace.WEST;

		}
	}

	public static double[] getXZCordsFromDegree(Player p, double radius, double degree) {
		double radian = Math.toRadians(degree);

		double xMultiplier = Math.cos(radian);
		double zMultiplier = Math.sin(radian);

		double addX = xMultiplier * radius;
		double addZ = zMultiplier * radius;

		double[] xzCords = new double[2];

		double x = p.getLocation().getX() + addX;
		double z = p.getLocation().getZ() + addZ;

		xzCords[0] = x;
		xzCords[1] = z;

		return xzCords;
	}

	public static double[] getXZCordsFromDegree(Location loc, double radius, double degree) {
		double radian = Math.toRadians(degree);

		double xMultiplier = Math.cos(radian);
		double zMultiplier = Math.sin(radian);

		double addX = xMultiplier * radius;
		double addZ = zMultiplier * radius;

		double[] xzCords = new double[2];

		double x = loc.getX() + addX;
		double z = loc.getZ() + addZ;

		xzCords[0] = x;
		xzCords[1] = z;

		return xzCords;
	}

	public static double[] getXZCordsMultipliersFromDegree(double degree) {
		double radian = Math.toRadians(degree);

		double xMultiplier = Math.cos(radian);
		double zMultiplier = Math.sin(radian);

		double[] xzCords = new double[2];

		xzCords[0] = xMultiplier;
		xzCords[1] = zMultiplier;

		return xzCords;
	}

	public static double[] getXZCordsFromDegree(Location loc, boolean rotated, double addAngle, double radius,
			double degree) {
		double radian = Math.toRadians(degree);

		double xMultiplier = Math.cos(radian);
		double zMultiplier = Math.sin(radian + addAngle);

		double addX = xMultiplier * radius;
		double addZ = zMultiplier * radius;

		double[] xzCords = new double[2];

		double x = loc.getX();
		double z = loc.getZ();

		if (rotated) {
			x += addX;
			z -= addZ;
		} else {
			x += addX;
			z += addZ;
		}

		xzCords[0] = x;
		xzCords[1] = z;

		return xzCords;
	}

	public static Location highestLocation(Location locat) {
		double blockY = locat.getBlockY();

		Location tplocation = null;
		for (int i = (int) blockY; i > 1; i--) {
			Location loc = new Location(locat.getWorld(), locat.getX(), i, locat.getZ(), locat.getYaw(),
					locat.getPitch());

			if (loc.getBlock().getType().isSolid()) {
				tplocation = new Location(locat.getWorld(), locat.getX(), i + 1.5, locat.getZ(), locat.getYaw(),
						locat.getPitch());
				break;
			}
		}

		return tplocation;
	}

	public static HashMap<Block, Double> getBlocksInRadius(Location loc, Double dR, double heightLimit) {
		HashMap<Block, Double> blockList = new HashMap<>();
		int iR = dR.intValue() + 1;

		for (int x = -iR; x <= iR; x++) {
			for (int z = -iR; z <= iR; z++)
				for (int y = -iR; y <= iR; y++) {
					if (Math.abs(y) <= heightLimit) {

						Block curBlock = loc.getWorld().getBlockAt((int) (loc.getX() + x), (int) (loc.getY() + y),
								(int) (loc.getZ() + z));

						double offset = UtilMath.offset(loc, curBlock.getLocation().add(0.5D, 0.5D, 0.5D));

						if (offset <= dR.doubleValue())
							blockList.put(curBlock, Double.valueOf(1.0D - offset / dR.doubleValue()));
					}
				}
		}
		return blockList;
	}

}
