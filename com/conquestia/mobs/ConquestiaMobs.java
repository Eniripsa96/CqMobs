package com.conquestia.mobs;

import com.conquestia.mobs.Commands.CqmCommandHandler;
import com.conquestia.mobs.Config.Config;
import com.conquestia.mobs.MobArena.MobArenaHandler;
import java.io.File;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wolf;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Controls the enabling and disabling of the plugin
 *
 * @author ferrago
 */
public class ConquestiaMobs extends JavaPlugin implements CommandExecutor {

    private Config mobConfig; //Configuration file
    private static HoloUtils holoUtility; //Holo util
    private static boolean debug; //debug variable
    private static DisplayUtil display;
    
    

    /**
     * Sets up config file, generates defaults if needed. Enables handlers
     */
    @Override
    public void onEnable() {
        //Config Operations
        mobConfig = new Config(this, "Spawning" + File.separator + "MobSpawns"); //File should be located under Spawning/MobSpawns.yml
        mobConfig.saveDefaultConfig();
        setDefaults(mobConfig.getConfig());;
        setConfigForWorlds(); //Generates default config on first use
        generateNewConfig(); //Generates v0.2 config on first use
        mobConfig.saveConfig();

        //Handler Operations
        TurnOnHandlers();
        
        //Is debug enabled?
        debug = mobConfig.getConfig().getBoolean("Debug", false);

        //Lets the user know that we successfully enabled this plugin
        getLogger().info("ConquestiaMobs Enabled!");
        
    }
    
