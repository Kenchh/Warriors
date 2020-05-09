package me.rey.core.players;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.commands.CommandType;
import me.rey.core.database.SQLManager;
import me.rey.core.energy.EnergyHandler;
import me.rey.core.pvp.Build;
import me.rey.core.pvp.Build.BuildSet;
import me.rey.core.utils.Text;
import me.rey.core.utils.Utils;

public class User {
	
	private Player player;
	private Warriors plugin = Warriors.getInstance();
	private SQLManager sql;
	private EnergyHandler energyHandler = new EnergyHandler();
	private PlayerHitCache cache = Warriors.getInstance().getHitCache();
	
	public User(Player player) {
		this.player = player;
		
		sql = plugin.getSQLManager();
	}
	
	public Player getPlayer() {
		return this.player;
	}
	 
	public UUID getUniqueId() {
		return getPlayer().getUniqueId();
	}
	
	public boolean isInCombat() {
		return cache.hasCombatTimer(this.getPlayer());
	}
	
	public double getEnergy() {
		return energyHandler.getUserEnergy(this.getUniqueId());
	}
	
	public void consumeEnergy(double energy) {
		double toSet = getEnergy()-energy;
		energyHandler.setEnergy(this.getUniqueId(), toSet);
	}
	
	public void addEnergy(double energy) {
		double toSet = getEnergy()+energy;
		energyHandler.setEnergy(this.getUniqueId(), toSet);
	}
	
	public float getEnergyExp() {
		return Math.min(0.999F, ((float) EnergyHandler.MAX_ENERGY * (float) this.getEnergy() / (float) energyHandler.getCapacity(this.getUniqueId())) / (float) EnergyHandler.MAX_ENERGY);
	}
	
	public boolean isUsingAbility(Ability ability) {
		if(this.getWearingClass() != null && this.getSelectedBuild(this.getWearingClass()) != null
				&& this.getSelectedBuild(this.getWearingClass()).getAbility(ability.getAbilityType()) == ability)
			return true;
		return false;
	}
	
	public boolean hasPotionEffect(PotionEffectType effect) {
		if(player.getActivePotionEffects().isEmpty()) return false;
		
		for(PotionEffect e : player.getActivePotionEffects()) {
			if(e.getType().equals(effect)) return true;
		}

		return false;
	}
	
	public User resetEffects() {
		
		List<PotionEffectType> activeEffects = new ArrayList<>();
		for(PotionEffect active : this.getPlayer().getActivePotionEffects()) {
			activeEffects.add(active.getType());
		}
		
		if(!activeEffects.isEmpty()) {
			for(ClassType classType : ClassType.values()) {
				if(classType.getEffects().length == 0) continue;
				
				for(PotionEffect effect : classType.getEffects()) {
					if(activeEffects.contains(effect.getType())) {
						this.getPlayer().removePotionEffect(effect.getType());
					}
				}
			}
		}
		
		this.updateClassEffects();
		return this;
	}
	
	public User updateClassEffects() {
		
		if(this.getWearingClass() == null || this.getWearingClass().getEffects() == null) return this;
		
		List<PotionEffectType> activeEffects = new ArrayList<>();
		for(PotionEffect active : this.getPlayer().getActivePotionEffects()) {
			activeEffects.add(active.getType());
		}
		
		for(PotionEffect effect : this.getWearingClass().getEffects()) {
			if(activeEffects.contains(effect.getType())) continue;
			this.getPlayer().addPotionEffect(effect);
		}
		return this;
	}
	
	public void sendMessageWithPrefix(String prefix, String message) {
		this.getPlayer().sendMessage(Text.format(prefix, message));
	}
	
	public void sendMessageWithPrefix(CommandType commandType, String message) {
		sendMessageWithPrefix(commandType.getName(), message);
	}
	
	public void sendMessage(String message) {
		this.getPlayer().sendMessage(Text.color(message));
	}
	
	public ClassType getWearingClass() {
		PlayerInventory inventory = this.getPlayer().getInventory();
		
		for(ClassType classType : ClassType.values()) {
			try {
				
				if(!Utils.compareItems(inventory.getHelmet(), classType.getHelmet().get())) continue;
				if(!Utils.compareItems(inventory.getChestplate(), classType.getChestplate().get())) continue;
				if(!Utils.compareItems(inventory.getLeggings(), classType.getLeggings().get())) continue;
				if(!Utils.compareItems(inventory.getBoots(), classType.getBoots().get())) continue;
				return classType;
				
			} catch (NullPointerException e) {
				return null;
			}
		}
		
		return null;
	}
	
