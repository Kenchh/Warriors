package me.rey.core.classes.abilities.druid.passive_a;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.classes.abilities.IDamageTrigger;
import me.rey.core.effects.ParticleEffect;
import me.rey.core.energy.EnergyHandler;
import me.rey.core.enums.State;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.events.customevents.update.UpdateEvent;
import me.rey.core.players.User;
import me.rey.core.utils.UtilBlock;
import me.rey.core.utils.UtilParticle;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class ArcticZone extends Ability implements IConstant, IConstant.ITogglable, IDamageTrigger.IPlayerDamagedEntity {

    private EnergyHandler handler = new EnergyHandler();
    private final double energyPerSecond = 12;

    public ArcticZone() {
        super(232, "Arctic Zone", ClassType.GOLD, AbilityType.PASSIVE_A, 1, 3, 0.00, Arrays.asList(
                "Freeze the ground by radiating an arctic circle",
                "",
                "You receive Resistance II and give your allies",
                "Resistance I when in zone.",
                "",
                "Enemies attacked in zone receive Slowness I until",
                "they are outside the circle.",
                "",
                "The zone has a radius of <variable>3+l</variable> blocks.",
                "",
                "Energy: <variable>12-l</variable> Per Second."
        ));
        this.setEnergyCost(energyPerSecond / 20);
        this.setIgnoresCooldown(true);
        this.setInLiquid(true);
    }

    public HashMap<UUID, ArrayList<UUID>> slowedByArctic = new HashMap<UUID, ArrayList<UUID>>();
    public HashMap<UUID, ArrayList<Block>> frozenblocks = new HashMap<>();
    public HashMap<UUID, ArrayList<Block>> frozenflowblocks = new HashMap<>();

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {
        Object arg = conditions[0];
        this.setEnergyCost((energyPerSecond-level) / 20);

        double radius = 3+level;

        if(arg != null && arg instanceof UpdateEvent) {

            // Consuming energy
            if(!this.getEnabledPlayers().contains(p.getUniqueId())) return false;
            handler.togglePauseEnergy(State.ENABLED, p.getUniqueId());

            for(Location loc : UtilBlock.circleLocations(p.getLocation(), radius, 5)) {
                loc.getWorld().spigot().playEffect(loc, Effect.SNOW_SHOVEL, 0, 0, 0, 0, 0, 1, 0, 30);
            }

            /* Giving user Resistance II */
            p.getWorld().playSound(p.getLocation(), Sound.AMBIENCE_RAIN, 1F, 0.5F);
            p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 5, 1));

            /* Giving allies Resistance I */
            for(Entity e : UtilBlock.getEntitiesInCircle(p.getLocation(), radius)) {
                if(e instanceof Player) {
                    Player a = (Player) e;

                    if(a.getName().equals(p.getName())) continue;

                    if(u.getTeam().contains(a)) {
                        a.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 5, 0));
                    }
                }
            }

            /* Freezing water */
            for(Block b : UtilBlock.getBlocksInRadius(new Location(p.getWorld(), p.getLocation().getX(), p.getLocation().getY() - 3.0, p.getLocation().getZ()), radius, 3).keySet()) {
                if(b.getType() == Material.WATER || b.getType() == Material.STATIONARY_WATER) {
                    if(b.getData() == 0) {
                        if(frozenblocks.containsKey(p.getUniqueId())) {
                            if(frozenblocks.get(p.getUniqueId()) != null) {
                                if (frozenblocks.get(p.getUniqueId()).contains(b) == false) {
                                    frozenblocks.get(p.getUniqueId()).add(b);
                                }
                            }
                        } else {
                            ArrayList<Block> bb = new ArrayList<Block>();
                            bb.add(b);
                            frozenblocks.put(p.getUniqueId(), bb);
                        }
                    } else {
                        if(frozenflowblocks.containsKey(p.getUniqueId())) {
                            if (frozenflowblocks.get(p.getUniqueId()).contains(b) == false) {
                                frozenflowblocks.get(p.getUniqueId()).add(b);
                            }
                        } else {
                            ArrayList<Block> bb = new ArrayList<Block>();
                            bb.add(b);
                            frozenflowblocks.put(p.getUniqueId(), bb);
                        }
                    }

                    b.setType(Material.ICE);
                }
            }

            ArrayList<Block> toremove = new ArrayList<>();
            ArrayList<Block> toremoveflow = new ArrayList<>();

            if(frozenblocks.get(p.getUniqueId()) != null) {
                for (Block b : frozenblocks.get(p.getUniqueId())) {
                    if (UtilBlock.getBlocksInRadius(new Location(p.getWorld(), p.getLocation().getX(), p.getLocation().getY() - 3.0, p.getLocation().getZ()), radius, 3).containsKey(b) == false) {
                        toremove.add(b);
                    }
                }
            }

            if(frozenflowblocks.get(p.getUniqueId()) != null) {
                for (Block b : frozenflowblocks.get(p.getUniqueId())) {
                    if (UtilBlock.getBlocksInRadius(new Location(p.getWorld(), p.getLocation().getX(), p.getLocation().getY() - 3.0, p.getLocation().getZ()), radius, 3).containsKey(b) == false) {
                        toremoveflow.add(b);
                    }
                }
            }

            for(Block b : toremove) {
                if(b.getType() == Material.ICE) {
                    b.setType(Material.WATER);
                }
                frozenblocks.get(p.getUniqueId()).remove(b);
            }

            for(Block b : toremoveflow) {
                if(b.getType() == Material.ICE) {
                    b.setType(Material.AIR);
                }
                frozenflowblocks.get(p.getUniqueId()).remove(b);
            }

            /* Keep giving slowness to enemies in zone */
            if(slowedByArctic.containsKey(p.getUniqueId())) {

                ArrayList<UUID> slowedplayers = slowedByArctic.get(p.getUniqueId());
                ArrayList<UUID> playerstoremove = new ArrayList<UUID>();

                for(UUID uu : slowedplayers) {

                    try {
                        Player sp = Bukkit.getPlayer(uu);
                        if(UtilBlock.getEntitiesInCircle(p.getLocation(), radius).contains(sp) == false) {
                            playerstoremove.add(uu);
                        } else {
                            sp.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5, 0));
                        }

                    } catch (NullPointerException e) {
                        playerstoremove.add(uu);
                    }
                }

                for(UUID uu : playerstoremove)
                    slowedplayers.remove(uu);


                playerstoremove.clear();
                slowedByArctic.replace(p.getUniqueId(), slowedplayers);

            }

            return true;
        }

        if(arg != null && arg instanceof DamageEvent) {
            LivingEntity damaged = ((DamageEvent) arg).getDamagee();

            if(!(damaged instanceof Player)) return false;

            if(damaged.equals(p)) return false;

            /* Checking if enemy is in zone and giving slowness */
            if(UtilBlock.getEntitiesInCircle(p.getLocation(), radius).contains(damaged)) {
                if(slowedByArctic.get(p.getUniqueId()).contains(damaged.getUniqueId()) == false) {
                    slowedByArctic.get(p.getUniqueId()).add(damaged.getUniqueId());
                }
                damaged.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5, 0));
            }
            return true;
        }


        return true;
    }

    @Override
    public boolean off(Player p) {
        handler.togglePauseEnergy(State.DISABLED, p.getUniqueId());
        slowedByArctic.remove(p.getUniqueId());
        if(frozenblocks.containsKey(p.getUniqueId())) {
            for (Block b : frozenblocks.get(p.getUniqueId())) {
                if (b.getType() == Material.ICE) {
                    b.setType(Material.WATER);
                }
            }
            frozenblocks.remove(p.getUniqueId());
        }
        if(frozenflowblocks.containsKey(p.getUniqueId())) {
            for(Block fb : frozenflowblocks.get(p.getUniqueId())) {
                if (fb.getType() == Material.ICE) {
                    fb.setType(Material.AIR);
                }
            }
            frozenflowblocks.remove(p.getUniqueId());
        }
        return true;
    }

    @Override
    public boolean on(Player p) {
        handler.togglePauseEnergy(State.ENABLED, p.getUniqueId());
        slowedByArctic.put(p.getUniqueId(), new ArrayList<UUID>());
        return true;
    }
}
