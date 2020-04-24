package me.rey.core.classes.abilities;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffectType;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.AbilityType.EventType;
import me.rey.core.events.customevents.AbilityUseEvent;
import me.rey.core.events.customevents.AbilityUseWhileCooldownEvent;
import me.rey.core.events.customevents.DamageEvent;
import me.rey.core.players.PlayerHit;
import me.rey.core.players.User;
import me.rey.core.pvp.Build;
import me.rey.core.pvp.ToolType;
import me.rey.core.utils.Cooldown;
import me.rey.core.utils.Text;

public abstract class Ability extends Cooldown implements Listener {
	
	private String name;
	private AbilityType abilityType;
	private ClassType classType;
	private String[] description;
	private int maxLevel, tempDefaultLevel, tokenCost;
	private long id;
	private boolean cooldownCanceled, ignoresCooldown, inLiquid, whileSlowed, inAir;
	private double cooldown, resetCooldown, energyCost;
	public String MAIN = "&7", VARIABLE = "&a", SECONDARY = "&e";
	
	public Ability(long id, String name, ClassType classType, AbilityType abilityType, int tokenCost, int maxLevel, double cooldown, List<String> description) {
		super(Warriors.getInstance(), Text.format(name, "You can use &a" + name + "&7."));
		
		this.name = name;
		this.classType = classType;
		this.abilityType = abilityType;
		this.maxLevel = maxLevel;
		this.cooldown = cooldown;
		this.resetCooldown = cooldown;
		this.inLiquid = false;
		this.whileSlowed = false;
		this.inAir = true;
		this.cooldownCanceled = false;
		this.id = id;
		this.tokenCost = tokenCost;
		this.energyCost = 0.00;
		this.description = new String[description.size()];
		
		int index = 0;
		for(String s : description) {
			this.description[index] = s;
			index++;
		}
	}
	
	public void run(Player p, ToolType toolType) {

		User user = new User(p);
		if(user.getWearingClass() == null || !(user.getWearingClass().equals(this.getClassType()))) return;
		
		Build b = user.getSelectedBuild(this.getClassType());
		if(b == null) b = this.getClassType().getDefaultBuild();
		if(b.getAbility(this.getAbilityType()) == null || b.getAbility(this.getAbilityType()).getIdLong() != this.getIdLong()) return;
		
		/*
		 * BOOSTER WEAPONS
		 */
		int level = b.getAbilityLevel(this.getAbilityType());
		if(toolType == ToolType.BOOSTER_AXE || toolType == ToolType.BOOSTER_SWORD) {
			level = (level + 2) > (this.getMaxLevel() + 1) ? level + 1 : level + 2;
		}
		
		// IN LIQUID
		if(p.getLocation().getBlock() != null && p.getLocation().getBlock().isLiquid() && !this.inLiquid) {
			String source = p.getLocation().getBlock().getType().name().toLowerCase().contains("water") ? "water" : "lava";
			new User(p).sendMessageWithPrefix(this.getName(), String.format("You cannot use &a" + this.getName() + "&7 in %s.", source));
			return;
		}
		
		// WHILE SLOWED
		if(user.hasPotionEffect(PotionEffectType.SLOW) && !whileSlowed) {
			new User(p).sendMessageWithPrefix(this.getName(), "You cannot use &a" + this.getName() + "&7 while slowed.");
			return;
		}
		
		// IN THE AIR
		if(!((Entity) p).isOnGround() && !inAir) {
			new User(p).sendMessageWithPrefix(this.getName(), "You cannot use &a" + this.getName() + "&7 in the air.");
			return;
		}
		
		if(this.hasCooldown(p)) {
			AbilityUseWhileCooldownEvent cooldownEvent = new AbilityUseWhileCooldownEvent(p, this, level);
			Bukkit.getServer().getPluginManager().callEvent(cooldownEvent);
			if(!cooldownEvent.isCancelled()) {
				if(!cooldownEvent.isMessageCancelled())
					user.sendMessageWithPrefix(this.getName(), "You cannot use &a" + this.getName() + "&7 for &a" + this.getPlayerCooldown(p) + " &7seconds.");
				
				return;
			}
		}
		
		
		// CALLING ABILITY EVENT
		AbilityUseEvent abilityEvent = new AbilityUseEvent(p, this, level);
		Bukkit.getServer().getPluginManager().callEvent(abilityEvent);
		if(abilityEvent.isCancelled()) return;
		
		if(this.energyCost > 0) {
			if(user.getEnergy() < this.energyCost) {
				user.sendMessageWithPrefix("Error", String.format("You don't have enough energy to use &a%s&7!", this.getName()));
				return;
			} else {
				user.consumeEnergy(this.energyCost);
			}
		}
		
		this.setSound(Sound.NOTE_PLING, 2.0F);
		this.execute(user, p, level);
		
		if(!ignoresCooldown && !cooldownCanceled) {
			this.setCooldownForPlayer(p, this.cooldown);
		}
		
		this.resetCooldown();
		this.setCooldownCanceled(false);
	}
	
	public abstract void execute(User u, final Player p, final int level);
	
