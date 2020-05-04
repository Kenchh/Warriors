package me.rey.core.classes.abilities.ninja;

import com.avaje.ebean.Update;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.events.customevents.UpdateEvent;
import me.rey.core.players.User;
import me.rey.core.pvp.ToolType;
import me.rey.core.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Flash extends Ability {

    private final int cooldownUntilNewCharge = 60;
    private final int range = 5;

    private HashMap<UUID, Integer> charges = new HashMap<UUID, Integer>();
    private HashMap<UUID, Integer> cd = new HashMap<UUID, Integer>();

    public Flash() {
        super(3, "Flash", ClassType.LEATHER, AbilityType.AXE, 1, 4, 0, Arrays.asList(
                "Flash forwards 6 Blocks.",
                "Store up to <variable>1 + l</variable> Flash Charges.",
                "Cannot be used while Slowed."
        ));

        setIgnoresCooldown(true);
        setWhileSlowed(false);
    }

    @EventHandler
    public void onTick(UpdateEvent e) {

        for(Player p : Bukkit.getOnlinePlayers()) {
            if (!(new User(p).isUsingAbility(this))) return;

            checkInFlashList(p);

            if(charges.get(p.getUniqueId()) < 5) {
                if (cd.get(p.getUniqueId()) >= cooldownUntilNewCharge) {
                    addCharge(p);
                    setCD(p, 0);

                    sendAbilityMessage(p, "Flash Charges: " + ChatColor.YELLOW + charges.get(p.getUniqueId()) + " " + ChatColor.GREEN + "(+1)");

                } else {
                    setCD(p, cd.get(p.getUniqueId()) + 1);
                }
            }
        }

    }

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {

        if(charges.get(p.getUniqueId()) <= 0) {
            return false;
        }

        Block b = null;


        if(p.getLocation().getBlock().getType().isSolid() == false) { /* TODO Temporary Condition Solution for non-cubic blocks */

            if (atBlockGap(p, p.getLocation().getBlock()) == false && atBlockGap(p, getBlockAbove(p.getLocation().getBlock())) == false) {
                for (int i = 0; i < range; i++) {

                    if (atBlockGap(p, getTargetBlock(p, i)) || atBlockGap(p, getBlockAbove(getTargetBlock(p, i)))) {
                        b = getTargetBlock(p, i - 1);
                        break;
                    }

                    if (getTargetBlock(p, i).getType().isSolid() == false && getBlockAbove(getTargetBlock(p, i)).getType().isSolid() == false) {
                        b = getTargetBlock(p, i);
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
            p.teleport(loc);
            makeParticlesBetween(p.getLocation(), loc);
        } else {
            makeParticlesBetween(p.getLocation(), p.getLocation());
        }

        p.setFallDistance(0);

        p.getWorld().playSound(p.getLocation(), Sound.WITHER_SHOOT, 0.4f, 1.2f);
        p.getWorld().playSound(p.getLocation(), Sound.SILVERFISH_KILL, 1f, 1.6f);

        removeCharge(p);

        sendAbilityMessage(p, "Flash Charges: " + ChatColor.YELLOW + charges.get(p.getUniqueId()) + " " + ChatColor.RED + "(-1)");

        return true;

    }

    public boolean atBlockGap(Player p, Block block) {

        /* Sketch
         * https://imgur.com/a/H1BUrBQ
         */

        double yaw = p.getLocation().getYaw();
        double angle = Math.toRadians(yaw);

        /* South - West */
        if(angle >= 0 + 0.3 && angle <= Math.PI/2 - 0.3 || angle >= -2*Math.PI + 0.3 && angle <= -3*(Math.PI/2) - 0.3) {

            Location locAtX = block.getLocation();
            locAtX.setX(locAtX.getX() - 1);

            Location locAtZ = block.getLocation();
            locAtZ.setZ(locAtZ.getZ() + 1);

            if(locAtX.getBlock().getType().isSolid() && locAtZ.getBlock().getType().isSolid()) {
                return true;
            }

        }

        /* North - West */
        if(angle >= Math.PI/2 + 0.3 && angle <= Math.PI - 0.3 || angle >= -3*(Math.PI/2) + 0.3 && angle <= -Math.PI - 0.3) {

            Location locAtX = block.getLocation();
            locAtX.setX(locAtX.getX() - 1);

            Location locAtZ = block.getLocation();
            locAtZ.setZ(locAtZ.getZ() - 1);

            if(locAtX.getBlock().getType().isSolid() && locAtZ.getBlock().getType().isSolid()) {
                return true;
            }

        }

        /* North - East */
        if(angle >= Math.PI + 0.3 && angle <= 3*(Math.PI/2) - 0.3 || angle >= -Math.PI + 0.3 && angle <= -1*(Math.PI/2) - 0.3) {

            Location locAtX = block.getLocation();
            locAtX.setX(locAtX.getX() + 1);

            Location locAtZ = block.getLocation();
            locAtZ.setZ(locAtZ.getZ() - 1);

            if(locAtX.getBlock().getType().isSolid() && locAtZ.getBlock().getType().isSolid()) {
                return true;
            }

        }

        /* South - East */
        if(angle >= 3*(Math.PI/2) + 0.3 && angle <= 2*Math.PI - 0.3 || angle >= -1*(Math.PI/2) + 0.3 && angle <= 0 - 0.3) {

            Location locAtX = block.getLocation();
            locAtX.setX(locAtX.getX() + 1);

            Location locAtZ = block.getLocation();
            locAtZ.setZ(locAtZ.getZ() + 1);

            if(locAtX.getBlock().getType().isSolid() && locAtZ.getBlock().getType().isSolid()) {
                return true;
            }

        }

        return false;

    }

    public void checkInFlashList(Player p) {
        if(cd.containsKey(p.getUniqueId()) && charges.containsKey(p.getUniqueId())) {
        } else {
            cd.remove(p.getUniqueId());
            charges.remove(p.getUniqueId());

            cd.put(p.getUniqueId(), 0);
            charges.put(p.getUniqueId(), 0);
        }
    }

    public void addCharge(Player p) {
        charges.replace(p.getUniqueId(), charges.get(p.getUniqueId()) + 1);
    }

    public void removeCharge(Player p) {
        charges.replace(p.getUniqueId(), charges.get(p.getUniqueId()) - 1);
    }

    public void setCD(Player p, int cooldown) {
        cd.replace(p.getUniqueId(), cooldown);
    }

    public static Block getTargetBlock(Player player, int range) {
        Location loc = player.getEyeLocation();
        Vector dir = loc.getDirection().normalize();

        Block b = null;

        for (int i = 0; i <= range; i++) {

            b = loc.add(dir).getBlock();
        }
        return b;
    }

    private void makeParticlesBetween(Location init, Location loc) {
        Vector pvector = Utils.getDirectionBetweenLocations(init, loc);
        for(double i = 1; i <= init.distance(loc); i += 0.5) {
            pvector.multiply(i);
            init.add(pvector);
            Location toSpawn = init.clone();
            toSpawn.setY(toSpawn.getY() + 0.5);
            init.getWorld().spigot().playEffect(toSpawn, Effect.FIREWORKS_SPARK, 0, 0, 0F, 0F, 0F, 0F, 5, 50);
            init.subtract(pvector);
            pvector.normalize();
        }
    }

    public static Block getBlockUnderneath(Block b) {
        Location loc = new Location(b.getWorld(), b.getLocation().getX(), b.getLocation().getY() - 1.0, b.getLocation().getZ());
        Block bu = Bukkit.getWorld(b.getWorld().getName()).getBlockAt(loc);
        return bu;
    }

    public static Block getBlockAbove(Block b) {
        Location loc = new Location(b.getWorld(), b.getLocation().getX(), b.getLocation().getY() + 1.0, b.getLocation().getZ());
        Block ba = Bukkit.getWorld(b.getWorld().getName()).getBlockAt(loc);
        return ba;
    }

}
