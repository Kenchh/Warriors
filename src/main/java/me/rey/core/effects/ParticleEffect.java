package me.rey.core.effects;

import org.bukkit.Effect;
import org.bukkit.Location;

public class ParticleEffect {
	
	private Effect effect;
	private float offsetX, offsetY, offsetZ, speed;

	private int particleCount, radius, id, data;
	
	public ParticleEffect(Effect effect) {
		this.effect = effect;
		
		this.id = 0;
		this.data = 0;
		
		this.offsetX = 0F;
		this.offsetY = 0F;
		this.offsetZ = 0F;
		
		this.speed = 0F;
		this.particleCount = 1;
		
		this.radius = 50;
	}
	
	public Effect getEffect() {
		return effect;
	}
	
	public ParticleEffect setEffect(Effect effect) {
		this.effect = effect;
		return this;
	}

	public float getSpeed() {
		return speed;
	}

	public ParticleEffect setSpeed(float speed) {
		this.speed = speed;
		return this;
	}

	public int getParticleCount() {
		return particleCount;
	}

	public ParticleEffect setParticleCount(int particleCount) {
		this.particleCount = particleCount;
		return this;
	}

	public int getRadius() {
		return radius;
	}

	public ParticleEffect setRadius(int radius) {
		this.radius = radius;
		return this;
	}

	public int getId() {
		return id;
	}

	public ParticleEffect setId(int id) {
		this.id = id;
		return this;
	}

	public int getData() {
		return data;
	}

	public ParticleEffect setData(int data) {
		this.data = data;
		return this;
	}
	
	public ParticleEffect setOffset(float offsetX, float offsetY, float offsetZ) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
		return this;
	}

	public float getOffsetX() {
		return offsetX;
	}

	public float getOffsetY() {
		return offsetY;
	}

	public float getOffsetZ() {
		return offsetZ;
	}
	
	public void play(Location loc) {
		loc.getWorld().spigot().playEffect(loc, effect, id, data, offsetX, offsetY, offsetZ, speed, particleCount, radius);
	}

	public static void playCustomParticle(Location loc, Effect effect, int id, int data, float offsetX, float offsetY, float offsetZ, float speed, int particleCount, int radius) {
		loc.getWorld().spigot().playEffect(loc, effect, id, data, offsetX, offsetY, offsetZ, speed, particleCount, radius);
	}

	
	public static class ColoredParticle extends ParticleEffect {

		private int red, green, blue;
		
		public ColoredParticle(int red, int green, int blue) {
			super(Effect.COLOURED_DUST);
			
			this.red = red;
			this.green = green;
			this.blue = blue;
		}
		
		@Override
		public void play(Location loc) {
			loc.getWorld().spigot().playEffect(loc, Effect.COLOURED_DUST, 0, 0, red/255, green/255, blue/255, 1F, 0, 50);
		}
	}
}
