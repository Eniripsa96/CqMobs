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
    private static boolean debug;

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
        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            public void run() {
                Bukkit.getLogger().info("Clearing all mobs to fix any naming issues!");
                RefreshMobs();
            }
        }, 20);
        
    }

    @Override
    public void onDisable() {
        //Unregisters unneeded handlers, then alerts server the plugin was successfully disabled
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
        this.getLogger().info("Enabling Mob Handlers");
        MobSpawnHandler mobSpawnHandler = new MobSpawnHandler(this);
        MobDamageHandler mobDamageHandler = new MobDamageHandler(this);

        //Mob Arena Handler
        if (getMobArena() != null && mobConfig.getConfig().getBoolean("MobArenaExperience", false)) {
            MobArenaHandler mobArenaHandler = new MobArenaHandler(this);
            this.getLogger().info("MobArena Handler created! MobArena experience rates are now being modified!");
        } else {
            this.getLogger().info("MobArena not detected or is disabled, MobArena Handler not enabled!");
        }
        
        //Experience Handlers
        if (mobConfig.getConfig().contains("ExperiencePerLevel") && mobConfig.getConfig().getDouble("ExperiencePerLevel") > 0.0) {
            if (mobConfig.getConfig().contains("HeroesExperience") && mobConfig.getConfig().getBoolean("HeroesExperience")) {
                HeroesExperienceHandler heroesExperienceHandler = new HeroesExperienceHandler(this, mobConfig.getConfig().getDouble("ExperiencePerLevel"), mobConfig.getConfig().getBoolean("MobArenaExperience", false), mobConfig.getConfig().getDouble("MobArenaExperienceScale", 0.0), mobConfig.getConfig().getBoolean("MoneyDrop", false), mobConfig.getConfig().getBoolean("HologramUtils", false) && Bukkit.getPluginManager().getPlugin("HolographicDisplays") != null);
            } else if (mobConfig.getConfig().contains("SkillAPIExperience") && mobConfig.getConfig().getBoolean("SkillAPIExperience")) {
                SkillAPIExperienceHandler skillAPIExperienceHandler = new SkillAPIExperienceHandler(this, mobConfig.getConfig().getDouble("ExperiencePerLevel"));
            } else {
                MobExperienceHandler mobExperienceHandler = new MobExperienceHandler(this, mobConfig.getConfig().getDouble("ExperiencePerLevel"));
            }
        }
        
        
        //Hologram Utilities
        if (mobConfig.getConfig().contains("HologramUtils") && mobConfig.getConfig().getBoolean("HologramUtils") && Bukkit.getPluginManager().getPlugin("HolographicDisplays") != null) {
            holoUtility = new HoloUtils(this);
        } else {
            this.getLogger().info("Hologram Utils NOT enabled! Either disabled in config, or HolographicDisplays not present");
            
        }
        
        //Lets server know that the handlers were successfully enabled
        this.getLogger().info("Mob Handlers Enabled!");
    }

    /**
     * If MobArena is not on the server this returns null, otherwise it return
     * MobArena plugin.
     * 
     * @return MobArena The instance of MobArena running on the server
     */
    public static Plugin getMobArena() {
        if (Bukkit.getPluginManager().getPlugin("MobArena") != null) {
            return Bukkit.getPluginManager().getPlugin("MobArena");
        } else {
            return null;
        }
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
                    continue;
                } else if (MobSpawnHandler.getNotExemptEntities().contains(le.getType())) {
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
            this.getLogger().info("Default config found! Generating example settings for your worlds.");
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
     * Generates v0.2 config on first time use
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
        
    }
    
    /**
     * Gets the hologram utility object.
     * 
     * @return Hologram utility object. 
     */
    public static HoloUtils getHoloUtil() {
        return holoUtility;
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
