package me.rey.core.classes.abilities.druid.axe;

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.enums.AbilityFail;
import me.rey.core.events.customevents.ability.AbilityFailEvent;
import me.rey.core.events.customevents.block.CustomBlockPlaceEvent.PlaceCause;
import me.rey.core.gui.Gui.Item;
import me.rey.core.players.User;
import me.rey.core.utils.UtilBlock;
import me.rey.core.utils.UtilMath;

public class IcePrison extends Ability {

	public HashMap<Player, HashMap<Block, Object[]>> toRestore = new HashMap<>();
	
	/* Throwing the item */
	final double throwBaseV = 0.5;
	final double throwChargeV = 0.25;
	final double throwLevelMultiplier = 0.1;

	public IcePrison() {
		super(213, "Ice Prison", ClassType.GOLD, AbilityType.AXE, 1, 5, 20.00, Arrays.asList(
				"Spawn a sphere of ice that can",
				"last up to <variable>4+l</variable> (+1) Seconds. Using",
				"Shift-Right Click will destroy all",
				"your active ice prisons.", "",
				"Energy: <variable>57-(3*l)</variable> (-3)",
				"Recharge: <variable>20-l</variable> (-1) Seconds"
				));
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		return spawnIcePrison(p, level, false);
	}
	
	@EventHandler
	public void onFail(AbilityFailEvent e) {
		if(e.getAbility() != this) return;
		if(e.getFail().equals(AbilityFail.COOLDOWN)) {
			if(e.getPlayer().isSneaking()) {
				spawnIcePrison(e.getPlayer(), e.getLevel(), true);
				e.setMessageCancelled(true);
			}
		}
	}
	
	private boolean spawnIcePrison(Player p, int level, boolean forceDestroy) {
		
		if(!toRestore.containsKey(p)) toRestore.put(p, new HashMap<Block, Object[]>());
		if(p.isSneaking() || forceDestroy) {
			if(this.toRestore.containsKey(p)) {
				
				@SuppressWarnings("unchecked")
				HashMap<Block, Object[]> blocks = (HashMap<Block, Object[]>) toRestore.get(p).clone();
				
				for(Block b : blocks.keySet()) {
					replaceBlock(p, b);
				    b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, 79);
				}
				
				if(forceDestroy || (!forceDestroy && p.isSneaking() && !blocks.isEmpty())) return true;
			}
			
			if(forceDestroy) return false;
		}
		
		me.rey.core.items.Throwable throwable = new me.rey.core.items.Throwable(new Item(Material.ICE), false);

		Vector vec = (p.getLocation().getDirection().normalize()
				.multiply(throwBaseV + (throwChargeV) * (1 + level * throwLevelMultiplier))
				.setY(p.getLocation().getDirection().getY() + 0.2));
		throwable.fire(p.getEyeLocation(), vec);

		new BukkitRunnable() {
			
			int ticks = 0;
			
			@Override
			public void run() {
				
				boolean check = me.rey.core.items.Throwable.checkForBlockCollision(throwable) != null;
				if (check) {
					Block block = throwable.getEntityitem().getLocation().getBlock();
					Location loc = block.getLocation();
					loc.setX(loc.getX() + 0.5);
					loc.setZ(loc.getZ() + 0.5);
					throwable.destroy();
		
					HashMap<Block, Double> blocks = UtilBlock.getBlocksInRadius(loc, 4.2D);
					
					for (Block cur : blocks.keySet()) {
						if (solid(cur)) {
		
							double offset = UtilMath.offset(block.getLocation(), cur.getLocation());
							if (offset >= 2.8D && offset <= 4.1) {
		
								if ((cur.getX() != block.getX()) || (cur.getZ() != block.getZ())
										|| (cur.getY() <= block.getY())) {
		
									FreezeBlock(p, cur, block, level);
								}
							}
						}
					}
					
					this.cancel();
					return;
				}
				
				if(ticks > (30 * 20)) {
					throwable.destroy();
					this.cancel();
					return;
				}
				
				ticks++;
			}
		}.runTaskTimer(Warriors.getInstance(), 0, 1);

		
		this.setCooldown(20 - (level));
		this.setEnergyCost(57 - (level * 3));
		return true;
	}

	private boolean solid(Block b) {
		return b == null || b.getType().equals(Material.AIR) || !b.getType().isSolid() || !b.getType().isOccluding();
	}
	
	@SuppressWarnings("deprecation")
	public void FreezeBlock(Player p, Block freeze, Block mid, int level){
	    if (!solid(freeze)) 
	      return;
	    
	    double time = 4 + (1 * level);
	    
	    int yDiff = freeze.getY() - mid.getY();
	    time = (time - (yDiff * 1 - Math.random() * 1D));
	    
	    restoreLater(p, freeze, Material.ICE, time);
	    freeze.getWorld().playEffect(freeze.getLocation(), Effect.STEP_SOUND, Material.ICE.getId());
	 }
	
	@SuppressWarnings("deprecation")
	public void restoreLater(Player p, Block block, Material toReplace, double time) {
		
		Material type = block == null ? Material.AIR : block.getType();
		Object[] array = new Object[2]; array[0] = type; array[1] = block.getData();
		
		HashMap<Block, Object[]> self = toRestore.get(p);
		self.put(block, array);
		
		UtilBlock.replaceBlock(PlaceCause.ABILITY, block, toReplace, (byte) 0 );
		
		toRestore.replace(p, self);
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				replaceBlock(p, block);
			}
			
		}.runTaskLater(Warriors.getInstance(), (int) (time * 20));
	}
	
	private void replaceBlock(Player p, Block block) {
		if(!toRestore.containsKey(p)) return;
		
		HashMap<Block, Object[]> self = toRestore.get(p);
		if(!self.containsKey(block)) return;
		
		Object[] objects = self.get(block);
		self.remove(block);
		
		boolean success = UtilBlock.replaceBlock(PlaceCause.ABILITY, block, (Material) objects[0], (byte) objects[1]);
		if(success) {
			Location loc = block.getLocation();
			loc.setZ(loc.getZ() + 0.5);
			loc.setX(loc.getX() + 0.5);
			
			block.getLocation().getWorld().spigot().playEffect(loc, Effect.CLOUD, 0, 0, 0F, 0F, 0F, 0F, 1, 50);
		}
		
		toRestore.replace(p, self);
		if(self.isEmpty())
			toRestore.remove(p);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		for(Player keys : this.toRestore.keySet())
			for(Block b : this.toRestore.get(keys).keySet())
				if(b.equals(e.getBlock())) {
					e.setCancelled(true);
					return;
				}
	}
	
	@EventHandler
	public void onIceMelt(BlockFadeEvent e) {
		for(Player keys : this.toRestore.keySet())
			for(Block b : this.toRestore.get(keys).keySet())
				if(b.equals(e.getBlock())) {
					e.setCancelled(true);
					return;
				}
	}

}
