package me.rey.core.classes.abilities.shaman.passive_a;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.google.common.collect.ImmutableMap;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.classes.abilities.IConstant.ITogglable;
import me.rey.core.classes.abilities.IDamageTrigger.IPlayerDamagedEntity;
import me.rey.core.effects.ParticleEffect;
import me.rey.core.effects.SoundEffect;
import me.rey.core.energy.EnergyHandler;
import me.rey.core.enums.State;
import me.rey.core.events.customevents.block.CustomBlockPlaceEvent.PlaceCause;
import me.rey.core.events.customevents.combat.CustomDamageEvent;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.events.customevents.update.UpdateEvent;
import me.rey.core.gui.Gui.Item;
import me.rey.core.packets.Title;
import me.rey.core.players.User;
import me.rey.core.utils.ChargingBar;
import me.rey.core.utils.UtilBlock;
import me.rey.core.utils.Utils;

public class Aromatherapy extends Ability implements IConstant, ITogglable, IPlayerDamagedEntity {
	
	public HashMap<Player, HashMap<Block, Object[]>> toRestore = new HashMap<>();
	
	private final Map<Material, Integer> FAKE_BLOCKS_FLORA = ImmutableMap.of(
			Material.LONG_GRASS, 1,
			Material.RED_ROSE, 0,
			Material.YELLOW_FLOWER, 0
			);

	private final List<Item> FAKE_ITEMS_TO_SPAWN = new ArrayList<>(Arrays.asList(
			new Item(Material.YELLOW_FLOWER),
			new Item(Material.RED_ROSE),
			new Item(Material.LONG_GRASS).setDurability(1),
			new Item(Material.DOUBLE_PLANT),
			new Item(Material.DOUBLE_PLANT).setDurability(2),
			new Item(Material.DOUBLE_PLANT).setDurability(3),
			new Item(Material.RED_ROSE).setDurability(1)
			));
	
	private EnergyHandler handler = new EnergyHandler();
	private HashMap<Player, Integer> ticks = new HashMap<>();
	private HashMap<Player, Integer> blossomCharge = new HashMap<>();
	private HashMap<Player, Set<LivingEntity>> healing = new HashMap<Player, Set<LivingEntity>>();
	private Set<Player> onBlossomCooldown = new HashSet<>();
	
	final static double minHeartsToTransfer = 7.0D;
	final static double blossomCooldown = 20.0D;
	final String BLOSSOM = "Blossom";
	
	public Aromatherapy() {
		super(531, "Aromatherapy", ClassType.GREEN, AbilityType.PASSIVE_A, 1, 3, 0.00, Arrays.asList(
				"Transfer <variable>1+(0.5*l)</variable> (+0.5) health to your nearest ally",
				"under " + minHeartsToTransfer + " hearts. When activated, you will",
				"gain Regeneration 1 and",
				"lifesteal upon hitting enemies for <variable>0.25+(0.25*l)</variable> (+0.25) health",
				"",
				"Holding shift for <variable>8-l</variable> (-1) Seconds will enable Blossom.",
				"Blossom gives Regeneration 2 to all nearby allies",
				"and gives the user a Regeneration 3 and Speed 1",
				"effect. Blossom disables all buffs from",
				"Aromatherapy", "",
				"Aromatherapy can only be used above 7 hearts.",
				"Blossom can only be used below 7 hearts.",
				"",
				"Aromatherapy Energy: <variable>10-l</variable> (-1)",
				"Blossom Recharge: " + blossomCooldown + " Seconds"
				));
		
		this.setIgnoresCooldown(true);
		this.setEnergyCost(1);
	}

	@Override
	public boolean off(Player p) {
		handler.togglePauseEnergy(State.DISABLED, p.getUniqueId());
		p.removePotionEffect(PotionEffectType.REGENERATION);
		this.ticks.remove(p);
		return true;
	}

