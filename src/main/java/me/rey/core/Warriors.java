package me.rey.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import me.rey.core.classes.ClassCondition;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.assassin.axe.Dash;
import me.rey.core.classes.abilities.assassin.axe.Flash;
import me.rey.core.classes.abilities.assassin.bow.BlindingArrow;
import me.rey.core.classes.abilities.assassin.bow.Disarm;
import me.rey.core.classes.abilities.assassin.bow.WitheredArrow;
import me.rey.core.classes.abilities.assassin.passive_a.HiddenAssault;
import me.rey.core.classes.abilities.assassin.passive_a.SmokeBomb;
import me.rey.core.classes.abilities.assassin.passive_b.BlitzStrikes;
import me.rey.core.classes.abilities.assassin.sword.BladeVortex;
import me.rey.core.classes.abilities.assassin.sword.Evade;
import me.rey.core.classes.abilities.bandit.axe.Blink;
import me.rey.core.classes.abilities.bandit.axe.Leap;
import me.rey.core.classes.abilities.bandit.passive_a.BruteForce;
import me.rey.core.classes.abilities.bandit.passive_a.Recall;
import me.rey.core.classes.abilities.bandit.passive_b.Backstab;
import me.rey.core.classes.abilities.bandit.passive_b.RapidSuccession;
import me.rey.core.classes.abilities.druid.axe.FireBlast;
import me.rey.core.classes.abilities.druid.passive_a.Void;
import me.rey.core.classes.abilities.druid.passive_b.MagmaBlade;
import me.rey.core.classes.abilities.druid.passive_b.NullBlade;
import me.rey.core.classes.abilities.druid.passive_c.EnergyPool;
import me.rey.core.classes.abilities.druid.passive_c.EnergyRegeneration;
import me.rey.core.classes.abilities.knight.axe.HoldPosition;
import me.rey.core.classes.abilities.knight.sword.Immunity;
import me.rey.core.classes.abilities.shaman.spade.Tornado;
import me.rey.core.classes.conditions.ArcaneRepair;
import me.rey.core.classes.conditions.Lightweight;
import me.rey.core.classes.conditions.Vigour;
import me.rey.core.combat.DamageHandler;
import me.rey.core.commands.Equip;
import me.rey.core.commands.Help;
import me.rey.core.commands.Skill;
import me.rey.core.database.SQLManager;
import me.rey.core.events.BuildHandler;
import me.rey.core.events.ClassEditorClickEvent;
import me.rey.core.events.ClassHandler;
import me.rey.core.events.DurabilityChangeEvent;
import me.rey.core.events.PlayerDeathEvent;
import me.rey.core.events.PlayerInteractChecker;
import me.rey.core.events.UseSoupEvent;
import me.rey.core.gui.GuiHelp;
import me.rey.core.items.Glow;
import me.rey.core.players.PlayerRunnableHandler;
import me.rey.core.players.combat.PlayerHitCache;
import me.rey.core.pvp.Build;
import me.rey.core.utils.Text;

/*
 * This is the official Warriors plugin
 *
 * @author	Rey, Kenchh
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
	public static Map<UUID, HashMap<ClassType, Build[]>> buildCache;
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
		new ClassHandler();

		userCache = new HashMap<>();
		this.initAbilityCache();
		this.initConditionCache();

		registerEnchantments();

		// HUNGER RATES

		deathMessagesEnabled = this.getConfig().getBoolean("kill-death-messages");
		logger.warning("Search for any errors in CONSOLE, they may be fatal to player gameplay");
		buildCache = this.getSQLManager().loadAllBuilds();
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
		pm.registerEvents(new BuildHandler(), this);
		pm.registerEvents(new PlayerDeathEvent(), this);
		pm.registerEvents(new DurabilityChangeEvent(), this);
		pm.registerEvents(new DamageHandler(), this);
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
				new Vigour(),
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
				new BlindingArrow(),
				new Blink(),
				new BruteForce(),
				new Disarm(),
				new HiddenAssault(),
				new WitheredArrow(),
				new Leap(),
				new Recall(),
				new RapidSuccession(),
				new SmokeBomb(),
				
				//NINJA
				new Backstab(),
				new BladeVortex(),
				new BlitzStrikes(),
				new Dash(),
				new Flash(),
				new Evade(),
				
				//WIZARD
				new EnergyRegeneration(),
				new EnergyPool(),
				new FireBlast(),
				new MagmaBlade(),
				new NullBlade(),
				new Tornado(),
				new Void(),
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
