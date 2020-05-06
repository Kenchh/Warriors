package me.rey.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BlockLocation {

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

        /* Sketch
         * https://imgur.com/a/H1BUrBQ
         */

        double yaw = p.getLocation().getYaw();
        double angle = Math.toRadians(yaw);

        /* South - West */
        if(angle >= 0 + 0.3 && angle <= Math.PI/2 - 0.3 || angle >= -2*Math.PI + 0.3 && angle <= -3*(Math.PI/2) - 0.3) {

            org.bukkit.Location locAtX = block.getLocation();
            locAtX.setX(locAtX.getX() - 1);

            org.bukkit.Location locAtZ = block.getLocation();
            locAtZ.setZ(locAtZ.getZ() + 1);

            if(locAtX.getBlock().getType().isSolid() && locAtZ.getBlock().getType().isSolid()) {
                return true;
            }

        }

        /* North - West */
        if(angle >= Math.PI/2 + 0.3 && angle <= Math.PI - 0.3 || angle >= -3*(Math.PI/2) + 0.3 && angle <= -Math.PI - 0.3) {

            org.bukkit.Location locAtX = block.getLocation();
            locAtX.setX(locAtX.getX() - 1);

            org.bukkit.Location locAtZ = block.getLocation();
            locAtZ.setZ(locAtZ.getZ() - 1);

            if(locAtX.getBlock().getType().isSolid() && locAtZ.getBlock().getType().isSolid()) {
                return true;
            }

        }

        /* North - East */
        if(angle >= Math.PI + 0.3 && angle <= 3*(Math.PI/2) - 0.3 || angle >= -Math.PI + 0.3 && angle <= -1*(Math.PI/2) - 0.3) {

            org.bukkit.Location locAtX = block.getLocation();
            locAtX.setX(locAtX.getX() + 1);

            org.bukkit.Location locAtZ = block.getLocation();
            locAtZ.setZ(locAtZ.getZ() - 1);

            if(locAtX.getBlock().getType().isSolid() && locAtZ.getBlock().getType().isSolid()) {
                return true;
            }

        }

        /* South - East */
        if(angle >= 3*(Math.PI/2) + 0.3 && angle <= 2*Math.PI - 0.3 || angle >= -1*(Math.PI/2) + 0.3 && angle <= 0 - 0.3) {

            org.bukkit.Location locAtX = block.getLocation();
            locAtX.setX(locAtX.getX() + 1);

            org.bukkit.Location locAtZ = block.getLocation();
            locAtZ.setZ(locAtZ.getZ() + 1);

            if(locAtX.getBlock().getType().isSolid() && locAtZ.getBlock().getType().isSolid()) {
                return true;
            }

        }

        return false;

    }

    public static Block getBlockUnderneath(Block b) {
        org.bukkit.Location loc = new org.bukkit.Location(b.getWorld(), b.getLocation().getX(), b.getLocation().getY() - 1.0, b.getLocation().getZ());
        Block bu = Bukkit.getWorld(b.getWorld().getName()).getBlockAt(loc);
        return bu;
    }

    public static Block getBlockAbove(Block b) {
        org.bukkit.Location loc = new org.bukkit.Location(b.getWorld(), b.getLocation().getX(), b.getLocation().getY() + 1.0, b.getLocation().getZ());
        Block ba = Bukkit.getWorld(b.getWorld().getName()).getBlockAt(loc);
        return ba;
    }

    public static BlockFace getClosestFace(float direction){

        direction = direction % 360;

        if(direction < 0)
            direction += 360;

        direction = Math.round(direction / 90);

        switch((int)direction){

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

    public static double[] getXZCordsFromDegree(Player p, boolean rotated, double addAngle, double radius, double degree) {
        double radian = Math.toRadians(degree);

        double xMultiplier = Math.cos(radian);
        double zMultiplier = Math.sin(radian + addAngle);

        double addX = xMultiplier * radius;
        double addZ = zMultiplier * radius;

        double[] xzCords = new double[2];

        double x = p.getLocation().getX();
        double z = p.getLocation().getZ();

        if(rotated) {
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

}
