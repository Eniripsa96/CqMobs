/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.conquestia.mobs.MobArena;

import com.conquestia.mobs.Config.Config;
import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handles functionality of MobArena mobs
 * 
 * @author ferrago
 */
public class MobArenaHandler implements Listener {
    private final Config mobConfig;
    private final double modifier;
    private final double waveModifier;
    public MobArenaHandler(JavaPlugin plugin)
    {
      Bukkit.getServer().getPluginManager().registerEvents(this, plugin);  
      mobConfig = new Config(plugin, "Spawning" + File.separator + "MobSpawns");
      modifier = mobConfig.getConfig().getDouble("ExperienceInMobArenaMultiplier", 1);
      waveModifier = mobConfig.getConfig().getDouble("WaveModifier", 1);
    }
    
    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=false)
    public void onMobArenaMonsterDeath(EntityDeathEvent event)
    {
        
    }

    
}
