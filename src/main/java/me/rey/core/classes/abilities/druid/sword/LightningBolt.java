package me.rey.core.classes.abilities.druid.sword;

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
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class LightningBolt extends Ability implements IConstant {

    public LightningBolt() {
        super(201, "Lightning Bolt", ClassType.GOLD, AbilityType.SWORD, 1, 5, 0.0, Arrays.asList(
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

    public HashMap<UUID, BoltObject> bolts = new HashMap<UUID, BoltObject>();
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
            BoltObject bolt = new BoltObject(p, u, level);
            bolts.put(p.getUniqueId(), bolt);
        }

        BoltObject bolt = bolts.get(p.getUniqueId());

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

    class Bolt {

        BoltObject bo;

        Location loc;
        Location origin;

        boolean onlyvisual;

        boolean incurve = false;

        Location locWithNoCurve;
        double travelDistance = 20;
        double travelledDistanceSinceLastCurve = 0;
        double maxCurveDistance = 5;

        ArrayList<UUID> damagedplayers = new ArrayList<UUID>();

        public Bolt(BoltObject bo, boolean onlyvisual, Location origin) {
            this.bo = bo;
            this.onlyvisual = onlyvisual;
            this.origin = origin;
            this.loc = origin.clone();

            this.travelDistance = bo.maxTravelDistance * (bo.charge/bo.maxcharge);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if(!destroy()) {
                        tick();
                    } else {
                        this.cancel();
                    }
                }
            }.runTaskTimer(Warriors.getInstance(), 1L, 1L);

        }

        public void tick() {

            if(!onlyvisual) {
                Random randomsound = new Random();
                int rs = randomsound.nextInt(3) + 1;
                SoundEffect.playCustomSound(loc, "lightningbolt" + rs, 1F, 1F + 0.2F*(stacks.get(bo.shooter.getUniqueId())-1));
            }

            if(destroy()) {
                return;
            }

            double addX = UtilBlock.getXZCordsMultipliersFromDegree(loc.getYaw() + 90)[0] / bo.particledensity;
            double addY = UtilBlock.getYCordsMultiplierByPitch(loc.getPitch());
            double addZ = UtilBlock.getXZCordsMultipliersFromDegree(loc.getYaw() + 90)[1] / bo.particledensity;

            if(!incurve) {

                locWithNoCurve = loc.clone();

                while(travelledDistanceSinceLastCurve <= bo.maxDistanceUntilCurve) {

                    travelledDistanceSinceLastCurve = locWithNoCurve.distance(loc);

                    loc.setX(loc.getX() + addX);
                    loc.setY(loc.getY() + addY);
                    loc.setZ(loc.getZ() + addZ);

                    if(destroy()) {
                        return;
                    }

                    checkCollision();

                    for(int i=1;i<=5;i++) {
                        UtilParticle.playColoredParticle(loc, 255, 255-20*stacks.get(bo.shooter.getUniqueId()), 102-20*stacks.get(bo.shooter.getUniqueId()));
                        UtilParticle.playColoredParticle(loc, 255, 255-20*stacks.get(bo.shooter.getUniqueId()), 255-20*stacks.get(bo.shooter.getUniqueId()));
                    }
                }

                incurve = true;

            } else {
                Location bloc = loc.clone(); /* Setting start break point */

                Random rd = new Random();
                double randomaddDegree = rd.nextInt(8) + 5; /* Minimum curve 10, max 25 */
                Random positive = new Random();
                if (!positive.nextBoolean()) {
                    randomaddDegree = -randomaddDegree;
                }
                double degree = (loc.getYaw() + 90) + randomaddDegree;

                while(bloc.distance(loc) <= maxCurveDistance) {
                    double curveaddX = UtilBlock.getXZCordsMultipliersFromDegree(degree)[0];
                    double curveaddZ = UtilBlock.getXZCordsMultipliersFromDegree(degree)[1];

                    loc.setX(loc.getX() + curveaddX);
                    loc.setY(loc.getY() + addY);
                    loc.setZ(loc.getZ() + curveaddZ);

                    if(destroy()) {
                        return;
                    }

                    checkCollision();

                    for(int i=1;i<=5;i++) {
                        UtilParticle.playColoredParticle(loc, 255, 255-20*stacks.get(bo.shooter.getUniqueId()), 102-20*stacks.get(bo.shooter.getUniqueId()));
                        UtilParticle.playColoredParticle(loc, 255, 255-20*stacks.get(bo.shooter.getUniqueId()), 255-20*stacks.get(bo.shooter.getUniqueId()));
                    }
                }

                bloc = loc.clone(); /* Setting middle break point */
                degree = (loc.getYaw() + 90) - randomaddDegree*2; /* Reflection */

                while(bloc.distance(loc) <= maxCurveDistance) {
                    double curveaddX = UtilBlock.getXZCordsMultipliersFromDegree(degree)[0];
                    double curveaddZ = UtilBlock.getXZCordsMultipliersFromDegree(degree)[1];

                    loc.setX(loc.getX() + curveaddX);
                    loc.setY(loc.getY() + addY);
                    loc.setZ(loc.getZ() + curveaddZ);

                    if(destroy()) {
                        return;
                    }

                    checkCollision();

                    for(int i=1;i<=5;i++) {
                        UtilParticle.playColoredParticle(loc, 255, 255-20*stacks.get(bo.shooter.getUniqueId()), 102-20*stacks.get(bo.shooter.getUniqueId()));
                        UtilParticle.playColoredParticle(loc, 255, 255-20*stacks.get(bo.shooter.getUniqueId()), 255-20*stacks.get(bo.shooter.getUniqueId()));
                    }
                }

                travelledDistanceSinceLastCurve = 0;
                incurve = false;
            }

        }

        public void checkCollision() {

            if(onlyvisual) {
                return;
            }

            Location collisionloc = loc.clone();
            collisionloc.setY(loc.getY() - 2.0);
            for(Entity e : collisionloc.getWorld().getNearbyEntities(collisionloc, bo.hitbox, bo.hitbox + 1, bo.hitbox)) {
                if(e instanceof LivingEntity) {
                    if(e == bo.shooter || damagedplayers.contains(e.getUniqueId()))
                        continue;

                    if(e instanceof Player) {
                        Player p = (Player) e;
                        if(bo.user.getTeam().contains(p)) {
                            continue;
                        }
                    }

                    damagedplayers.add(e.getUniqueId());

                    if(stacks.containsKey(bo.shooter.getUniqueId())) {
                        if(stacks.get(bo.shooter.getUniqueId()) < bo.maxstacks) {
                            stacks.replace(bo.shooter.getUniqueId(), stacks.get(bo.shooter.getUniqueId()) + 1);
                        }
                    }

                    if(stackdecay.containsKey(bo.shooter.getUniqueId()) == false) {
                        stackdecay.put(bo.shooter.getUniqueId(), 100L);
                    } else {
                        stackdecay.replace(bo.shooter.getUniqueId(), 100L);
                    }
                    UtilEnt.damage(bo.baseDamage+bo.level*bo.damagePerLevel+bo.damagePerStack*stacks.get(bo.shooter.getUniqueId()), "Lightning Bolt", (LivingEntity) e, bo.shooter);
                }
            }
        }

        public boolean destroy() {

            if((UtilBlock.airFoliage(loc.getBlock()) && loc.distance(origin) < travelDistance) || (loc.getBlock().isLiquid() && loc.distance(origin) < travelDistance)) {

                return false;
            }

            if(!onlyvisual) {
                if (damagedplayers.isEmpty()) {
                    stacks.replace(bo.shooter.getUniqueId(), 1);
                }
            }

            return true;
        }

    }

    /* TODO:
        Water 
    */

    class BoltObject {

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

        public BoltObject(Player shooter, User u, int level) {
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

            new Bolt(this, false, origin);

            /* Extra Bolts but only visuals */
            for(int i=1; i<stacks.get(shooter.getUniqueId()); i++)
                new Bolt(this, true, origin);

        }


    }

}
