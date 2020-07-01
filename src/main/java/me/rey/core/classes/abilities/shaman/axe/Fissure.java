package me.rey.core.classes.abilities.shaman.axe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.events.customevents.block.CustomBlockPlaceEvent.PlaceCause;
import me.rey.core.players.User;
import me.rey.core.utils.UtilBlock;
import me.rey.core.utils.UtilEnt;

public class Fissure extends Ability {
	
	public static HashMap<Block, Object[]> blockData = new HashMap<>();
	public static ArrayList<Block> unbreakable = new ArrayList<>();

	public Fissure() {
		super(513, "Fissure", ClassType.GREEN, AbilityType.AXE, 1, 5, 0.00, Arrays.asList(
				"Create a wall that on impact applies a",
				"Slowness 2 effect to enemies for <variable>2.5+(0.5*l)</variable> Seconds.",
				"",
				"If hit with fissure, enemies will take",
				"<variable>2.4+(0.4*l)</variable> damage and for every block traveled,",
				"it will deal <variable>0.8+(0.2*l)</variable> additional damage.","",
				"Energy: <variable>50-(3*l)</variable>",
				"Recharge: <variable>12-l</variable> Seconds"
				));
		
		this.setWhileInAir(false);
		this.setEnergyCost(50, 3);
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		this.setCooldown(12-level);
		
		FissureObject fissure = new FissureObject(u, p.getLocation(), p.getLocation().getYaw(), level, 2.5 + 0.5 * level);
		fissure.start();
		
		return true;
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		cancel(e, e.getBlock());
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(e.getClickedBlock() == null) return;
		cancel(e, e.getClickedBlock());
	}
	
	private void cancel(Cancellable c, Block block) {
		if(!unbreakable.contains(block)) return;
		c.setCancelled(true);
	}
	
	class FissureObject {
		
		private static final int fissureLength = 14;
		
		private int maxHeight = 1;
		private int blockIndex = 1;
		private int blockHeightIndex = 1;
		
		private final int level;
		private final double degree; // yaw
		private final User owner;
		private double originY;
		
		private double slownessSeconds;
		private Location updatableLoc, origin;
		
		public FissureObject(User owner, Location origin, double degree, int level, double slownessSeconds) {
			this.owner = owner;
			this.origin = origin;
			this.updatableLoc = origin;
			this.originY = origin.getBlockY();
			this.degree = degree + 90D;
			this.level = level;
			this.slownessSeconds = slownessSeconds;

		}
		
		public void start() {
			
			new BukkitRunnable() {
				
				@SuppressWarnings("deprecation")
				@Override
				public void run() {
					
					if(blockIndex >= fissureLength && blockHeightIndex > maxHeight) {
						this.cancel();
						return;
					}
					
					if(blockIndex == 3) maxHeight = 2;
					else if (blockIndex == 5) maxHeight = 3;
					
					if (blockHeightIndex > maxHeight) {
						blockHeightIndex = 1;
						blockIndex++;
					}
					
					double z = UtilBlock.getXZCordsFromDegree(origin, blockIndex, degree)[1];
					double x = UtilBlock.getXZCordsFromDegree(origin, blockIndex, degree)[0];
					Block found = new Location(origin.getWorld(), x, originY, z).getBlock();
					Block lowestFromFound = UtilBlock.getLowestBlockFrom(found);
					
					int yDiff = found.getY() - lowestFromFound.getY();
					
					if (yDiff-1 > 2 || yDiff+1 < -2) {
						this.cancel();
						return;
					}
					
					stack(lowestFromFound, yDiff - 1 + blockHeightIndex, lowestFromFound.getType(), lowestFromFound.getData());
					
					Iterator<Entity> ents = UtilBlock.getEntitiesInCircle(updatableLoc, 0.15).iterator();
					while(ents.hasNext()) {
						Entity next = ents.next();
						
						if(!(next instanceof LivingEntity)) continue;
						LivingEntity ent = (LivingEntity) next;
						
						if (owner.getTeam().contains(ent)) continue;
						
						double dmg = 2.4 * (0.4 * level) + ((0.8 + (0.2 * level)) * blockIndex);
						UtilEnt.damage(dmg, getName(), ent, owner.getPlayer());
						ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) Math.round(slownessSeconds * 20), 1, false, false));
					}
					
					blockHeightIndex++;
				}
				
			}.runTaskTimer(Warriors.getInstance(), 0, 1);
			
		}
		
		@SuppressWarnings("deprecation")	
		private boolean stack(Block start, int height, Material mat, byte data) {
			Set<Block> toReplace = new HashSet<Block>();
			int y = start.getY(), x = start.getX(), z = start.getZ();
			
			for (int i = 1; i < height+1; i++) {
				Block found = new Location(origin.getWorld(), x, y+i, z).getBlock();
				if (UtilBlock.solid(found) && !Fissure.unbreakable.contains(found)) return false;
				toReplace.add(found);
			}
			
			for (Block b : toReplace) {
				if (unbreakable.contains(b) || blockData.containsKey(b)) continue;
				
				Object[] array = {b.getType(), b.getData()};
				blockData.put(b, array);
				
				UtilBlock.replaceBlock(PlaceCause.ABILITY, b, mat, data);
				b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getTypeId());
				Fissure.unbreakable.add(b);
				
				new BukkitRunnable() {
					@Override
					public void run() {
						UtilBlock.replaceBlock(PlaceCause.ABILITY, b, (Material) blockData.get(b)[0], (byte) blockData.get(b)[1]);
						blockData.remove(b);
						Fissure.unbreakable.remove(b);
					}
				}.runTaskLater(Warriors.getInstance(), fissureLength * 20);
			}
			
			return true;
		}
		
	}

}
