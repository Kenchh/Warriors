package me.rey.core.classes.abilities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.AbilityType.EventType;
import me.rey.core.classes.abilities.IConstant.IDroppable;
import me.rey.core.classes.abilities.IConstant.ITogglable;
import me.rey.core.classes.abilities.IDamageTrigger.IPlayerDamagedByEntity;
import me.rey.core.classes.abilities.IDamageTrigger.IPlayerDamagedEntity;
import me.rey.core.energy.IEnergyEditor;
import me.rey.core.enums.AbilityFail;
import me.rey.core.enums.State;
import me.rey.core.events.customevents.AbilityFailEvent;
import me.rey.core.events.customevents.AbilityUseEvent;
import me.rey.core.events.customevents.DamageEvent;
import me.rey.core.events.customevents.DamagedByEntityEvent;
import me.rey.core.events.customevents.EnergyUpdateEvent;
import me.rey.core.events.customevents.UpdateEvent;
import me.rey.core.players.PlayerHit;
import me.rey.core.players.User;
import me.rey.core.pvp.Build;
import me.rey.core.pvp.ToolType;
import me.rey.core.utils.Cooldown;
import me.rey.core.utils.Text;

public abstract class Ability extends Cooldown implements Listener {
	
	// Droppable
	private Set<UUID> playersEnabled;
	
	private String name;
	private AbilityType abilityType;
	private ClassType classType;
	private String[] description;
	private int maxLevel, tempDefaultLevel, tokenCost;
	private long id;
	private boolean cooldownCanceled, ignoresCooldown, inLiquid, whileSlowed, inAir;
	private double cooldown, resetCooldown, energyCost;
	private String MAIN = "&7", VARIABLE = "&a", SECONDARY = "&e";
	
	public Ability(long id, String name, ClassType classType, AbilityType abilityType, int tokenCost, int maxLevel, double cooldown, List<String> description) {
		super(Warriors.getInstance(), Text.format(name, "You can use &a" + name + "&7."));
		
		this.name = name;
		this.classType = classType;
		this.abilityType = abilityType;
		this.maxLevel = maxLevel;
		this.cooldown = cooldown;
		this.resetCooldown = cooldown;
		this.inLiquid = false;
		this.whileSlowed = true;
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
		
		if(this instanceof ITogglable)
			this.playersEnabled = new HashSet<>();

	}
	
	public boolean run(Player p, ToolType toolType, boolean messages, Object... conditions) {
		return run(true, false, p, toolType, messages, conditions);
	}
	
	public boolean run(boolean useEnergy, boolean ignoreEvents, Player p, ToolType toolType, boolean messages, Object... conditions) {

		User user = new User(p);
		if(user.getWearingClass() == null || !(user.getWearingClass().equals(this.getClassType()))) return false;
		
		Build b = user.getSelectedBuild(this.getClassType());
		if(b == null) b = this.getClassType().getDefaultBuild();
		if(b.getAbility(this.getAbilityType()) == null || b.getAbility(this.getAbilityType()).getIdLong() != this.getIdLong()) return false;
		
		/*
		 * BOOSTER WEAPONS
		 */
		int level = b.getAbilityLevel(this.getAbilityType());
		if(toolType != null && (toolType == ToolType.BOOSTER_AXE || toolType == ToolType.BOOSTER_SWORD)) {
			level = Math.min(this.getMaxLevel() + 1, level + 2);
		}
		
		AbilityFailEvent event = null;
		
		// WHILE COOLDOWN
		if(this.hasCooldown(p)) {
			event = new AbilityFailEvent(AbilityFail.COOLDOWN, p, this, level);
			Bukkit.getServer().getPluginManager().callEvent(event);
			
			if(!event.isCancelled()) {
				if(!event.isMessageCancelled() && messages)
						user.sendMessageWithPrefix(this.getName(), "You cannot use &a" + this.getName() + "&7 for &a" + this.getPlayerCooldown(p) + " &7seconds.");
				
				return false;
			}
		}
		
		// IN LIQUID
		if(p.getLocation().getBlock() != null && p.getLocation().getBlock().isLiquid() && !this.inLiquid) {
			
			event = new AbilityFailEvent(AbilityFail.LIQUID, p, this, level);
			Bukkit.getServer().getPluginManager().callEvent(event);
			
			if(!event.isCancelled()) {
				String source = p.getLocation().getBlock().getType().name().toLowerCase().contains("water") ? "water" : "lava";
				if(messages && !event.isMessageCancelled())
					new User(p).sendMessageWithPrefix(this.getName(), String.format("You cannot use &a" + this.getName() + "&7 in %s.", source));
				return false;
			}
		}
		
		// WHILE SLOWED
		if(user.hasPotionEffect(PotionEffectType.SLOW) && !whileSlowed) {
			
			event = new AbilityFailEvent(AbilityFail.SLOWED, p, this, level);
			Bukkit.getServer().getPluginManager().callEvent(event);
			
			if(!event.isCancelled()) {
				if(messages && !event.isMessageCancelled())
					new User(p).sendMessageWithPrefix(this.getName(), "You cannot use &a" + this.getName() + "&7 while slowed.");
				return false;
			}
		}
		
		// IN THE AIR
		if(!((Entity) p).isOnGround() && !inAir) {

			event = new AbilityFailEvent(AbilityFail.AIR, p, this, level);
			Bukkit.getServer().getPluginManager().callEvent(event);
			
			if(!event.isCancelled()) {
				if(messages && !event.isMessageCancelled())
					new User(p).sendMessageWithPrefix(this.getName(), "You cannot use &a" + this.getName() + "&7 in the air.");
				return false;
			}
		}
		
		// CALLING ABILITY EVENT
		if(!ignoreEvents) {
			AbilityUseEvent abilityEvent = new AbilityUseEvent(p, this, level);
			Bukkit.getServer().getPluginManager().callEvent(abilityEvent);
			if(abilityEvent.isCancelled()) return false;
		}
		
		if(useEnergy && this.energyCost > 0) {
			if(user.getEnergy() < this.energyCost) {

				if(messages)
					user.sendMessageWithPrefix("Error", String.format("You don't have enough energy to use &a%s&7!", this.getName()));
				
				if(this instanceof ITogglable) {
					this.playersEnabled.remove(p.getUniqueId());
					((ITogglable) this).off(p);
				}
				return false;
			} else {
				user.consumeEnergy(this.energyCost);
			}
		}
		
		this.setSound(Sound.NOTE_PLING, 2.0F);
		boolean success = this.execute(user, p, level, conditions);
		
		this.applyCooldown(p);
		return success;
	}
	
