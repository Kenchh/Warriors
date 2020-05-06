package me.rey.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import me.rey.core.classes.abilities.ninja.*;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import me.rey.core.classes.ClassCondition;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.bandit.Blink;
import me.rey.core.classes.abilities.bandit.HiddenAssault;
import me.rey.core.classes.abilities.bandit.Leap;
import me.rey.core.classes.abilities.bandit.Recall;
import me.rey.core.classes.abilities.bandit.SmokeBomb;
import me.rey.core.classes.abilities.knight.HoldPosition;
import me.rey.core.classes.abilities.knight.Immunity;
import me.rey.core.classes.abilities.wizard.EnergyPool;
import me.rey.core.classes.abilities.wizard.EnergyRegeneration;
import me.rey.core.classes.abilities.wizard.FireBlast;
import me.rey.core.classes.abilities.wizard.MagmaBlade;
import me.rey.core.classes.abilities.wizard.NullBlade;
import me.rey.core.classes.conditions.ArcaneRepair;
import me.rey.core.classes.conditions.AssasinKB;
import me.rey.core.classes.conditions.Lightweight;
import me.rey.core.commands.Equip;
import me.rey.core.commands.Help;
import me.rey.core.commands.Skill;
import me.rey.core.database.SQLManager;
import me.rey.core.events.ClassEditorClickEvent;
import me.rey.core.events.DamageHandlerEvents;
import me.rey.core.events.DurabilityChangeEvent;
import me.rey.core.events.EquipClassEvent;
import me.rey.core.events.PlayerDeathEvent;
import me.rey.core.events.PlayerInteractChecker;
import me.rey.core.events.UseSoupEvent;
import me.rey.core.gui.GuiHelp;
import me.rey.core.items.Glow;
import me.rey.core.players.PlayerHitCache;
import me.rey.core.players.PlayerRunnableHandler;
import me.rey.core.pvp.Build;
import me.rey.core.utils.Text;

/*
 * This is the official Warriors plugin
 *
 * @author	Rey
 * @version 1.0.0
 * @since	2020-01-01
 */
public class Warriors extends JavaPlugin {

	// Gui
	public static GuiHelp guiHelp;

	// Cache
	public static ArrayList<Ability> abilityCache;
	public static ArrayList<ClassCondition> classConditions;
	public static Map<Player, ClassType> userCache;
	public static Map<Player, HashMap<ClassType, Build>> buildCache;
	public static PlayerHitCache hitCache;

	PluginManager pm = Bukkit.getServer().getPluginManager();
	Plugin plugin;
	static Warriors instance;

	//Config
	public static boolean deathMessagesEnabled;

	private final Logger logger = getLogger();
	private SQLManager sql;


	/*
	 * Called whenever the plugin is ENABLED.
	 */
	public void onEnable() {
		this.plugin = this;
		instance = this;

		loadConfig();
		initDatabase();

		this.registerCommands();
		this.registerListeners();


		guiHelp = new GuiHelp(this);

		new PlayerRunnableHandler(this);

		buildCache = new HashMap<>();
		userCache = new HashMap<>();
		this.initAbilityCache();
		this.initConditionCache();


		registerEnchantments();

		// HUNGER RATES

		deathMessagesEnabled = this.getConfig().getBoolean("kill-death-messages");
		logger.warning("Search for any errors in CONSOLE, they may be fatal to player gameplay");
	}


	/*
	 * Called whenever the plugin is DISABLED.
	 */
	public void onDisable() {
		this.plugin = null;


		userCache.clear();
		userCache = null;

		buildCache.clear();
		buildCache = null;


	    sql.onDisable();
	}

	private void initDatabase() {
		sql = new SQLManager(this);

		Text.log(this, "=====================================");
		Text.log(this, "");
		Text.log(this, "&5&lMySQL database connected!");
		Text.log(this, "&f&lWarriors now has access to all player data");
		Text.log(this, "");
		Text.log(this, "=====================================");
	}

	public SQLManager getSQLManager() {
		return sql;
	}

	public PlayerHitCache getHitCache() {
		if(hitCache == null)
			hitCache = new PlayerHitCache(this);
		return hitCache;
	}


