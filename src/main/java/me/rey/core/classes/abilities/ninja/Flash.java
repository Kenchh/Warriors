package me.rey.core.classes.abilities.ninja;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.players.User;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class Flash extends Ability {

    public Flash() {
        super(3, "Flash", ClassType.LEATHER, AbilityType.AXE, 1, 5, 0, Arrays.asList(
                "Flash forwards 6 Blocks.",
                "Store up to "
        ));
    }

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {
        return false;
    }
}
