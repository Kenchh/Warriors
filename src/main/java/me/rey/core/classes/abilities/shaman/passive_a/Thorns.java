package me.rey.core.classes.abilities.shaman.passive_a;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.classes.abilities.IDamageTrigger;
import me.rey.core.energy.EnergyHandler;
import me.rey.core.enums.State;
import me.rey.core.events.customevents.combat.CustomKnockbackEvent;
import me.rey.core.events.customevents.combat.DamagedByEntityEvent;
import me.rey.core.events.customevents.update.UpdateEvent;
import me.rey.core.players.User;
import me.rey.core.utils.UtilEnt;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class Thorns extends Ability implements IConstant, IConstant.ITogglable, IDamageTrigger, IDamageTrigger.IPlayerDamagedByEntity {

    private EnergyHandler handler = new EnergyHandler();

    public Thorns() {
        super(532, "Thorns", ClassType.GREEN, AbilityType.PASSIVE_A, 1, 3, 0.00, Arrays.asList(
                "By dropping your weapon, enter a state in which",
                "you receive Resistance 3 and have no knockback.",
                "",
                "Enemies attacking you will be prickled by your thorns",
                "causing them to take <variable>0.5+0.5*l</variable> damage.",
                "",
                "Energy: <variable>42-2*l</variable>"
        ));
        this.setEnergyCost(40 / 20, 2/20);
        this.setIgnoresCooldown(true);
        this.setInLiquid(true);
    }

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {

        double damage = 0.5+0.5*level;

        Object arg = conditions[0];
        if(arg != null && arg instanceof UpdateEvent) {

            // Consuming energy
            if(!this.getEnabledPlayers().contains(p.getUniqueId())) return false;
            handler.togglePauseEnergy(State.ENABLED, p.getUniqueId());

            for(double yp=0;yp<=2;yp+=0.25) {
                Location loc = p.getLocation().clone();
                loc.setY(loc.getY() + yp);
                p.getWorld().spigot().playEffect(loc, Effect.CRIT);
            }

            p.playSound(p.getLocation(), Sound.CLICK, 0.5F, 1.2F);

        }

        if(arg != null && arg instanceof DamagedByEntityEvent) {

            LivingEntity damaged = ((DamagedByEntityEvent) arg).getDamagee();
            LivingEntity damager = ((DamagedByEntityEvent) arg).getDamager();

            // Checking if damaged is player and is using void
            if(!(damaged instanceof Player)) return false;
            if(!(this.getEnabledPlayers().contains(((Player) damaged).getUniqueId()))) return false;

            UtilEnt.damage(damage, "Thorns", damager, damaged);
            for(int i=0; i<=10; i++)
                damager.getWorld().playSound(damager.getLocation(), Sound.DIG_GRAVEL, 1F, 1.3F);


        }


        return true;
    }

    @EventHandler
    public void onKB(CustomKnockbackEvent e) {
        if(!(e.getDamagee() instanceof Player)) return;
        if(!this.getEnabledPlayers().contains((e.getDamagee()).getUniqueId())) return;

        e.setCancelled(true);
    }

    @Override
    public boolean off(Player p) {
        handler.togglePauseEnergy(State.DISABLED, p.getUniqueId());
        p.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        return true;
    }

    @Override
    public boolean on(Player p) {
        handler.togglePauseEnergy(State.ENABLED, p.getUniqueId());
        p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 100000, 2));
        return true;
    }
}
