package me.rey.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.Vector;

import me.rey.core.classes.ClassType;
import me.rey.core.pvp.ToolType;

public class Utils {
	
	private static List<Material> usable = new ArrayList<>();
	
	public static List<Material> usableBlocks(){
		if(usable.isEmpty()) {
			usable = Arrays.asList(
		            Material.ACACIA_DOOR,
		            Material.ACACIA_FENCE_GATE,
		            Material.ACACIA_FENCE,
		            Material.ANVIL,
		            Material.STANDING_BANNER,
		            Material.BANNER,
		            Material.WALL_BANNER,
		            Material.BEACON,
		            Material.BED,
		            Material.BIRCH_DOOR,
		            Material.BIRCH_FENCE_GATE,
		            Material.BIRCH_FENCE,
		            Material.BOAT,
		            Material.BREWING_STAND,
		            Material.COMMAND,
		            Material.CHEST,
		            Material.DARK_OAK_DOOR,
		            Material.DARK_OAK_FENCE,
		            Material.DARK_OAK_FENCE_GATE,
		            Material.DAYLIGHT_DETECTOR,
		            Material.DAYLIGHT_DETECTOR_INVERTED,
		            Material.DISPENSER,
		            Material.DROPPER,
		            Material.ENCHANTMENT_TABLE,
		            Material.ENDER_CHEST,
		            Material.FENCE_GATE,
		            Material.FENCE,
		            Material.FURNACE,
		            Material.HOPPER,
		            Material.HOPPER_MINECART,
		            Material.ITEM_FRAME,
		            Material.IRON_DOOR,
		            Material.IRON_DOOR_BLOCK,
		            Material.IRON_TRAPDOOR,
		            Material.JUNGLE_DOOR,
		            Material.JUNGLE_FENCE,
		            Material.JUNGLE_FENCE_GATE,
		            Material.LEVER,
		            Material.MINECART,
		            Material.NOTE_BLOCK,
		            Material.POWERED_MINECART,
		            Material.REDSTONE_COMPARATOR,
		            Material.REDSTONE_COMPARATOR_OFF,
		            Material.REDSTONE_COMPARATOR_ON,
		            Material.SIGN,
		            Material.SIGN_POST,
		            Material.SPRUCE_DOOR,
		            Material.SPRUCE_FENCE,
		            Material.SPRUCE_FENCE_GATE,
		            Material.STORAGE_MINECART,
		            Material.TRAP_DOOR,
		            Material.TRAPPED_CHEST,
		            Material.WALL_SIGN,
		            Material.WOOD_BUTTON,
		            Material.WOODEN_DOOR,
		            Material.WOOD_DOOR
		            );
		}
		
		return usable;
	}
	
	public static boolean compareItems(ItemStack a, ItemStack b) {
		try {
			LeatherArmorMeta meta1 = (LeatherArmorMeta)a.getItemMeta();
			LeatherArmorMeta meta2 = (LeatherArmorMeta)b.getItemMeta();
			
			if(meta1.getColor().asRGB() == meta2.getColor().asRGB())
				return true;
			
		} catch (Exception e) {
		    if(a == null || b == null)
		        return false;
		    if(a.getType() != b.getType())
		        return false;
		    return true;
					
		}
		return false;
	}
	
	public static ItemStack getColoredArmor(Material LEATHER_ARMOR, int red, int green, int blue) {
		ItemStack larmor = new ItemStack(LEATHER_ARMOR, 1);
		LeatherArmorMeta lam = (LeatherArmorMeta)larmor.getItemMeta();
		lam.setColor(Color.fromRGB(red, green, blue));
		larmor.setItemMeta(lam);
		return larmor;
	}
	
	public static Vector getDirectionBetweenLocations(Location start, Location end) {
		Vector from = start.toVector();
		Vector to = end.toVector();
		return to.subtract(from);
	}
	
	public static boolean isInPlayerView(Player player, LivingEntity toFind, double accuracy) {
		final double maxDistance = 200.0;
        final double precision = accuracy;
        final Location nearbyToPlayer = player.getLocation(); // this is the starting point of a ray

        Set<LivingEntity> nearbyToStart = getNearbyEntities(nearbyToPlayer, maxDistance);

        final Vector direction = player.getLocation().getDirection();
        for(int i = 0; i < maxDistance; i += precision) { // then for a distance as big as our specified maximum
            Vector offset = direction.clone().multiply(i);
            Location pointCheck = nearbyToPlayer.clone().add(offset);// we check a lot of points on the line depending on the precision
            Set<LivingEntity> nearbyToPoint = getNearbyEntities(pointCheck, precision, nearbyToStart); // to see if there are players near that point

            if(! nearbyToPoint.isEmpty() && nearbyToPoint.contains(toFind)) { // if there is at least one
                return true; // then that's our target
            }
        }
        
        return false;
    }

    private static Set<LivingEntity> getNearbyEntities(Location to, double maxDistance) {
        List<Entity> playersInSameWorld = to.getWorld().getEntities();
        List<LivingEntity> le = new ArrayList<LivingEntity>();
        for(Entity e : playersInSameWorld) {
        	if(e instanceof LivingEntity)
        		le.add((LivingEntity) e);
        }
        return getNearbyEntities(to, maxDistance, le);
    }

    private static Set<LivingEntity> getNearbyEntities(Location to, double maxDistance, Collection<LivingEntity> through) {
        Set<LivingEntity> nearbyPlayers = new HashSet<>();

        for(Entity nearby : through) {
        	if(!(nearby instanceof LivingEntity))
        		continue;
            if(nearby.getLocation().distanceSquared(to) > maxDistance * maxDistance)
                continue;
            nearbyPlayers.add((LivingEntity) nearby);
        } return nearbyPlayers;
    }
    
    public static ItemStack updateItem(ItemStack item) {
		if(item.getType().equals(Material.AIR)) return item;
		if(item.getMaxStackSize() > 1) return item;
		
		if(item.hasItemMeta() && item.getItemMeta().hasDisplayName()) return item;
				
		String name = null;
		boolean edit = false;
		for(ToolType toolType : ToolType.values()) {
			if(!toolType.getType().equals(item.getType())) continue;
			name = toolType.getName();
			edit = true;
		}
		
		for(ClassType classType : ClassType.values()) {
			if(Utils.compareItems(item, classType.getHelmet().get())) name = classType.getName() + " Helmet";
			if(Utils.compareItems(item, classType.getChestplate().get())) name = classType.getName() + " Chestplate";
			if(Utils.compareItems(item, classType.getLeggings().get())) name = classType.getName() + " Leggings";
			if(Utils.compareItems(item, classType.getBoots().get())) name = classType.getName() + " Boots";
			edit = true;
		}
		
		ItemMeta meta = item.getItemMeta();
		if(edit) {
			item.setDurability((short)0);
			meta.spigot().setUnbreakable(true);
			item.setItemMeta(meta);	
		}
		
		if(name == null) return item;
		meta.setDisplayName(ChatColor.RESET + name);
		item.setItemMeta(meta);
		return item;
	}
    
	public static void showPlayer(Player p) {
		for(Player online : Bukkit.getOnlinePlayers()) {
			if(online.getUniqueId().equals(p.getUniqueId())) continue;
			online.showPlayer(p);
		}
	}
	
	public static void hidePlayer(Player p) {
		for(Player online : Bukkit.getOnlinePlayers()) {
			if(online.getUniqueId().equals(p.getUniqueId())) continue;
			online.hidePlayer(p);
		}
	}
	
    

}
