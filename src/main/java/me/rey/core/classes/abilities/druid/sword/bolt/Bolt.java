package me.rey.core.classes.abilities.druid.sword.bolt;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.effects.SoundEffect;
import me.rey.core.events.customevents.update.UpdateEvent;
import me.rey.core.gui.Gui;
import me.rey.core.packets.ActionBar;
import me.rey.core.players.User;
import me.rey.core.utils.ChargingBar;
import me.rey.core.utils.UtilBlock;
import me.rey.core.utils.UtilEnt;
import me.rey.core.utils.UtilParticle;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Bolt extends Ability implements IConstant {

    public Bolt() {
        super(201, "Bolt", ClassType.GOLD, AbilityType.SWORD, 1, 5, 0.0, Arrays.asList(
                "Charge up an electric beam, piercing and dealing ",
                "<variable>4.5+0.5*l</variable> (+0.5) + stacks/2 damage to all enemies hit by it.",
                "",
                "Every time an enemy is hit, a stack is gained up to ",
                "a maximum of 5 stacks.",
                "",
                "When a beam doesn't connect with any target, your",
                "stacks will decay over time or will disappear",
                "upon missing a fired bolt.",
                "",
                "Energy: <variable>39-l*3</variable> (-3)"
        ));
        setIgnoresCooldown(true);
    }

    public HashMap<UUID, BoltProfile> bolts = new HashMap<UUID, BoltProfile>();
    public HashMap<UUID, Integer> stacks = new HashMap<UUID, Integer>();
    public HashMap<UUID, Long> stackdecay = new HashMap<>();
    public ArrayList<UUID> cooldown = new ArrayList<>();

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {

        if(conditions != null && conditions.length == 1 && conditions[0] != null && conditions[0] instanceof UpdateEvent) {

            if(stacks.containsKey(p.getUniqueId()) == false) {
                stacks.put(p.getUniqueId(), 1);
            }

            int maxstacks;

            if(level < 4) {
                maxstacks = 2 + level;
            } else {
                maxstacks = 5;
            }

            if(bolts.containsKey(p.getUniqueId()) == false && this.matchesAbilityTool(this.match(p.getItemInHand() == null ? new Gui.Item(Material.AIR).get() : p.getItemInHand())))
                showStacks(maxstacks, p);

            if(stacks.get(p.getUniqueId()) == 1 && !p.isBlocking())
                return false;

            if(stackdecay.containsKey(p.getUniqueId()) == false && !p.isBlocking()) {
                return false;
            }

            if(stackdecay.containsKey(p.getUniqueId())) {
                if (stackdecay.get(p.getUniqueId()) <= 0) {
                    if (stacks.get(p.getUniqueId()) > 1) {
                        stacks.replace(p.getUniqueId(), stacks.get(p.getUniqueId()) - 1);
                    }
                    if (stacks.get(p.getUniqueId()) <= 1) {
                        stackdecay.remove(p.getUniqueId());
                    } else {
                        stackdecay.replace(p.getUniqueId(), 20L);
                    }
                } else {
                    stackdecay.replace(p.getUniqueId(), stackdecay.get(p.getUniqueId()) - 1);
                    if (stackdecay.get(p.getUniqueId()) % 5 == 0 && stacks.get(p.getUniqueId()) >= maxstacks) {
                        SoundEffect.playCustomSound(p.getLocation(), "lightningboltoverload", 1F, 1F + 0.2F*(stacks.get(p.getUniqueId())-1));
                    }
                }
            }

            if(!p.isBlocking())
                return false;
        }

        if(bolts.containsKey(p.getUniqueId()) || cooldown.contains(p.getUniqueId()))
            return false;

        if(bolts.containsKey(p.getUniqueId()) == false) {
            BoltProfile bolt = new BoltProfile(p, u, level);
            bolts.put(p.getUniqueId(), bolt);
        }

        BoltProfile bolt = bolts.get(p.getUniqueId());

        if(bolt.stopcharge)
            return false;

        cooldown.add(p.getUniqueId());

        new BukkitRunnable() {
            @Override
            public void run() {
                if(p.isBlocking()) {
                    if(bolt.charge < bolt.maxcharge) {
                        bolt.charge++;
                        p.playSound(p.getLocation(), Sound.CREEPER_HISS, 1F, 0.25F + 2.5F * (float) (bolt.charge/bolt.maxcharge));
                        new ActionBar(new ChargingBar(ChargingBar.ACTIONBAR_BARS, bolt.maxcharge - bolt.charge + 1, bolt.maxcharge).
                                getBarString().replace(ChatColor.GREEN + "", ChatColor.YELLOW + "").replace(ChatColor.RED + "", ChatColor.WHITE + "")).send(p);

                        u.consumeEnergy((39-level*3)/bolt.maxcharge);

                    } else {
                        bolt.shoot();

                        startCooldown(p);

                        bolts.remove(p.getUniqueId());
                        this.cancel();
                    }
                } else {
                    bolt.shoot();

                    startCooldown(p);

                    bolts.remove(p.getUniqueId());
                    this.cancel();
                }
            }
        }.runTaskTimer(Warriors.getInstance(), 1L, 1L);

        return false;
    }

    private void startCooldown(Player p) {
        new BukkitRunnable() {
            @Override
            public void run() {
                p.playSound(p.getLocation(), Sound.NOTE_PLING, 1F, 2F);
                cooldown.remove(p.getUniqueId());
            }
        }.runTaskLater(Warriors.getInstance(), (long) (0.5*20));
    }

    private void showStacks(int maxstacks, Player p) {

        String stackstring = "";
        String nonstackedballs = "○○";
        String stackedballs = "●●";

        for(int i=1;i<=maxstacks-1; i++) {
            if(i == 1) {
                if(stacks.get(p.getUniqueId())-1 >= 1) {
                    stackstring += ChatColor.YELLOW + stackedballs;
                } else {
                    stackstring += ChatColor.YELLOW + nonstackedballs;
                }
            }

            if(i == 2) {
                if(stacks.get(p.getUniqueId())-1 >= 2) {
                    stackstring += ChatColor.GOLD + stackedballs;
                } else {
                    stackstring += ChatColor.GOLD + nonstackedballs;
                }
            }

            if(i == 3) {
                if(stacks.get(p.getUniqueId())-1 >= 3) {
                    stackstring += ChatColor.RED + stackedballs;
                } else {
                    stackstring += ChatColor.RED + nonstackedballs;
                }
            }

            if(i == 4) {
                if(stacks.get(p.getUniqueId())-1 >= 4) {
                    stackstring += ChatColor.DARK_RED + stackedballs;
                } else {
                    stackstring += ChatColor.DARK_RED + nonstackedballs;
                }
            }
        }

        if(maxstacks == stacks.get(p.getUniqueId())) {
            if (maxstacks - 1 == 1)
                stackstring += ChatColor.YELLOW + stackedballs;
            if (maxstacks - 1 == 2)
                stackstring += ChatColor.GOLD + stackedballs;
            if (maxstacks - 1 == 3)
                stackstring += ChatColor.RED + stackedballs;
            if (maxstacks - 1 == 4)
                stackstring += ChatColor.DARK_RED + stackedballs;
        } else {
            if (maxstacks - 1 == 1)
                stackstring += ChatColor.YELLOW + nonstackedballs;
            if (maxstacks - 1 == 2)
                stackstring += ChatColor.GOLD + nonstackedballs;
            if (maxstacks - 1 == 3)
                stackstring += ChatColor.RED + nonstackedballs;
            if (maxstacks - 1 == 4)
                stackstring += ChatColor.DARK_RED + nonstackedballs;
        }

        for(int i=maxstacks-1;i>=1; i--) {
            if(i == 1) {
                if(stacks.get(p.getUniqueId())-1 >= 1) {
                    stackstring += ChatColor.YELLOW + stackedballs;
                } else {
                    stackstring += ChatColor.YELLOW + nonstackedballs;
                }
            }

            if(i == 2) {
                if(stacks.get(p.getUniqueId())-1 >= 2) {
                    stackstring += ChatColor.GOLD + stackedballs;
                } else {
                    stackstring += ChatColor.GOLD + nonstackedballs;
                }
            }

            if(i == 3) {
                if(stacks.get(p.getUniqueId())-1 >= 3) {
                    stackstring += ChatColor.RED + stackedballs;
                } else {
                    stackstring += ChatColor.RED + nonstackedballs;
                }
            }

            if(i == 4) {
                if(stacks.get(p.getUniqueId())-1 >= 4) {
                    stackstring += ChatColor.DARK_RED + stackedballs;
                } else {
                    stackstring += ChatColor.DARK_RED + nonstackedballs;
                }
            }
        }

        new ActionBar(stackstring).send(p);
    }

    class BoltProfile {

        Player shooter;
        User user;

        Location origin;

        int level;

        double charge = 0;
        boolean stopcharge = false;

        int maxstacks;

        final double hitbox = 0.7;
        final double maxcharge = 1.2 * 20;
        final double particledensity = 3;
        final double maxDistanceUntilCurve = 0.5;
        final double maxTravelDistance = 15;

        final double baseDamage = 4.5;
        final double damagePerLevel = 0.5;
        final double damagePerStack = 0.5;

        public BoltProfile(Player shooter, User u, int level) {
            this.shooter = shooter;
            this.user = u;
            this.level = level;
            if(level < 4) {
                this.maxstacks = 2 + level;
            } else {
                this.maxstacks = 5;
            }
        }

        public void shoot() {
            origin = shooter.getEyeLocation().clone();
            stopcharge = true;

            if(stacks.containsKey(shooter.getUniqueId()) == false) {
                stacks.put(shooter.getUniqueId(), 1);
            }

            new DefaultBolt(this, stacks, stackdecay, false, origin);

            /* Visuals */
            new DefaultBolt(this, stacks, stackdecay, true, origin);

        }


    }

}
