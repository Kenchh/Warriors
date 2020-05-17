package me.rey.core.utils;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import me.rey.core.effects.ParticleEffect;

public class UtilParticle {
	
	/*
	 * PARTICLES
	 */
	public static void makeParticlesBetween(Location init, Location loc, ParticleEffect effect, double particleSeparation) {
		Vector pvector = Utils.getDirectionBetweenLocations(init, loc);
		for(double i = particleSeparation; i <= init.distance(loc); i += particleSeparation) {
			pvector.multiply(i);
			init.add(pvector);
			Location toSpawn = init.clone();
			toSpawn.setY(toSpawn.getY() + 0.5);
			
			effect.play(toSpawn);
			
			init.subtract(pvector);
			pvector.normalize();
		}
	}

	public static void playColoredParticle(Location loc, float red, float green, float blue) {
		loc.getWorld().spigot().playEffect(loc, Effect.COLOURED_DUST, 0, 0, red/255, green/255, blue/255, 1F, 0, 50);
	}

	public static void playColoredParticle(Location loc, float red, float green, float blue, int particlecount, int radius) {
		loc.getWorld().spigot().playEffect(loc, Effect.COLOURED_DUST, 0, 0, red/255, green/255, blue/255, 1F, particlecount, radius);
	}

}