	public String[] getDescription(int level) {
		String[] desc = this.description.clone();
		boolean selected = level <= 0;
		level = level <= 0 ? 1 : level;
		
		for(int i = 0; i < desc.length; i++) {
			String s = desc[i];
			if(s == null) continue;
			
			// EDITING in VARIABLES INSIDE ()
			Pattern p1 = Pattern.compile("\\(.*?\\)");
			Matcher m1 = p1.matcher(s);
			while(m1.find()) {
				String match = m1.group().subSequence(1,  m1.group().length()-1).toString();
				
				s = selected ? s.replace(" (" + match + ")", "") : s.replace("(" + match + ")", MAIN + "(<secondary>"+ match + "<main>)");
			}				
			
			//ADDING <VARIABLE> </VARIABLE> COLORS AND CALCULATING
			Pattern p2 = Pattern.compile("(?<=\\<variable\\>)(\\s*.*\\s*)(?=\\<\\/variable\\>)");
			Matcher m2 = p2.matcher(s);
			int finds = 0;
			while(m2.find()) {
				String match = m2.group(finds).replaceAll("\\s+", "").toLowerCase().replaceAll("l", level + "");
				String result = Text.calc(match);
				
				s = s.replace(m2.group(), result + "");
				finds++;
			}				
			
			if(selected) {
				desc[i] = Text.color(MAIN + s.replaceAll("<main>", MAIN).replaceAll("</variable>", MAIN)
						.replaceAll("<variable>", VARIABLE).replaceAll("<secondary>", SECONDARY));
			} else {
				desc[i] = Text.color(MAIN + s.replaceAll("<main>", MAIN).replaceAll("</variable>", MAIN)
						.replaceAll("<variable>", SECONDARY).replaceAll("<secondary>", VARIABLE));
			}
		}
		
		return desc;
	}
	
	public String[] getDescription() {
		return getDescription(-1);
	}
	
	public int getSkillTokenCost() {
		return this.tokenCost;
	}
	
	public void setIgnoresCooldown(boolean ignore) {
		this.ignoresCooldown = ignore;
	}
	
	public void setCooldown(double time) {
		this.cooldown = time;
	}
	
	private void resetCooldown() {
		this.cooldown = this.resetCooldown;
	}
	
	public long getIdLong() {
		return this.id;
	}
	
	public String getId() {
		return this.getIdLong() + "";
	}

	public String getName() {
		return this.name;
	}
	public double getCooldown() {
		return this.cooldown;
	}
	
	public ClassType getClassType() {
		return this.classType;
	}
	
	public AbilityType getAbilityType() {
		return this.abilityType;
	}
	
	public int getMaxLevel() {
		return this.maxLevel;
	}
	
	public int getTempDefaultLevel() {
		int temp = this.tempDefaultLevel;
		this.tempDefaultLevel = 0;
		return temp;
	}
	
	public Ability setTempDefaultLevel(int level) {
		this.tempDefaultLevel = level;
		return this;
	}
	
	public boolean sendUsedMessageToPlayer(Player p, String name) {
		new User(p).sendMessageWithPrefix(this.getName(), "You used &a" + name + "&7.");
		return true;
	}
	
	public void setInLiquid(boolean inLiquid) {
		this.inLiquid = inLiquid;
	}
	
	public void setWhileSlowed(boolean whileSlowed) {
		this.whileSlowed = whileSlowed;
	}
	
	public void setWhileInAir(boolean inAir) {
		this.inAir = inAir;
	}
	
	public void setCooldownCanceled(boolean canceled) {
		this.cooldownCanceled = canceled;
	}
	
	public void setEnergyCost(double energy) {
		this.energyCost = energy;
	}
	
	@EventHandler
	public void onDamage(DamageEvent e) {
		if(!(this instanceof DamageTrigger) || !(new User(e.getDamager()).isUsingAbility(this))) return;
		int level = new User(e.getDamager()).getSelectedBuild(new User(e.getDamager()).getWearingClass()).getAbilityLevel(this.getAbilityType());
		
		if(!e.isCancelled()) {
			boolean success = ((DamageTrigger) this).damageTrigger(e, level);
			
			if(!(e.getDamagee() instanceof Player)) return;
			if(!success) return;
			PlayerHit hit = new PlayerHit((Player) e.getDamagee(), (Player) e.getDamager(), e.getDamage(), null);
			hit.setCause("&a" + this.getName());
			e.setHit(hit);
		}
 	}
	
	@EventHandler
	public void onDropEvent(PlayerDropItemEvent e) {
		
		if(this.getAbilityType().getEventType().equals(EventType.DROP_ITEM)) {
			
			Material item = e.getItemDrop() == null ? Material.AIR : e.getItemDrop().getItemStack().getType();
			
			for(ToolType type : this.getAbilityType().getToolTypes()) {
				
				if(type.getType().equals(item)) {
					run(e.getPlayer(), type);	
					return;
				}
			}
		}
		
	}
	
	@EventHandler
	public void onEvent(PlayerInteractEvent e) {
		
		if(this.getAbilityType().getEventType().equals(EventType.RIGHT_CLICK)
				&& (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
			
			Material item = e.getItem() == null ? Material.AIR : e.getItem().getType();
			
			for(ToolType type : this.getAbilityType().getToolTypes()) {
				
				if(type.getType().equals(item)) {
					run(e.getPlayer(), type);					
					return;
				}
			}
		}
		
		if(this.getAbilityType().getEventType().equals(EventType.LEFT_CLICK)
				&& (e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK))) {
			
			Material item = e.getItem() == null ? Material.AIR : e.getItem().getType();
			
			for(ToolType type : this.getAbilityType().getToolTypes()) {
				
				if(type.getType().equals(item)) {
					run(e.getPlayer(), type);					
				}
			}
		}
	}

}
