package me.rey.core.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.players.User;
import me.rey.core.pvp.Build;

public class GuiClassAbilitiesEditor extends GuiEditable {

	private ClassType classType;
	private Build toEdit;
	private Warriors w;
	
	public GuiClassAbilitiesEditor(Player player, ClassType classType, Build build) {
		super("Editing: &4" + build.getName(), 6, Warriors.getInstance());
		
		this.classType = classType;
		this.toEdit = build;
		this.w = Warriors.getInstance();
	}

	@Override
	public void setup() {
		
		/*
		 * SETTING ICONS
		 */
		int i = 0;
		for(AbilityType abilityType : AbilityType.values()) {
			
			AbilityType toExclude = AbilityType.BOW;
			for(Ability a : Warriors.getInstance().getClassAbilities(classType)) {
				if(a.getAbilityType().equals(AbilityType.BOW)) {
					toExclude = AbilityType.SPADE;
					break;
				}
			}
			
			
			if(toExclude.equals(abilityType)) continue;
			int row = 9 * (i + 1) - 9;
			setItem(new GuiItem(abilityType.getIcon().setName("&a&l" + abilityType.getName() + " Skills")) {
				@Override
				public void onUse(Player player, ClickType type, int slot) {
					// IGNORE
				}
				
			}, row);

			
			/*
			 *  SETTING ABILITIES
			 */
			int abilityCount = 0;
			for(int index = 0; index < w.getAbilitiesInCache().size(); index++) {
				
				Ability ability = w.getAbilitiesInCache().get(index);
				
				if(ability.getClassType().equals(classType) && ability.getAbilityType().equals(abilityType) && abilityCount <= 7) {
					
					boolean isInBuild = toEdit.getAbility(ability.getAbilityType()) != null && toEdit.getAbility(ability.getAbilityType()).getIdLong() == ability.getIdLong();
					Material material = isInBuild ? Material.WRITTEN_BOOK : Material.BOOK;
					String name = String.format("&a&l%s &f- &a&lLevel %s/%s",
							ability.getName(),
							isInBuild ? toEdit.getAbilityLevel(ability.getAbilityType()) : 0,
							ability.getMaxLevel());
					
					String actionOnClick = !isInBuild ? "Select" : "Upgrade to Level " + (toEdit.getAbilityLevel(ability.getAbilityType()) + 1);
					int level = !isInBuild ? 0 : toEdit.getAbilityLevel(ability.getAbilityType());
					List<String> lore = this.formatLore(Arrays.asList(ability.getDescription(level)), level, toEdit, ability, actionOnClick);
					int tokens = toEdit.getTokensRemaining();
					
					setItem(new GuiItem(new Item(material).setName(name).setAmount(Math.max(1, level)).setLore(lore)) {
						
						@Override
						public void onUse(Player player, ClickType type, int slot) {
							
							if(type == ClickType.LEFT) {
								Build newBuild = toEdit;
								
								if(!isInBuild) {
									newBuild.setAbility(ability, 1);
									new User(player).editBuild(toEdit, newBuild, classType);
									updateInventory();
								}
								
								if (isInBuild && (newBuild.getAbilityLevel(abilityType)) < ability.getMaxLevel()){
									if(tokens <= 0) return;
									newBuild.setAbility(ability, newBuild.getAbilityLevel(abilityType) + 1);
									new User(player).editBuild(toEdit, newBuild, classType);
									updateInventory();
								}
								
								updateInventory();
							} else if (type == ClickType.RIGHT) {
								Build newBuild = toEdit;
								
								if(isInBuild && (newBuild.getAbilityLevel(abilityType)) > 0) {
									
									if(newBuild.getAbilityLevel(abilityType) == 1) {
										newBuild.remove(newBuild.getAbility(abilityType));
										new User(player).editBuild(toEdit, newBuild, classType);
										updateInventory();
										return;
									}
									
									newBuild.setAbility(ability, newBuild.getAbilityLevel(abilityType) - 1);
									new User(player).editBuild(toEdit, newBuild, classType);
								}
								
								updateInventory();
							}
							
							
						}
						
					}, (row + 1) + abilityCount);	
					
					abilityCount++;
				}	
				
			}
			
			i++;
		}
		
		int tokens = toEdit.getTokensRemaining();
		String name = String.format("&a&l%s Skill Tokens", tokens);
		setItem(new GuiItem(new Item(tokens > 0 ? Material.GOLD_INGOT : Material.REDSTONE_BLOCK).setAmount(tokens > 0 ? tokens : 1).setName(name)) {

			@Override
			public void onUse(Player player, ClickType type, int slot) { /** ignore*/	}
				
		}, 8);
		
	}

	@Override
	public void init() {
		
	}
	
	private List<String> formatLore(List<String> description, int level, Build toEdit, Ability ability, String actionOnClick){
		List<String> lore = new ArrayList<String>();
		for(String s : description) {
			lore.add(s);
		}
		
		lore.add("");
		lore.add("");
		
		if(level < ability.getMaxLevel()) {
			lore.add("&eSkill Token Cost: &f" + ability.getSkillTokenCost());
			lore.add(String.format("&eSkill Tokens Remaining: &f%s/%s", toEdit.getTokensRemaining(), Build.MAX_TOKENS));
			lore.add("");
			lore.add("&aLeft-Click to " + actionOnClick);
		} else {
			lore.add("&6You have the maximum Level.");
		}
		return lore;
	}

}
