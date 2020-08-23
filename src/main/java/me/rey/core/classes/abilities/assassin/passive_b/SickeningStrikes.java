package me.rey.core.classes.abilities.assassin.passive_b;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IDamageTrigger;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.players.User;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class SickeningStrikes extends Ability implements IDamageTrigger.IPlayerDamagedEntity {

    public SickeningStrikes() {
        super(043, "Sickening Strikes", ClassType.LEATHER, AbilityType.PASSIVE_B, 1, 3, 0.00, Arrays.asList(
                "Each hit applies wither to your",
                "enemy for <variable>1+l</variable> Seconds!"
        ));

        this.setIgnoresCooldown(true);
    }

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {
        DamageEvent damageEvent = (DamageEvent) conditions[0];

        final double witherTime = 1+level;
        PotionEffect effect = new PotionEffect(PotionEffectType.WITHER, (int) Math.floor(20 * witherTime), 0);

        damageEvent.getDamagee().addPotionEffect(effect);

        return true;
    }

}
