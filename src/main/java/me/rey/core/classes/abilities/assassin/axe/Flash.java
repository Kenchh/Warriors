package me.rey.core.classes.abilities.assassin.axe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.events.customevents.update.UpdateEvent;
import me.rey.core.gui.Gui.Item;
import me.rey.core.packets.ActionBar;
import me.rey.core.players.User;
import me.rey.core.utils.BlockLocation;
import me.rey.core.utils.Text;
import me.rey.core.utils.Utils;

public class Flash extends Ability implements IConstant {

    private final int cooldownUntilNewCharge = 60;
    private final int range = 5;

    private HashMap<UUID, Integer> charges = new HashMap<UUID, Integer>();
    private HashMap<UUID, Integer> cd = new HashMap<UUID, Integer>();
    
    private static final String NOT_CHARGED = "○";
    private static final String CHARGING = "◎";
    private static final String CHARGED = "●";

    public Flash() {
        super(12, "Flash", ClassType.LEATHER, AbilityType.AXE, 1, 4, 0, Arrays.asList(
                "Flash forwards 6 Blocks.",
                "Store up to <variable>1 + l</variable> Flash Charges.",
                "Cannot be used while Slowed."
        ));

        setIgnoresCooldown(true);
        setWhileSlowed(false);
    }

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {
    	
    	/*
    	 * CHARGE REGENERATION
    	 */
    	if(conditions != null && conditions.length == 1 && conditions[0] != null && conditions[0] instanceof UpdateEvent) {
    		
            int maxCharges = level + 1;
            checkInFlashList(p);

            if(charges.get(p.getUniqueId()) < maxCharges) {
                if (cd.get(p.getUniqueId()) >= cooldownUntilNewCharge) {
                    addCharge(p);
                    setCD(p, 0);

                    sendAbilityMessage(p, "Charges: " + ChatColor.YELLOW + charges.get(p.getUniqueId()) /* + " " + ChatColor.GREEN + "(+1)" */);
                } else {
                    setCD(p, cd.get(p.getUniqueId()) + 1);
                }
            } 
            
            // Capping people's charges
            while(this.charges.get(p.getUniqueId()) > maxCharges) {
            	removeCharge(p);
            }
            
            
            // Action Bar
            if(this.matchesAbilityTool(this.match(p.getItemInHand() == null ? new Item(Material.AIR).get() : p.getItemInHand()))) {
	            String chargeList = "";
	            int flashCount = charges.get(p.getUniqueId());
	            
	            for(int i = 0; i < flashCount; i++) chargeList += ChatColor.GREEN + CHARGED + " ";
	            if(flashCount < maxCharges) chargeList += ChatColor.YELLOW + CHARGING + " ";
	            for(int i = 0; i < (maxCharges - flashCount - 1); i++) chargeList += ChatColor.RED + NOT_CHARGED + " ";
	            
	            new ActionBar(Text.color("&f&lFlash Charges: " + chargeList)).send(p);;
            }
       
    		return false;
    	}
    	//END
    	

        if(charges.get(p.getUniqueId()) <= 0) return false;

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

        p.getWorld().playSound(p.getLocation(), Sound.WITHER_SHOOT, 0.4f, 1.2f);
        p.getWorld().playSound(p.getLocation(), Sound.SILVERFISH_KILL, 1f, 1.6f);

        removeCharge(p);

        sendAbilityMessage(p, "Charges: " + ChatColor.YELLOW + charges.get(p.getUniqueId()) /*+ " " + ChatColor.RED + "(-1)" */);

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
        for(double i = 1; i <= init.distance(loc); i += 0.2) {
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