	protected abstract boolean execute(User u, final Player p, final int level, Object... conditions);
	
	public String[] getDescription(int level) {
		String[] desc = this.description.clone();
		boolean selected = level <= 0;
		level = level <= 0 ? 1 : level;
		
		for(int i = 0; i < desc.length; i++) {
			String s = desc[i];
			if(s == null) continue;				
			
			//ADDING <VARIABLE> </VARIABLE> COLORS AND CALCULATING
			Pattern p2 = Pattern.compile("(?<=\\<variable\\>)(\\s*.*\\s*)(?=\\<\\/variable\\>)");
			Matcher m2 = p2.matcher(s);
			int finds = 0;
			while(m2.find()) {
				String match = m2.group(finds).replaceAll("\\s+", "").toLowerCase().replaceAll("l", level + "");
				String result = String.valueOf(Text.eval(match));
				
				s = s.replace(m2.group(), result.replace(".0", ""));
				finds++;
			}	
			
			// EDITING in VARIABLES INSIDE ()
			Pattern p1 = Pattern.compile("\\(.*?\\)");
			Matcher m1 = p1.matcher(s);
			while(m1.find()) {
				String match = m1.group().subSequence(1,  m1.group().length()-1).toString();
				
				s = selected ? s.replace(" (" + match + ")", "") : s.replace("(" + match + ")", MAIN + "(<secondary>"+ match + "<main>)");
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
	
	public void applyCooldown(Player p) {
		if(!ignoresCooldown && !cooldownCanceled) {
			this.setCooldownForPlayer(p, this.cooldown);
		}
		
		this.resetCooldown();
		this.setCooldownCanceled(false);
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
	
	public boolean sendAbilityMessage(Player p, String text) {
		new User(p).sendMessageWithPrefix(this.getName(), text);
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
	
	public Set<UUID> getEnabledPlayers(){
		return this.playersEnabled;
	}
	
	public void toggle(Player player, State state) {
		new User(player).sendMessageWithPrefix(this.getName(), this.getName() + ": " + state.getName());
	}
	
	/*
	 * UPDATING ENERGY
	 */
	@EventHandler	
	public void onEnergyUpdate(EnergyUpdateEvent e) {
		if(!(this instanceof IEnergyEditor) || !(this instanceof IConstant)) return;
		if(!(new User(e.getPlayer()).isUsingAbility(this))) return;
		
		this.run(true, true, e.getPlayer(), null, true, e);
	}
	
	
	/*
	 * DAMAGE EVENT TRIGGER
	 */
	@EventHandler
	public void onDamage(DamageEvent e) {
		if(!(new User(e.getDamager()).isUsingAbility(this))) return;
		if(this instanceof IPlayerDamagedByEntity) return;
		if(!(this instanceof IDamageTrigger)) return;
		
		
		if(!e.isCancelled()) {
			boolean success = this.run(false, true, e.getDamager(), null, false, e);
			
			if(!(e.getDamagee() instanceof Player)) return;
			if(!success) return;
			PlayerHit hit = new PlayerHit((Player) e.getDamagee(), (Player) e.getDamager(), e.getDamage(), null);
			hit.setCause("&a" + this.getName());
			e.setHit(hit);
		}
 	}
	
	/*
	 * DAMAGE BY ENTITY EVENT TRIGGER
	 */
	@EventHandler
	public void onDamageByEntity(DamagedByEntityEvent e) {
		if(!(new User(e.getDamagee()).isUsingAbility(this))) return;
		if(this instanceof IPlayerDamagedEntity) return;
		if(!(this instanceof IDamageTrigger)) return;
		
		
		if(!e.isCancelled()) {
			boolean success = this.run(false, true, e.getDamagee(), null, false, e);
			
			if(!(e.getDamager() instanceof Player)) return;
			if(!success) return;
			PlayerHit hit = new PlayerHit((Player) e.getDamager(), (Player) e.getDamagee(), e.getDamage(), null);
			hit.setCause("&a" + this.getName());
			e.setHit(hit);
		}
	}
	
	/*
	 * CONSTANT PASSIVES
	 */
	@EventHandler
	public void onUpdate(UpdateEvent e) {
		if(!(this instanceof IConstant) || this instanceof IEnergyEditor ) return;
		this.setMessage(null);

		for(Player p : Bukkit.getOnlinePlayers()) {
			if(this instanceof ITogglable && this.playersEnabled.contains(p.getUniqueId()) && !new User(p).isUsingAbility(this)){
				this.playersEnabled.remove(p.getUniqueId());
				((ITogglable) this).off(p);
				this.toggle(p, State.DISABLED);
				continue;
			}
			
			if(!(new User(p).isUsingAbility(this))) continue;
			
			boolean messages = true;
			if(this instanceof ITogglable && !this.playersEnabled.contains(p.getUniqueId())) continue;
			if(this instanceof IConstant && !(this instanceof ITogglable)) messages = false;
			
			boolean success = this.run(true, this instanceof IConstant && !(this instanceof ITogglable) ? true : false, p, null, messages, e);
			if(!success && this instanceof ITogglable && this.playersEnabled.contains(p.getUniqueId())) {
				((ITogglable) this).off(p);
				this.playersEnabled.remove(p.getUniqueId());
				this.toggle(p, State.DISABLED);
			}
		}
	}
	
	/*
	 * DROPPABLE/TOGGLEABLE ITEMS
	 */
	@EventHandler
	public void onDropEvent(PlayerDropItemEvent e) {
		if(!(new User(e.getPlayer()).isUsingAbility(this))) return;
		if(this instanceof ITogglable) {
			
			ItemStack holding = e.getItemDrop().getItemStack();
			if(this.match(holding) == null) return;
			
			e.setCancelled(true);
			
			// TOGGLING STATE AND SAVING
			UUID uuid = e.getPlayer().getUniqueId();
			State newState = this.playersEnabled.contains(uuid) ? State.DISABLED : State.ENABLED;
	
			
			// RUNNING IF ENABLED
			boolean success = true;
			if(newState == State.ENABLED) {
				success = this.run(e.getPlayer(), match(holding), true, e);
			}
	
			if(!success) return;
			
			if(playersEnabled.contains(e.getPlayer().getUniqueId())) {
				playersEnabled.remove(e.getPlayer().getUniqueId());
				newState = State.DISABLED;
				((ITogglable) this).off(e.getPlayer());
			} else {
				playersEnabled.add(e.getPlayer().getUniqueId());
				newState = State.ENABLED;
				((ITogglable) this).on(e.getPlayer());
			}
			
			// SENDING MESSAGE IF NOT NULL
			this.toggle(e.getPlayer(), newState);
			
		} else if (this instanceof IDroppable) {
			ItemStack holding = e.getItemDrop().getItemStack();
			if(this.match(holding) == null) return;
			
			e.setCancelled(true);
			
			this.run(e.getPlayer(), this.match(holding), true);
		}
	}
	
	@EventHandler
	public void onEvent(PlayerInteractEvent e) {
		
		// RIGHT CLICK ABILITIES
		if(this.getAbilityType().getEventType().equals(EventType.RIGHT_CLICK)
				&& (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
			
			Material item = e.getItem() == null ? Material.AIR : e.getItem().getType();
			
			ToolType match = match(item);
			if(match == null) return;
			
			run(e.getPlayer(), match, true);
		}
		
		// LEFT CLICK ABILITIES
		if(this.getAbilityType().getEventType().equals(EventType.LEFT_CLICK)
				&& (e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK))) {
			
			Material item = e.getItem() == null ? Material.AIR : e.getItem().getType();
			ToolType match = match(item);
			if(match == null) return;
			
			run(e.getPlayer(), match, true);
		}
		
	}
	
	private ToolType match(Material item) {
		ToolType toolType = null;
		for(ToolType type : this.getAbilityType().getToolTypes())
			if(type.getType().equals(item))
				toolType = type;
		
		return toolType;
	}
	
	private ToolType match(ItemStack item) {
		return match(item.getType());
	}
	
	/*
	 * CLEAR ENABLED PLAYERS ON LOG OFF
	 */
	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		if(this instanceof ITogglable && this.playersEnabled.contains(e.getPlayer().getUniqueId())){
			((ITogglable) this).off(e.getPlayer());
			this.playersEnabled.remove(e.getPlayer().getUniqueId());
		}
	}

}