	@Override
	public boolean on(Player p) {
		if(this.onBlossomCooldown.contains(p)) {
			this.off(p);
			this.toggle(p, State.DISABLED);
			return true;
		}
		
		handler.togglePauseEnergy(State.ENABLED, p.getUniqueId());
		p.removePotionEffect(PotionEffectType.REGENERATION);
		p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 100000, 0, false, false));
		this.reset(p);
		return true;
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		Object arg = conditions[0];
		
		if(arg != null) { 
				
			if(arg instanceof UpdateEvent) {
				
				if(!toRestore.containsKey(p)) toRestore.put(p, new HashMap<Block, Object[]>());
				
				/*
				 * AROMATHERAPY
				 */
					
					// Consuming energy
					if(!this.getEnabledPlayers().contains(p.getUniqueId())) return false;
					handler.togglePauseEnergy(State.ENABLED, p.getUniqueId());
					
					// Checking for the seconds
					int ticksPassed = this.ticks.get(p);
					double intervalSeconds = 1;
					
					// Spawning effects/items
					if(ticksPassed % (intervalSeconds * 20 / 4) == 0) {
						// Particles on player
						if(ticksPassed > (intervalSeconds * 20 / 2)) spawnFlora(p, p.getLocation(), level, 2+level);
						
						// Items
						double secondsToDespawn = 1.5;
						me.rey.core.items.Throwable flower = new me.rey.core.items.Throwable(this.FAKE_ITEMS_TO_SPAWN.get(new Random().nextInt(this.FAKE_ITEMS_TO_SPAWN.size())), false);
						flower.drop(p.getLocation(), true);
						
						new BukkitRunnable() {
							@Override
							public void run() {
								flower.destroy();
							}
						}.runTaskLaterAsynchronously(Warriors.getInstance(), (int) (20 * secondsToDespawn));
					}
					
					// Sucessfully healed
					if(ticksPassed >= intervalSeconds * 20) {
						double healthToTransfer = 1 + (0.5 * level);
						double radius = 6 + (3 * level);
						
						Iterator<Entity> nearby = p.getNearbyEntities(radius, 5, radius).iterator();
						if(p.getHealth() > minHeartsToTransfer * 2) {
							while(nearby.hasNext()) {
								Entity next = nearby.next();
								if(!(next instanceof LivingEntity)) continue;
								
								LivingEntity ent = (LivingEntity) next;
								if (this.getPlayerHealing(p).contains(ent)) continue;
								
								// are teamed
								if(u.getTeam().contains(ent)) {
									if (p == ent || p.getHealth() < minHeartsToTransfer * 2) continue;
									if (ent.getHealth() + healthToTransfer > ent.getMaxHealth()) continue;
									
									
									final int secondsIntervalTicks = (int) (20*0.1);
									final Location initial = p.getEyeLocation().clone();
									addToHealing(p, ent);
									new BukkitRunnable() {
										
										Location heartLoc = initial.clone();
										final double particleSeparation = 0.2, hitbox = 0.3, offset = 0.8;
										
										@Override
										public void run() {
											if (ent == null || ent.isDead() || (ent instanceof Player && !((Player) ent).isOnline())) {
												this.cancel();
												return;
											}
											
											if (ent.getLocation().distance(initial) > radius) {
												this.cancel();
												return;
											}
											
											Vector pvector = Utils.getDirectionBetweenLocations(heartLoc, ent.getLocation());
											pvector.multiply(particleSeparation);
											heartLoc.add(pvector);
											Location toSpawn = heartLoc.clone();
											toSpawn.setY(toSpawn.getY() + offset);
											
											new ParticleEffect(Effect.HEART).play(toSpawn);
											
											if (toSpawn.clone().subtract(new Location(toSpawn.getWorld(), 0, offset, 0)).distance(ent.getLocation()) <= hitbox) {
												if (p.getHealth() >= minHeartsToTransfer * 2) {
													ent.setHealth(Math.min(ent.getHealth() + healthToTransfer, ent.getMaxHealth()));
													p.setHealth(Math.max(0, Math.min(p.getMaxHealth(), p.getHealth() - healthToTransfer)));
												}
												removeFromHealing(p, ent);
												this.cancel();
												return;
											}
												
											pvector.normalize();
											
										}
									}.runTaskTimerAsynchronously(Warriors.getInstance(), 0, secondsIntervalTicks);
									
								}
								
							}
						}
						
						this.reset(p);
					}
		
					// Consume energy
					int energyPerSecond = 10 - level;
					this.setEnergyCost(energyPerSecond / 20D);
					
					// Updating their in the ticks map
					ticks.replace(p, ticks.get(p)+1);
					
				}
				
	
				if(arg instanceof DamageEvent) {
					if(this.onBlossomCooldown.contains(p)) return false;
					
					LivingEntity damager = ((CustomDamageEvent) arg).getDamager();
					if(!(damager instanceof Player) || !this.getEnabledPlayers().contains(((Player) damager).getUniqueId())) return false;
					
					double lifesteal = 0.25 + (0.25 * level);
					damager.setHealth(Math.min(damager.getHealth() + lifesteal, damager.getMaxHealth()));
				}
			
			}
		
		return true;
	}
	
	/*
	 * BLOSSOM
	 */
	@EventHandler
	public void onBlossom(UpdateEvent e) {
		for(Player p : Bukkit.getOnlinePlayers()) {
			
			if(!new User(p).isUsingAbility(this)) continue;
			
			if((!this.blossomCharge.containsKey(p)) || this.onBlossomCooldown.contains(p)) continue;
			
			if(!toRestore.containsKey(p)) toRestore.put(p, new HashMap<Block, Object[]>());
			
			if(p.isSneaking() && !blossomCharge.containsKey(p))
				blossomCharge.put(p, 0);
			
			if(p.isSneaking()) {
				blossomCharge.replace(p, blossomCharge.get(p)+1);
			} else {
				if(this.blossomCharge.containsKey(p)) new SoundEffect(Sound.NOTE_PLING, 0.5F).play(p);
				blossomCharge.remove(p);
			}
			
			if(!this.blossomCharge.containsKey(p)) continue;
			
			int level = new User(p).getSelectedBuild(this.getClassType()).getAbilityLevel(this.getAbilityType());
			int selfCharge = this.blossomCharge.get(p);
			double chargeSeconds = 8 - level, neededCharge = chargeSeconds * 20;
			double floraRadius = 6+level/2;
			
			if(selfCharge <= neededCharge) {
				// DISPLAY COOLDOWN
				double percentage = selfCharge * 100 / neededCharge;
				
				int bars = ChargingBar.TITLE_BARS;
				ChargingBar bar = new ChargingBar(bars, percentage);
				Title.getChargingBar("", bar).send(p);
				
				double x = (double) ((selfCharge / 20D) * 100 / chargeSeconds), y = x % 5;
				if(y == 0) new SoundEffect(Sound.NOTE_PLING, 0.1F * bar.getChargeBars()).play(p);
				
				// PARTICLES
				ParticleEffect happy = new ParticleEffect(Effect.HAPPY_VILLAGER).setOffset(0.4F, 1F, 0.4F).setParticleCount(5);
				happy.play(p.getLocation());
				
				// FLORA SPAWN
				this.spawnFlora(p, p.getLocation(), level, floraRadius);
			} else {
				
				this.blossomCharge.remove(p);
				
				// Particles
				double particleInterval = 0.4;
				new BukkitRunnable() {					
					final double radiusIncrement = 0.8;
					final int maxCircles = 3;
					double radius = 0;
					
					@Override
					public void run() {
						radius += radiusIncrement;
						ArrayList<Location> circle = UtilBlock.circleLocations(p.getLocation(), radius, 1);
						for(Location point : circle) new ParticleEffect(Effect.SNOW_SHOVEL).play(point);						
						
						if(radius != 0 && radius > radiusIncrement * maxCircles) {
							this.cancel();
							return;
						}
					}
				}.runTaskTimer(Warriors.getInstance(), 0, (int) Math.round(20 * particleInterval));
				
				this.sendUsedMessageToPlayer(p, this.BLOSSOM);
			    new SoundEffect(Sound.AMBIENCE_THUNDER, 2.0F).play(p.getLocation());
			    new SoundEffect(Sound.CREEPER_HISS, 0.5F).play(p.getLocation());
				
				this.onBlossomCooldown.add(p);
				
				new BukkitRunnable() {
					
					@Override
					public void run() {
						onBlossomCooldown.remove(p);
						sendReadyMessageToPlayer(p, BLOSSOM);
					}
					
				}.runTaskLater(Warriors.getInstance(), (int) (20 * blossomCooldown));
				
				/*
				 * BUFFS
				 */
				p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * (3 + level), 2, false, false));
				p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * (5 + level), 0, false, false));
				
				Iterator<Entity> ents = UtilBlock.getEntitiesInCircle(p.getLocation(), floraRadius).iterator();
				while(ents.hasNext()) {
					Entity found = ents.next();
					if(!(found instanceof Player)) continue;
					
					if(new User(p).getTeam().contains((Player) found)) {
						((LivingEntity) found).addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * (5 + level), 1, false, false));
					}
					
				}
				
				
				this.off(p);
				this.toggle(p, State.DISABLED);
			}
			
		}
	}
	
	private void reset(Player p) {
		if(this.ticks.containsKey(p))
			this.ticks.replace(p, 0);
		else
			this.ticks.put(p, 0);
	}
	
	private void spawnFlora(Player p, Location loc, int level, double radius) {
		HashMap<Block, Double> near = UtilBlock.getBlocksInRadius(loc, radius + 0.5D);
		
		for(Block b : near.keySet()) {
			double offset = near.get(b);
			
			// Is in radius
			if(offset < radius + 0.2D) {
				// Checking if grass & if above is air
				if(!b.getType().equals(Material.GRASS)) continue;
				
				if(new Random().nextInt(5) < 1) continue;
				this.SetBlock(p, b.getRelative(BlockFace.UP), loc.getBlock(), level);
			}
			
		}

	}
	
	public void SetBlock(Player p, Block freeze, Block mid, int level){
	    if (freeze == null || !freeze.getType().equals(Material.AIR)) 
	      return;
	    
	    double time = 2;
	    
	    List<Entry<Material, Integer>> entries = new ArrayList<>(this.FAKE_BLOCKS_FLORA.entrySet());
	    int index = new Random().nextInt(entries.size()+5);
	    Entry<Material, Integer> item = entries.get(index > entries.size()-1 ? 0 : index);
	    restoreLater(p, freeze, item.getKey(), item.getValue(), time);
	 }
	
	@SuppressWarnings("deprecation")
	public void restoreLater(Player p, Block block, Material toReplace, int data, double time) {
		
		Material type = block == null ? Material.AIR : block.getType();
		Object[] array = new Object[2]; array[0] = type; array[1] = block.getData();
		
		HashMap<Block, Object[]> self = toRestore.get(p);
		self.put(block, array);
		
		UtilBlock.replaceBlock(PlaceCause.ABILITY, block, toReplace, (byte) data );
		
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
		
		UtilBlock.replaceBlock(PlaceCause.ABILITY, block, (Material) objects[0], (byte) objects[1]);
		
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
	
	private Set<LivingEntity> getPlayerHealing(Player p){
		return this.healing.containsKey(p) ? this.healing.get(p) : new HashSet<LivingEntity>();
	}
	
	private void addToHealing(Player p, LivingEntity le) {
		Set<LivingEntity> cur = this.getPlayerHealing(p);
		cur.add(le);
		
		if(!this.healing.containsKey(p))
			this.healing.put(p, cur);
		else
			this.healing.replace(p, cur);
	}
	
	private void removeFromHealing(Player p, LivingEntity le) {
		Set<LivingEntity> cur = this.getPlayerHealing(p);
		cur.remove(le);
		
		if(!this.healing.containsKey(p))
			this.healing.put(p, cur);
		else
			this.healing.replace(p, cur);
	}

}
