package me.rey.core.classes.abilities.marksman.sword;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IDamageTrigger;
import me.rey.core.events.customevents.combat.DamagedByEntityEvent;
import me.rey.core.players.User;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class Escape extends Ability implements IDamageTrigger.IPlayerDamagedByEntity {
    public Escape() {
        super(401, "Escape", ClassType.CHAIN, AbilityType.SWORD, 1, 4, 15, Arrays.asList(
                "",
                ""
        ));
    }

    HashMap<UUID, EscapeProfile> escape = new HashMap<>();

    @Override
    public void onDamageByEntity(DamagedByEntityEvent e) {
        super.onDamageByEntity(e);

        Player p = e.getDamagee();
        Bukkit.broadcastMessage("a");
        if(escape.containsKey(p.getUniqueId())) {
            Bukkit.broadcastMessage("b");
            if(escape.get(p.getUniqueId()).u.getTeam().contains(e.getDamager())) {
                return;
            }

            escape.remove(p.getUniqueId());
            e.setCancelled(true);
            e.getDamager().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20*escape.get(p.getUniqueId()).level, 3));
            p.setVelocity(p.getLocation().getDirection().setY(1.5).multiply(-0.8));
            p.getWorld().playSound(p.getLocation(), Sound.ITEM_BREAK, 1F, 2F);
        }
    }

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {

        this.setCooldown(15-level);

        if(escape.containsKey(p.getUniqueId()) == false) {
            escape.put(p.getUniqueId(), new EscapeProfile(u, level));
        }


        new BukkitRunnable() {
            @Override
            public void run() {
                if(!p.isBlocking() && escape.containsKey(p.getUniqueId())) {
                    escape.remove(p.getUniqueId());
                    sendAbilityMessage(p, "Failed to use Escape.");
                }
            }
        }.runTaskTimerAsynchronously(Warriors.getInstance(), 1L, 1L);

        new BukkitRunnable() {
            public void run() {
                if(escape.containsKey(p.getUniqueId())) {
                    escape.remove(p.getUniqueId());
                    sendAbilityMessage(p, "Failed to use Escape.");
                }
            }
        }.runTaskLaterAsynchronously(Warriors.getInstance(), 20L);

        return false;
    }

    class EscapeProfile {

        User u;
        int level;

        public EscapeProfile(User u, double level) {
            this.u = u;
            this.level = (int) level;
        }
    }

}
