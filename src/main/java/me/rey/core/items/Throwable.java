package me.rey.core.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import me.rey.core.Warriors;
import me.rey.core.gui.Gui;

public class Throwable implements Listener {

    Gui.Item item;
    Item entityitem;
    boolean pickupable = false;

    public boolean fired;
    public boolean destroy;

    public Throwable(Gui.Item item, boolean pickupable) {
        this.item = item;
        if(!pickupable) {
            this.item.setName("&4NON-PICKUPABLE");
            Bukkit.getPluginManager().registerEvents(this, Warriors.getInstance());
        } else {
            this.pickupable = true;
        }
    }

    public void fire(Location loc, double multiplier, double addY) {
        fired = true;
        this.entityitem = loc.getWorld().dropItem(loc, item.get());
        Vector direction = entityitem.getLocation().getDirection();
        this.entityitem.setVelocity(direction.normalize().multiply(multiplier).setY(direction.getY() + addY));
    }

    public void fire(Location loc, double multiplier, double baseY, double addY) {
        fired = true;
        this.entityitem = loc.getWorld().dropItem(loc, item.get());
        Vector direction = entityitem.getLocation().getDirection();
        this.entityitem.setVelocity(direction.normalize().multiply(multiplier).setY(baseY + addY));
    }

    public void fire(Location loc, Vector direction, double multiplier, double addY) {
        fired = true;
        this.entityitem = loc.getWorld().dropItem(loc, item.get());
        this.entityitem.setVelocity(direction.normalize().multiply(multiplier).setY(direction.getY() + addY));
    }

    public void fire(Location loc, Vector direction, double multiplier, double baseY, double addY) {
        fired = true;
        this.entityitem = loc.getWorld().dropItem(loc, item.get());
        this.entityitem.setVelocity(direction.normalize().multiply(multiplier).setY(baseY + addY));
    }

    public void fire(Location loc, Vector v) {
        fired = true;
        this.entityitem = loc.getWorld().dropItem(loc, item.get());
        this.entityitem.setVelocity(v);
    }

    public Gui.Item getItem() {
        return item;
    }

    public void setItem(Gui.Item item) {
        this.item = item;
    }

    public Item getEntityitem() {
        return entityitem;
    }

    public void setEntityitem(Item entityitem) {
        this.entityitem = entityitem;
    }

    public boolean isPickupable() {
        return pickupable;
    }

    public void setPickupable(boolean pickupable) {
        this.pickupable = pickupable;
    }

    public void destroyWhenOnGround() {
        if(this.entityitem.isOnGround()) {
            destroy();
        }
    }

    public void destroy() {
        destroy = true;
        this.entityitem.remove();
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        Item i = e.getItem();
        ItemStack is = i.getItemStack();

        if(is == null || is.hasItemMeta() == false || is.getItemMeta().hasDisplayName() == false) {
            return;
        }

        ItemMeta im = is.getItemMeta();

        if(im.getDisplayName().equals(this.item.getName()) && this.pickupable == false) {
            e.setCancelled(true);
        }

    }

    public static Set<Block> checkForBlockCollision(me.rey.core.items.Throwable item) {

		if (item == null)
			return null;
		
		Item ent = item.getEntityitem();
		if(ent == null) return null;
		
		Block self = ent.getLocation().getBlock();

		Block bOne = self.getRelative(BlockFace.UP), bTwo = self.getRelative(BlockFace.DOWN);
		Block bThree = self.getRelative(BlockFace.WEST), bFour = self.getRelative(BlockFace.EAST);
		Block bFive = self.getRelative(BlockFace.NORTH), bSix = self.getRelative(BlockFace.SOUTH);
		List<Block> list = new ArrayList<>(Arrays.asList(bOne, bTwo, bThree, bFour, bFive, bSix));
		Set<Block> toReturn = new HashSet<Block>();
		
		for(Block b : list) {
			if(!solid(b))
				toReturn.add(b);
		}
		
		
		return toReturn.isEmpty() ? null : toReturn;
	}
    
    public static Set<LivingEntity> checkForEntityCollision(Throwable item, double nearbyX, double nearbyY, double nearbyZ) {

		if (item == null)
			return null;
		
		Item ent = item.getEntityitem();
		if(ent == null) return null;
		
		Iterator<Entity> nearby = ent.getNearbyEntities(nearbyX, nearbyY, nearbyZ).iterator();
		Set<LivingEntity> toReturn = new HashSet<>();
		
		while(nearby.hasNext()) {
			Entity entity = nearby.next();
			if(!(entity instanceof LivingEntity)) continue;
			toReturn.add((LivingEntity) entity);
		}
		
		return toReturn.isEmpty() ? null : toReturn;
    }
    
	public static boolean solid(Block b) {
		return b == null || b.getType().equals(Material.AIR) || !b.getType().isSolid() || !b.getType().isOccluding();
	}

}
