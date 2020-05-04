package me.rey.core.classes.abilities.ninja;

import com.avaje.ebean.Update;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.events.customevents.UpdateEvent;
import me.rey.core.players.User;
import me.rey.core.pvp.ToolType;
import me.rey.core.utils.BlockLocation;
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

}
