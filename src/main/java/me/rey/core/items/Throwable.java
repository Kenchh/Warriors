package me.rey.core.items;

import me.rey.core.Warriors;
import me.rey.core.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class Throwable implements Listener {

    Gui.Item item;
    Item entityitem;
    boolean pickupable = false;

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
        this.entityitem = loc.getWorld().dropItem(loc, item.get());
        Vector direction = entityitem.getLocation().getDirection();
        this.entityitem.setVelocity(direction.normalize().multiply(multiplier).setY(direction.getY() + addY));
    }

    public void fire(Location loc, double multiplier, double baseY, double addY) {
        this.entityitem = loc.getWorld().dropItem(loc, item.get());
        Vector direction = entityitem.getLocation().getDirection();
        this.entityitem.setVelocity(direction.normalize().multiply(multiplier).setY(baseY + addY));
    }

    public void fire(Location loc, Vector direction, double multiplier, double addY) {
        this.entityitem = loc.getWorld().dropItem(loc, item.get());
        this.entityitem.setVelocity(direction.normalize().multiply(multiplier).setY(direction.getY() + addY));
    }

    public void fire(Location loc, Vector direction, double multiplier, double baseY, double addY) {
        this.entityitem = loc.getWorld().dropItem(loc, item.get());
        this.entityitem.setVelocity(direction.normalize().multiply(multiplier).setY(baseY + addY));
    }

    public void fire(Location loc, Vector v) {
        this.entityitem = loc.getWorld().dropItem(loc, item.get());
        this.entityitem.setVelocity(v);
    }

    public void drop(Location loc, boolean naturally) {
        if(naturally) {
            entityitem = loc.getWorld().dropItemNaturally(loc, item.get());
        } else {
            entityitem = loc.getWorld().dropItem(loc, item.get());
        }
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

    public void destroy() {
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

}
