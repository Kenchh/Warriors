package me.rey.core.pvp;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.ArrayUtils;

import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.utils.Text;
import net.md_5.bungee.api.ChatColor;

public class Build extends AbstractMap<Ability, Integer> {

	public static final int MAX_TOKENS = 12;
	
	private HashMap<Ability, Integer> elements;
    private String name;
    private int position;
    private UUID uuid;
    private boolean currentState;
	
	public Build(String name, UUID uuid, int position, HashMap<Ability, Integer> abilities) {
		this.name = name;
		this.elements = abilities;
		this.position = position;
		this.uuid = uuid;
		
		this.currentState = false;
	}
	
	public Build(String name, UUID uuid, int position, Ability... abilities) {
		this.name = name;
		this.position = position;
		this.uuid = uuid;
		
		this.currentState = false;
		this.elements = new HashMap<Ability, Integer>();
		for(Ability ability : abilities) {
			this.elements.put(ability, ability.getTempDefaultLevel());
		}
	}
	
	public int getTokensRemaining() {
		int remaining = MAX_TOKENS;
		
		for(Ability ability : this.getAbilities().keySet()) {
			remaining = remaining - (this.getAbilityLevel(ability.getAbilityType()) * ability.getSkillTokenCost());
		}
		return remaining;
	}
	
	public UUID getUniqueId() {
		return uuid;
	}
	
	public int getPosition() {
		return this.position;
	}
	
	public void setPosition(int position) {
		this.position = position;
	}
    
    public Ability getAbility(AbilityType ability) {
    	for(Ability ab : this.getAbilities().keySet()) {
    		if(ab.getAbilityType().equals(ability)) return ab;
    	}
    	return null;
    }
    
    public int getAbilityLevel(AbilityType ability) {
    	return this.getAbilities().get(this.getAbility(ability));
    }
    
	@Override
	public Set<Entry<Ability, Integer>> entrySet() {
		return this.elements.entrySet();
	}
	
	public String getName() {
		return Text.color(this.name);
	}
	
	public String getNameWithoutColors() {
		return ChatColor.stripColor(this.getName());
	}
	
	public String getRawName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public HashMap<Ability, Integer> getAbilities(){
		return this.elements;
	}
	
	public boolean getCurrentState() {
		return currentState;
	}

	public void setCurrentState(boolean currentState) {
		this.currentState = currentState;
	}
	
	public void setAbility(Ability ability, int level) {
		if(this.getAbility(ability.getAbilityType()) != null) {
			this.elements.remove(this.getAbility(ability.getAbilityType()));
		}
		this.elements.put(ability, level);
	}
	
	public void remove(AbilityType ability) {
		if(this.getAbility(ability) != null) {
			this.elements.remove(this.getAbility(ability));
		}
	}
	
	public static class DefaultBuild extends Build {

		public DefaultBuild(Ability... abilities) {
			super("Default Build", null, -1, abilities);
		}
		
		@Override
		public UUID getUniqueId() {
			return null;
		}
		
	}

	public static class BuildSet extends AbstractList<Build> {
		
		private Build[] list;
		private int size = 0;
		
		public BuildSet(Build... builds) {			
			this.list = new Build[0];
			
			for(Build b : builds) {
				this.add(b);
			}
		}
		
		public Build[] getArray() {
			return list.clone();
		}

		@Override
		public Build get(int index) {
	        if(index >= size) throw new IndexOutOfBoundsException("" + index);
	        return (Build) list[index];
		}

		@Override
		public int size() {
			return size;
		}
		
		@Override
		public boolean add(Build build) {
	        if(size >= list.length){
	            Build[] newList = new Build[list.length + 1];
	            System.arraycopy(list, 0, newList, 0, list.length);
	            list = newList;
	        }
	        list[size] = build;
	        size++;
	        return true;
		}
		
		public boolean remove(Build build) {  
			for(int i = 0; i < list.length; i++) {
				Build b=  list[i];
				if(b.getUniqueId().toString().equalsIgnoreCase(build.getUniqueId().toString())) {	
					list = (Build[]) ArrayUtils.remove(list, i);
					break;
				}
			}
			return true;
		}

	}
    
}
