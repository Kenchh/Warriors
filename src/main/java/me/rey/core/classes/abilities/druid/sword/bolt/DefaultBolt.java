package me.rey.core.classes.abilities.druid.sword.bolt;

import me.rey.core.utils.UtilParticle;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;

import java.util.HashMap;
import java.util.UUID;

public class DefaultBolt extends BoltObject {

    public DefaultBolt(Bolt.BoltProfile bo, HashMap<UUID, Integer> stacks, HashMap<UUID, Long> stackdecay, boolean onlyvisual, Location origin) {
        super(bo, stacks, stackdecay, onlyvisual, origin);
    }

    @Override
    public void tick() {
        super.tick();

        checkCollision();

        loc.getWorld().playSound(loc, Sound.SPIDER_IDLE, 1F, 2F);
        loc.getWorld().spigot().playEffect(loc, Effect.FIREWORKS_SPARK, 0, 0, 0F, 0F, 0F, 0F, 5, 50);

    }
}
