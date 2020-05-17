package me.rey.core.events.customevents.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CustomBlockPlaceEvent extends Event implements Cancellable {

    private PlaceCause cause;
    private Block oldBlock;
    private Material replacementBlock;
    private byte data;
    private boolean isCancelled;

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    public enum PlaceCause {
        ABILITY, OTHER
    }

    public CustomBlockPlaceEvent(PlaceCause cause, Block old, Material replace, byte data) {
        this.cause = cause;
        this.oldBlock = old;
        this.replacementBlock = replace;
        this.data = data;
    }

    public PlaceCause getCause() {
        return cause;
    }

    public Block getOldBlock() {
        return oldBlock;
    }

    public Material getReplacementMaterial() {
        return replacementBlock;
    }
    
    public byte getData() {
    	return data;
    }

    public static HandlerList getHANDLERS() {
        return HANDLERS;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
