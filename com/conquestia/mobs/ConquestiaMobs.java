package com.conquestia.mobs;

import com.conquestia.mobs.Commands.CqmCommandHandler;
import com.conquestia.mobs.Config.Config;
import com.conquestia.mobs.MobArena.MobArenaHandler;
import java.io.File;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author ferrago
 */
public class ConquestiaMobs extends JavaPlugin implements CommandExecutor {
    
    Config mobConfig;
    
    /**
     * Registers this as a listener, and turns on handlers.
     * 
     */
    @Override
    public void onEnable()
    {
        mobConfig = new Config(this, "Spawning" + File.separator + "MobSpawns");
        mobConfig.saveDefaultConfig();
        setDefaults(mobConfig.getConfig());;
        setConfigForWorlds();
        mobConfig.saveConfig();
        TurnOnHandlers();
        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            public void run() {
                RefreshMobs();
                }
        }, 2400);
            
        getLogger().info("ConquestiaMobs Enabled!");
    }
    
    @Override
    public void onDisable()
    {
        HandlerList.unregisterAll(this);
        getLogger().info("ConquestiaMobs Disabled!");
    }
    
    /**
     * TurnOnHandlers
     * 
     * Initializes all utilized handlers to help keep
     * OnEnable clean and easily readable.
     * 
     */
    public void TurnOnHandlers()
    {
        //Command Handler
        CqmCommandHandler commander = new CqmCommandHandler(this);

        //Mob Handlers
        this.getLogger().info("Enabling Mob Handlers");
        new MobSpawnHandler(this);
        new MobDamageHandler(this);
        
        //Mob Arena Handler
        if (getMobArena() != null && mobConfig.getConfig().getBoolean("MobArenaWaveLeveling", false))
        {
            new MobArenaHandler(this);
        }
        else
        {
            this.getLogger().info("MobArena not detected or is disabled, MobArena Handler not enabled!");
        }
        
        if(mobConfig.getConfig().contains("ExperiencePerLevel") && mobConfig.getConfig().getDouble("ExperiencePerLevel") > 0.0)
        {
            if (mobConfig.getConfig().contains("HeroesExperience") && mobConfig.getConfig().getBoolean("HeroesExperience")) {
                new HeroesExperienceHandler(this, mobConfig.getConfig().getDouble("ExperiencePerLevel"), mobConfig.getConfig().getBoolean("MobArenaExperience", false), mobConfig.getConfig().getDouble("MobArenaExperienceScale", 0.0));
            }
            else if(mobConfig.getConfig().contains("SkillAPIExperience") && mobConfig.getConfig().getBoolean("SkillAPIExperience")) {
                new SkillAPIExperienceHandler(this, mobConfig.getConfig().getDouble("ExperiencePerLevel"));
            }
            else {
                new MobExperienceHandler(this, mobConfig.getConfig().getDouble("ExperiencePerLevel"));
            }
        }
        
        if (mobConfig.getConfig().contains("HologramUtils") && mobConfig.getConfig().getBoolean("HologramUtils") && Bukkit.getPluginManager().getPlugin("HolographicDisplays") != null) {
            HoloUtils holoUtility = new HoloUtils(this);
        } else {
            this.getLogger().info("Hologram Utils NOT enabled! Either disabled in config, or HolographicDisplays not present");
        }
        this.getLogger().info("Mob Handlers Enabled!");
    }
    
    
    /**
     * 
     * If MobArena is not on the server this returns null,
     * otherwise it return MobArena.
     * 
     * @return MobArena
     */
    public static Plugin getMobArena()
    {
        if (Bukkit.getPluginManager().getPlugin("MobArena") != null)
        {
            return Bukkit.getPluginManager().getPlugin("MobArena");
        }
        else
        {
            return null;
        }
    }
    
    
    /**
     * <p>Applies default values to a configuration section</p>
     * <p>This copies over all unset default values that were added</p>
     *
     * @param config configuration section to apply default values for
     */
    private void setDefaults(ConfigurationSection config) {
        if (config.getDefaultSection() == null) return;
        for (String key : config.getDefaultSection().getKeys(false)) {
            if (config.isConfigurationSection(key)) {
                setDefaults(config.getConfigurationSection(key));
            }
            else if (!config.isSet(key)) {
                config.set(key, config.get(key));
            }
        }
    }
    
    /**
     * 
     * Removes all monsters from the server.
     * 
     */
    public void RefreshMobs()
    {
        for (World world : Bukkit.getServer().getWorlds())
        {
            for (LivingEntity le : world.getLivingEntities())
            {
                if (MobSpawnHandler.getNotExemptEntities().contains(le.getType()))
                {
                    le.remove();
                }
            }
        }
    }
    
    /**
     * If the default config is generated create
     * 2 sample spawn points for every world on
     * the server.
     * 
     */
    public void setConfigForWorlds()
    {
        if (mobConfig.getConfig().getList("Worlds").contains("Example") && mobConfig.getConfig().getList("Worlds").size() < 2)
        {
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
            for (World world : Bukkit.getServer().getWorlds())
            {
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
}