	public BuildSet getBuilds(ClassType classType) {
		BuildSet b = Warriors.buildCache.containsKey(this.getUniqueId()) && Warriors.buildCache.get(this.getUniqueId()).containsKey(classType)
				? new BuildSet(Warriors.buildCache.get(this.getUniqueId()).get(classType)) : new BuildSet();
		return b;
	}
	
//	public Build getSelectedBuild(ClassType classType) {
//		if(Warriors.buildCache.containsKey(this.getPlayer())) {
//			for(ClassType query : Warriors.buildCache.get(this.getPlayer()).keySet()) {
//				if(query == classType) return Warriors.buildCache.get(this.getPlayer()).get(query);
//			}
//		}
//		
//		return null;
//	}
	
	public Build getSelectedBuild(ClassType classType) {
		for(Build b : this.getBuilds(classType)) {
			if(b.getCurrentState()) return b;
		}
		
		return null;
	}

	
	/*
	 * Build selection 
	 */
	public void selectBuild(Build build, ClassType classType, boolean message) {
		for(Build b : this.sql.getPlayerBuilds(this.getUniqueId(), classType)) {
			b.setCurrentState(false);
			sql.saveBuild(player.getUniqueId(), b, classType);
		}
		
		if(build != null) {
			build.setCurrentState(true);
			sql.saveBuild(player.getUniqueId(), build, classType);
		}
		
		saveBuildsInCache();
		
		if(message) {
			this.sendBuildEquippedMessage(classType);
		}
		
	}
	
	public void selectBuild(Build build, ClassType classType) {
		
		selectBuild(build, classType, true);
	}
	
	public void sendBuildEquippedMessage(ClassType classType) {
		if(this.getWearingClass() == null || !this.getWearingClass().equals(classType)) return;
		
		Build build = this.getSelectedBuild(classType);
		Build query = build == null ? classType.getDefaultBuild() : build;
		
		this.sendListingClassSkills(classType);
		
		this.sendMessageWithPrefix("Class", "You equipped &a" + query.getName() + "&7.");
	}
	
	public void sendListingClassSkills(ClassType classType) {
		this.sendMessageWithPrefix("Skill", "Listing Class Skills:");
		if(this.getWearingClass() == null || !this.getWearingClass().equals(classType)) return;
		
		Build build = this.getSelectedBuild(classType);
		Build query = build == null ? classType.getDefaultBuild() : build;
		
		for(AbilityType abilityType : AbilityType.values()) {
			if(query.getAbility(abilityType) == null) continue;
			
			this.sendMessage(String.format("&2%s:&r&f %s Lvl%s", 
					abilityType.getName(),
					query.getAbility(abilityType).getName(),
					query.getAbilityLevel(abilityType)
					));
		}
	}
	
	public void editBuild(Build old, Build newBuild, ClassType classType) {
		for(Build b : this.sql.getPlayerBuilds(this.getUniqueId(), classType)) {
			if(b.getUniqueId().toString().trim().equals(old.getUniqueId().toString().trim())) {
				b = newBuild;
				sql.deletePlayerBuild(this.getPlayer().getUniqueId(), b, classType);
				sql.createPlayerBuild(this.getPlayer().getUniqueId(), b, classType);
				
			}
		}
		
		saveBuildsInCache();
	}
	
	private void saveBuildsInCache() {
		for(ClassType type : ClassType.values()) {
			HashMap<ClassType, Build[]> builds = Warriors.buildCache.containsKey(this.getUniqueId()) && Warriors.buildCache.get(this.getUniqueId()).containsKey(type) ?
					Warriors.buildCache.get(this.getUniqueId()) : Warriors.buildCache.put(this.getUniqueId(), new HashMap<ClassType, Build[]>());
			builds.put(type, this.sql.getPlayerBuilds(this.getUniqueId(), type).getArray());
			Warriors.buildCache.replace(this.getUniqueId(), builds);
		}
	}

}
