package me.rey.core.utils;

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

}
