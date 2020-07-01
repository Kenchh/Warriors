package me.rey.core.classes.abilities.marksman.sword;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IDamageTrigger;
import me.rey.core.events.customevents.combat.DamagedByEntityEvent;
import me.rey.core.players.User;
import me.rey.core.utils.UtilEnt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class Escape extends Ability implements IDamageTrigger, IDamageTrigger.IPlayerDamagedByEntity {
    public Escape() {
        super(401, "Escape", ClassType.CHAIN, AbilityType.SWORD, 1, 4, 15, Arrays.asList(
                "",
                ""
        ));
    }

    HashMap<UUID, EscapeProfile> escape = new HashMap<>();

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {

        if(conditions.length > 0 && conditions[0] != null && conditions[0] instanceof DamagedByEntityEvent) {
            Object arg = conditions[0];

            this.setCooldownCanceled(true);

            if(escape.containsKey(p.getUniqueId())) {
                if(escape.get(p.getUniqueId()).hit == false) {
                    if (escape.get(p.getUniqueId()).u.getTeam().contains(((DamagedByEntityEvent) arg).getDamager())) {
                        return false;
                    }

                    ((DamagedByEntityEvent) arg).setCancelled(true);
                    ((DamagedByEntityEvent) arg).getDamager().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * escape.get(p.getUniqueId()).level, 3));

                    Location turn = ((DamagedByEntityEvent) arg).getDamager().getLocation().clone();

                    escape.get(p.getUniqueId()).hit = true;

                    p.setVelocity(turn.getDirection().multiply(2).setY(1.1));
                    p.getWorld().playSound(p.getLocation(), Sound.ITEM_BREAK, 1F, 2F);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if(escape.containsKey(p.getUniqueId())) {
                                escape.remove(p.getUniqueId());
                            }
                        }
                    }.runTaskLaterAsynchronously(Warriors.getInstance(), 60L);

                    this.setCooldownCanceled(false);
                }
            }

            return true;
        }

        if(escape.containsKey(p.getUniqueId())) {
            this.setCooldownCanceled(true);
            return true;
        }

        this.setCooldown(10-level);

        if(escape.containsKey(p.getUniqueId()) == false) {
            escape.put(p.getUniqueId(), new EscapeProfile(u, level));
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if(!p.isBlocking() && escape.containsKey(p.getUniqueId())) {
                    if(escape.get(p.getUniqueId()).hit == false) {
                        escape.remove(p.getUniqueId());
                        applyCooldown(p);
                        sendAbilityMessage(p, "Failed to use Escape.");
                        this.cancel();
                    } else {
                        this.cancel();
                    }
                } else {
                    if(escape.containsKey(p.getUniqueId())) {
                        if(escape.get(p.getUniqueId()).ticks >= 20) {
                            this.cancel();
                        } else {
                            escape.get(p.getUniqueId()).ticks++;
                        }
                    } else {
                        this.cancel();
                    }
                }
            }
        }.runTaskTimerAsynchronously(Warriors.getInstance(), 1L, 1L);

        new BukkitRunnable() {
            public void run() {
                if(escape.containsKey(p.getUniqueId())) {
                    if(escape.get(p.getUniqueId()).hit == false) {
                        escape.remove(p.getUniqueId());
                        applyCooldown(p);
                        sendAbilityMessage(p, "Failed to use Escape.");
                    }
                }
            }
        }.runTaskLaterAsynchronously(Warriors.getInstance(), 20L);

        this.setCooldownCanceled(true);
        return true;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if(e.getEntity().getType() == EntityType.PLAYER) {
            if(e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                Player p = (Player) e.getEntity();
                if(escape.containsKey(p.getUniqueId())) {
                    if(escape.get(p.getUniqueId()).hit) {
                        e.setCancelled(true);
                        escape.remove(p.getUniqueId());
                    }
                }
            }
        }
    }

    class EscapeProfile {

        User u;
        int level;
        boolean hit = false;
        int ticks = 0;

        public EscapeProfile(User u, double level) {
            this.u = u;
            this.level = (int) level;
        }
    }

}
