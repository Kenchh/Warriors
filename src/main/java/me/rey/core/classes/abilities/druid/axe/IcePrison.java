package me.rey.core.classes.abilities.druid.axe;

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.gui.Gui.Item;
import me.rey.core.players.User;
import me.rey.core.utils.UtilBlock;
import me.rey.core.utils.UtilMath;

public class IcePrison extends Ability {

	public HashMap<Player, HashMap<Block, Material>> toRestore = new HashMap<>();
	
	/* Throwing the item */
	final double throwBaseV = 0.5;
	final double throwChargeV = 0.5;
	final double throwLevelMultiplier = 0.25;

	public IcePrison() {
		super(213, "Ice Prison", ClassType.GOLD, AbilityType.AXE, 1, 5, 20.00, Arrays.asList(""));

		this.setEnergyCost(57);
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		me.rey.core.items.Throwable throwable = new me.rey.core.items.Throwable(new Item(Material.ICE), false);

		Vector vec = (p.getLocation().getDirection().normalize()
				.multiply(throwBaseV + (throwChargeV) * (1 + level * throwLevelMultiplier))
				.setY(p.getLocation().getDirection().getY() + 0.2));
		throwable.fire(p.getEyeLocation(), vec);
		org.bukkit.entity.Item ei = throwable.getEntityitem();

		new BukkitRunnable() {
			
			int ticks = 0;
			
			@Override
			public void run() {
				
				boolean check = checkForCollision(throwable);
				if (check) {
					Block block = throwable.getEntityitem().getLocation().getBlock();
					Location loc = block.getLocation();
					loc.setX(loc.getX() + 0.5);
					loc.setZ(loc.getZ() + 0.5);
					throwable.destroy();
		
					HashMap<Block, Double> blocks = UtilBlock.getBlocksInRadius(loc, 4.2D);
					
					for (Block cur : blocks.keySet()) {
						if (air(cur)) {
		
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

		return true;
	}

	private boolean checkForCollision(me.rey.core.items.Throwable ice) {

		if (ice == null)
			return false;

		double offset = 1;

		Block self = ice.getEntityitem().getLocation().getBlock();

		Block bOne = self.getRelative(BlockFace.UP), bTwo = self.getRelative(BlockFace.DOWN);
		Block bThree = self.getRelative(BlockFace.WEST), bFour = self.getRelative(BlockFace.EAST);
		Block bFive = self.getRelative(BlockFace.NORTH), bSix = self.getRelative(BlockFace.SOUTH);

		return !air(bOne) || !air(bTwo) || !air(bThree) || !air(bFour) || !air(bFive) || !air(bSix);
	}

	private boolean air(Block b) {
		return b == null || b.getType().equals(Material.AIR);
	}
	
	public void FreezeBlock(Player p, Block freeze, Block mid, int level){
	    if (!air(freeze)) 
	      return;
	    
	    double time = 4000 + 1000 * level;
	    
	    int yDiff = freeze.getY() - mid.getY();
	    
	    time = (time - (yDiff * 1000 - Math.random() * 1000.0D));
	    
	    restoreLater(p, freeze, Material.ICE, time);
	    freeze.getWorld().playEffect(freeze.getLocation(), Effect.STEP_SOUND, 79);
	 }
	
	public void restoreLater(Player p, Block block, Material toReplace, double time) {
		
		Material type = block == null ? Material.AIR : block.getType();
		HashMap<Block, Material> self = toRestore.containsKey(p) ? toRestore.get(p) : toRestore.put(p, new HashMap<>());
		self.put(block, type);
		block.setType(toReplace);
		toRestore.replace(p, self);
		
		new BukkitRunnable() {
			
			@Override
			public void run() {

				HashMap<Block, Material> self = toRestore.containsKey(p) ? toRestore.get(p) : toRestore.put(p, new HashMap<>());
				self.remove(block);
				block.setType(type);
				toRestore.replace(p, self);
			}
		}.runTaskLater(Warriors.getInstance(), (int) (time/1000 * 20));
	}

}