    @Override
    public void onLoad() {
        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            public void run() {
                Bukkit.getLogger().info("Clearing all mobs to fix any naming issues!");
                RefreshMobs();
            }
        }, 40);
    }

    @Override
    public void onDisable() {
        //Unregisters unneeded handlers, then alerts server the plugin was successfully disabled
        RefreshMobs();
        HandlerList.unregisterAll(this);
        getLogger().info("ConquestiaMobs Disabled!");
    }

    /**
     * Initializes all utilized handlers to help keep OnEnable clean and easily
     * readable.
     */
    public void TurnOnHandlers() {
        //Command Handler
        CqmCommandHandler commander = new CqmCommandHandler(this);

        //Mob Handlers
        info("Enabling Handlers");
        
        MobSpawnHandler mobSpawnHandler = new MobSpawnHandler(this);
        info("    " + ChatColor.GOLD + "[" + ChatColor.GREEN + "√" + ChatColor.GOLD + "] - Spawn Handler");
        
        MobDamageHandler mobDamageHandler = new MobDamageHandler(this);
        info("    " + ChatColor.GOLD + "[" + ChatColor.GREEN + "√" + ChatColor.GOLD + "] - Damage Handler");
        
        MoneyUtil moneyUtil = MoneyUtil.getInstance();
        info("    " + ChatColor.GOLD + "[" + ChatColor.GREEN + "√" + ChatColor.GOLD + "] - Money Utility");
        
        display = new DisplayUtil(mobConfig.getConfig().getBoolean("Spigot1-8", false), mobConfig.getConfig().getBoolean("MoneyDrops", false), mobConfig.getConfig().getBoolean("HologramUtils", false), mobConfig.getConfig().getBoolean("TitleMoneyDrop", false), mobConfig.getConfig().getBoolean("TitleXp", false));
        info("    " + ChatColor.GOLD + "[" + ChatColor.GREEN + "√" + ChatColor.GOLD + "] - Display Utility");

        //Mob Arena Handler
        if (getMobArena() != null && mobConfig.getConfig().getBoolean("MobArenaExperience", false)) {
            MobArenaHandler mobArenaHandler = new MobArenaHandler(this);
            info("    " + ChatColor.GOLD + "[" + ChatColor.GREEN + "√" + ChatColor.GOLD + "] - Mob Arena Handler");
        } else {
            info("    " + ChatColor.GOLD + "[" + ChatColor.RED + "X" + ChatColor.GOLD + "] - Mob Arena Handler - Mob Arena not detected, or disabled in settings!");
        }
        
        //Experience Handlers
        if (mobConfig.getConfig().contains("ExperiencePerLevel") && mobConfig.getConfig().getDouble("ExperiencePerLevel") > 0.0) {
            if (mobConfig.getConfig().contains("HeroesExperience") && mobConfig.getConfig().getBoolean("HeroesExperience")) {
                HeroesExperienceHandler heroesExperienceHandler = new HeroesExperienceHandler(this, mobConfig.getConfig().getDouble("ExperiencePerLevel"), mobConfig.getConfig().getBoolean("MobArenaExperience", false) && getMobArena() != null, mobConfig.getConfig().getDouble("MobArenaExperienceScale", 0.0), mobConfig.getConfig().getBoolean("MoneyDrops", false), debug);
                info("    " + ChatColor.GOLD + "[" + ChatColor.GREEN + "√" + ChatColor.GOLD + "] - Heroes Expereince Handler");
            } else if (mobConfig.getConfig().contains("SkillAPIExperience") && mobConfig.getConfig().getBoolean("SkillAPIExperience")) {
                SkillAPIExperienceHandler skillAPIExperienceHandler = new SkillAPIExperienceHandler(this, mobConfig.getConfig().getDouble("ExperiencePerLevel"), mobConfig.getConfig().getBoolean("MobArenaExperience", false) && getMobArena() != null, mobConfig.getConfig().getDouble("MobArenaExperienceScale", 0.0), mobConfig.getConfig().getBoolean("MoneyDrops", false), debug);
                info("    " + ChatColor.GOLD + "[" + ChatColor.GREEN + "√" + ChatColor.GOLD + "] - SkillAPI Experience Handler");
            } else {
                MobExperienceHandler mobExperienceHandler = new MobExperienceHandler(this, mobConfig.getConfig().getDouble("ExperiencePerLevel"), mobConfig.getConfig().getBoolean("MoneyDrop", false), mobConfig.getConfig().getBoolean("HologramUtils", false) && Bukkit.getPluginManager().getPlugin("HolographicDisplays") != null);
                info("    " + ChatColor.GOLD + "[" + ChatColor.GREEN + "√" + ChatColor.GOLD + "] - Vanilla Experience Handler");
            }
        }
        
        
        //Hologram Utilities
        if (mobConfig.getConfig().contains("HologramUtils") && mobConfig.getConfig().getBoolean("HologramUtils") && Bukkit.getPluginManager().getPlugin("HolographicDisplays") != null) {
            holoUtility = new HoloUtils(this);
            info("    " + ChatColor.GOLD + "[" + ChatColor.GREEN + "√" + ChatColor.GOLD + "] - Hologram Utility");
        } else {
            info("    " + ChatColor.GOLD + "[" + ChatColor.RED + "X" + ChatColor.GOLD + "] - Hologram Utility - Either disabled in config, or plugin not detected!");
            
        }
        
        
        //Lets server know that the handlers were successfully enabled
        info("Mob Handlers Enabled!");
    }

    /**
     * If MobArena is not on the server this returns null, otherwise it return
     * MobArena plugin.
     * 
     * @return MobArena The instance of MobArena running on the server
     */
    public static Plugin getMobArena() {
        return Bukkit.getPluginManager().getPlugin("MobArena");
    }
    
    public static DisplayUtil getDisplay() {
        return display;
    }

    /**
     * Applies default values to a configuration section
     * This copies over all unset default values that were added
     *
     * @param config configuration section to apply default values for
     */
    private void setDefaults(ConfigurationSection config) {
        if (config.getDefaultSection() == null) {
            return;
        }
        for (String key : config.getDefaultSection().getKeys(false)) {
            if (config.isConfigurationSection(key)) {
                setDefaults(config.getConfigurationSection(key));
            } else if (!config.isSet(key)) {
                config.set(key, config.get(key));
            }
        }
    }

    /**
     * Removes all monsters from the server.
     */
    public void RefreshMobs() {
        for (World world : Bukkit.getServer().getWorlds()) {
            for (LivingEntity le : world.getLivingEntities()) {
                if (le.getType() == EntityType.OCELOT || le.getType() == EntityType.WOLF) {
                    if (le.getType() == EntityType.WOLF) {
                        Wolf wolf = (Wolf)le;
                        if (!wolf.isTamed()) {
                            wolf.remove();
                        }
                    }
                } else if (!MobSpawnHandler.getExemptEntities().contains(le.getType())) {
                    le.remove();
                }
            }
        }
    }

    /**
     * If the default config is generated create 2 sample spawn points for every
     * world on the server and generate all default values.
     */
    public void setConfigForWorlds() {
        if (mobConfig.getConfig().getList("Worlds").contains("Example") && mobConfig.getConfig().getList("Worlds").size() < 2) {
            info("Default config found! Generating example settings for your worlds.");
            ArrayList<String> worlds = new ArrayList<String>();
            mobConfig.getConfig().createSection("ExperiencePerLevel");
            mobConfig.getConfig().set("ExperiencePerLevel", 0);
            mobConfig.getConfig().createSection("NamePlatesAlwaysVisible");
            mobConfig.getConfig().set("NamePlatesAlwaysVisible", false);
            mobConfig.getConfig().createSection("HeroesExperience");
            mobConfig.getConfig().set("HeroesExperience", false);
            mobConfig.getConfig().createSection("SkillAPIExperience");
            mobConfig.getConfig().set("SkillAPIExperience", false);
            mobConfig.getConfig().createSection("MobArenaExperience");
            mobConfig.getConfig().set("MobArenaExperience", false);
            mobConfig.getConfig().createSection("MobArenaExperienceScale");
            mobConfig.getConfig().set("MobArenaExperienceScale", false);
            mobConfig.getConfig().createSection("MoneyDrop");
            mobConfig.getConfig().set("MoneyDrop", true);
            mobConfig.getConfig().createSection("HologramUtils");
            mobConfig.getConfig().set("HologramUtils", true);
            mobConfig.getConfig().createSection("DynamicFireDamage");
            mobConfig.getConfig().set("DynamicFireDamage", true);
            mobConfig.getConfig().createSection("LevelNameFormat");
            mobConfig.getConfig().set("LevelNameFormat", "&6[Lvl: &e#&6]");
            mobConfig.getConfig().createSection("UsePrefix");
            mobConfig.getConfig().set("UsePrefix", true);
            mobConfig.getConfig().createSection("UseSuffix");
            mobConfig.getConfig().set("UseSuffix", false);
            mobConfig.getConfig().createSection("Spigot1-8");
            mobConfig.getConfig().set("Spigot1-8", false);
            mobConfig.getConfig().createSection("TitleMoneyDrop");
            mobConfig.getConfig().set("TitleMoneyDrop", false);
            mobConfig.getConfig().createSection("TitleXp");
            mobConfig.getConfig().set("TitleXp", false);
            
            for (World world : Bukkit.getServer().getWorlds()) {
                worlds.add(world.getName());
                mobConfig.getConfig().createSection(world.getName());
                mobConfig.getConfig().createSection(world.getName() + ".MobArenaWaveLeveling");
                mobConfig.getConfig().set(world.getName() + ".MobArenaWaveLeveling", true);
                mobConfig.getConfig().createSection(world.getName() + ".DamageModifierEnabled");
                mobConfig.getConfig().set(world.getName() + ".DamageModifierEnabled", false);
                mobConfig.getConfig().createSection(world.getName() + ".MaxLevel");
                mobConfig.getConfig().set(world.getName() + ".MaxLevel", 0);
                mobConfig.getConfig().createSection(world.getName() + ".DistancePerLevel");
                mobConfig.getConfig().set(world.getName() + ".DistancePerLevel", 35);
                mobConfig.getConfig().createSection(world.getName() + ".HealthMultiplier");
                mobConfig.getConfig().set(world.getName() + ".HealthMultiplier", 0.2);
                mobConfig.getConfig().createSection(world.getName() + ".DamageMultiplier");
                mobConfig.getConfig().set(world.getName() + ".DamageMultiplier", 1);
                mobConfig.getConfig().createSection(world.getName() + ".spawnLocations");
                mobConfig.getConfig().createSection(world.getName() + ".spawnLocations.spawn1");
                mobConfig.getConfig().createSection(world.getName() + ".spawnLocations.spawn1.startLevel");
                mobConfig.getConfig().set(world.getName() + ".spawnLocations.spawn1.startLevel", 1);
                mobConfig.getConfig().createSection(world.getName() + ".spawnLocations.spawn1.x");
                mobConfig.getConfig().set(world.getName() + ".spawnLocations.spawn1.x", 0.0);
                mobConfig.getConfig().createSection(world.getName() + ".spawnLocations.spawn1.y");
                mobConfig.getConfig().set(world.getName() + ".spawnLocations.spawn1.y", 0.0);
                mobConfig.getConfig().createSection(world.getName() + ".spawnLocations.spawn1.z");
                mobConfig.getConfig().set(world.getName() + ".spawnLocations.spawn1.z", 0.0);
                mobConfig.getConfig().createSection(world.getName() + ".spawnLocations.spawn2");
                mobConfig.getConfig().createSection(world.getName() + ".spawnLocations.spawn2.startLevel");
                mobConfig.getConfig().set(world.getName() + ".spawnLocations.spawn2.startLevel", 1);
                mobConfig.getConfig().createSection(world.getName() + ".spawnLocations.spawn2.x");
                mobConfig.getConfig().set(world.getName() + ".spawnLocations.spawn2.x", 100.0);
                mobConfig.getConfig().createSection(world.getName() + ".spawnLocations.spawn2.y");
                mobConfig.getConfig().set(world.getName() + ".spawnLocations.spawn2.y", 100.0);
                mobConfig.getConfig().createSection(world.getName() + ".spawnLocations.spawn2.z");
                mobConfig.getConfig().set(world.getName() + ".spawnLocations.spawn2.z", 100.0);
            }
            mobConfig.getConfig().set("Worlds", worlds);

        }
    }
    
    /**
     * Generates v2.1b config on first time use
     * 
     */
    public void generateNewConfig() {
        
        if (!mobConfig.getConfig().contains("MoneyDrops")) {
            mobConfig.getConfig().createSection("MoneyDrops");
            mobConfig.getConfig().set("MoneyDrops", true);
        }
        
        if (!mobConfig.getConfig().contains("HologramUtils")) {
            mobConfig.getConfig().createSection("HologramUtils");
            mobConfig.getConfig().set("HologramUtils", true);
        }
        
        if (!mobConfig.getConfig().contains("DynamicFireDamage")) {
            mobConfig.getConfig().createSection("DynamicFireDamage");
            mobConfig.getConfig().set("DynamicFireDamage", true);    
        }
        
        if (!mobConfig.getConfig().contains("LevelNameFormat")) {
            mobConfig.getConfig().createSection("LevelNameFormat");
            mobConfig.getConfig().set("LevelNameFormat", "&6[Lvl: &e#&6]");
        }
        
        if (!mobConfig.getConfig().contains("UsePrefix")) {
            mobConfig.getConfig().createSection("UsePrefix");
            mobConfig.getConfig().set("UsePrefix", true);
        }
        
        if (!mobConfig.getConfig().contains("UseSuffix")) {
            mobConfig.getConfig().createSection("UseSuffix");
            mobConfig.getConfig().set("UseSuffix", false);
        }
        
        if (!mobConfig.getConfig().contains("Spigot1-8")) {
            mobConfig.getConfig().createSection("Spigot1-8");
            mobConfig.getConfig().set("Spigot1-8", false);
        }
        
        if (!mobConfig.getConfig().contains("TitleXp")) {
            mobConfig.getConfig().createSection("TitleXp");
            mobConfig.getConfig().set("TitleXp", false);
        }
        
        if (!mobConfig.getConfig().contains("TitleMoneyDrop")) {
            mobConfig.getConfig().createSection("TitleMoneyDrop");
            mobConfig.getConfig().set("TitleMoneyDrop", false);
        }
        
    }
    
    /**
     * Gets the hologram utility object.
     * 
     * @return Hologram utility object. 
     */
    public static HoloUtils getHoloUtil() {
        return holoUtility;
    }
    
    public static void info(String infoMsg) {
            if (Bukkit.getConsoleSender() != null) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[" + ChatColor.BLUE + "Conquestia" + ChatColor.YELLOW + "Mobs" + ChatColor.GOLD +"] " + ChatColor.WHITE + infoMsg);
            } else {
                Bukkit.getLogger().info("[ConquestiaMobs]" + infoMsg);
            }    
    }
    
    public static void debug(String debugMsg) {
        if (debug) {
            if (Bukkit.getConsoleSender() != null) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + ChatColor.DARK_PURPLE + "CQM DEBUG" + ChatColor.RED  + "] " + ChatColor.WHITE + debugMsg);
            } else {
                Bukkit.getLogger().info("[DEBUG] " + debugMsg);
            }
        }
    }
    
}