	/*
	 * Register commands
	 * FORMAT: this.getCommand("cmd").setExecutor(new CommandExecutorClass());
	 */
	public void registerCommands() {
		this.getCommand("help").setExecutor(new Help());
		this.getCommand("skill").setExecutor(new Skill());
		this.getCommand("equip").setExecutor(new Equip());
	}


	/*
	 * Register listeners
	 * FORMAT: pm.registerEvents(new ListenerClass(), this);
	 */
	public void registerListeners() {
		pm.registerEvents(new ClassEditorClickEvent(), this);
		pm.registerEvents(new EquipClassEvent(), this);
		pm.registerEvents(new PlayerDeathEvent(), this);
		pm.registerEvents(new DurabilityChangeEvent(), this);
		pm.registerEvents(new DamageHandlerEvents(), this);
		pm.registerEvents(new UseSoupEvent(), this);
		pm.registerEvents(new PlayerInteractChecker(), this);
	}

	/*
	 * Return Main class instance
	 */
	public static Warriors getInstance() {
		return instance;
	}

	/*
	 * Initialize all ability listeners
	 */
	public void initConditionCache() {
		classConditions = new ArrayList<>(Arrays.asList(
				// NINJA
				new AssasinKB(),
				// WIZARD
				new ArcaneRepair(),
				// MARKSMAN
				// KNIGHT
				// BRUTE
				// BLACK
				new Lightweight()
				));

		for(ClassCondition condition : classConditions) {
			Bukkit.getPluginManager().registerEvents(condition, this);
		}
	}

	/*
	 * Initialize all ability listeners
	 */
	public void initAbilityCache() {
		abilityCache = new ArrayList<>(Arrays.asList(
				//BANDIT
				new Blink(),
				new Leap(),
				new Recall(),
				new SmokeBomb(),
				new HiddenAssault(),
				
				//NINJA
				new Backstab(),
				new Dash(),
				new Flash(),
				new Evade(),
				new BladeVortex(),
				
				//WIZARD
				new EnergyRegeneration(),
				new EnergyPool(),
				new FireBlast(),
				new MagmaBlade(),
				new NullBlade(),
				new me.rey.core.classes.abilities.wizard.Void(),
				
				//MAKRSMAN
				
				//KNIGHT
				new HoldPosition(),
				new Immunity()
				
				//BRUTE
				));
		
		for(Ability ability : abilityCache) {
			Bukkit.getPluginManager().registerEvents(ability, this);
			Text.log(this, String.format("Successfully loaded ability [%s]", ability.getName()));
		}
	}
	
	public void registerAbility(Ability ability) {
		if(abilityCache == null) {
			abilityCache = new ArrayList<>();
		}
		
		for(Ability query : getAbilitiesInCache()) {
			if(query.getIdLong() == ability.getIdLong()) {
				Text.log(this, "&4&lFAILED TO LOAD ABILITY " + ability.getName());
				plugin.getPluginLoader().disablePlugin(this);
				throw new AbilityIdentifierException("An ability already has ID: " + ability.getIdLong());
			}
		}
		
		abilityCache.add(ability);
		Bukkit.getPluginManager().registerEvents(ability, this);
	}
	
	public ArrayList<Ability> getAbilitiesInCache(){
		return abilityCache != null ? abilityCache : new ArrayList<>();
	}
	
	public ArrayList<Ability> getClassAbilities(ClassType classType){
		ArrayList<Ability> abilities = new ArrayList<>();
		for(Ability a : this.getAbilitiesInCache()) {
			if(a.getClassType().equals(classType)) abilities.add(a);
		}
		return abilities;
	}
	
	public void loadConfig() {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}
	
	private void registerEnchantments() {
		
		try {
			
			Field f = Enchantment.class.getDeclaredField("acceptingNew");
			f.setAccessible(true);
			f.set(null, true);
		} catch (Exception e ) {
			e.printStackTrace();
		}
		
		try {
			Glow glow = new Glow(255);
			Enchantment.registerEnchantment(glow);
		} catch(IllegalArgumentException e) {
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
