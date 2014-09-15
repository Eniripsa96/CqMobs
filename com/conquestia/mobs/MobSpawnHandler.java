/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.conquestia.mobs;

import com.conquestia.mobs.Config.Config;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author ferrago
 */
public class MobSpawnHandler implements Listener {
    
    private static ArrayList<EntityType> notExempt = new ArrayList<EntityType>();
    Config mobConfig;
    Double distancePerLevel;
    Double healthMultiplier;
    ConquestiaMobs cqm;
    
    public MobSpawnHandler(JavaPlugin plugin)
    {
        cqm = (ConquestiaMobs)plugin;
        mobConfig = new Config(plugin, "Spawning" + File.separator + "MobSpawns");
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin); 
        addNotExemptEntities();
    }
    
    public static ArrayList<EntityType> getNotExemptEntities()
    {
        return notExempt;
    }
    
    private static void addNotExemptEntities()
    {
      notExempt.add(EntityType.BLAZE);
      notExempt.add(EntityType.CAVE_SPIDER);
      notExempt.add(EntityType.CREEPER);
      notExempt.add(EntityType.ENDERMAN);
      notExempt.add(EntityType.GHAST);
      notExempt.add(EntityType.GIANT);
      notExempt.add(EntityType.IRON_GOLEM);
      notExempt.add(EntityType.MAGMA_CUBE);
      notExempt.add(EntityType.PIG_ZOMBIE);
      notExempt.add(EntityType.SKELETON);
      notExempt.add(EntityType.SLIME);
      notExempt.add(EntityType.SNOWMAN);
      notExempt.add(EntityType.SPIDER);
      notExempt.add(EntityType.WITCH);
      notExempt.add(EntityType.WITHER);
      notExempt.add(EntityType.WOLF);
      notExempt.add(EntityType.ZOMBIE);
    }
    
    private Location getClosestSpawn(ArrayList<Location> spawnPoints, Location eventLoc)
    {
        ArrayList<Location> spawns = spawnPoints;
        Location closestSpawn = new Location(eventLoc.getWorld(), eventLoc.getX(), eventLoc.getY(), eventLoc.getZ());
        int lowestDistance = 999999999; //Hard Coded for ease
        if (!spawns.isEmpty()) 
        {
            for (Location loc : spawns)
            {
                if (loc.distanceSquared(eventLoc) < lowestDistance)
                {
                    lowestDistance = (int)loc.distanceSquared(eventLoc);
                    closestSpawn.setX(loc.getX());
                    closestSpawn.setY(loc.getY());
                    closestSpawn.setZ(loc.getZ());

                }
             }
            return closestSpawn;
        } 
        else
        {
            return null;
        }
    }
    
    private String getSpawnPointName(Location closestPoint) {
        List<String> worlds = mobConfig.getConfig().getStringList("Worlds");
        
        for (String world : worlds) {
            if (world.equals(closestPoint.getWorld().getName())) {
                int spawnPointNumber = 1;
                String nextSpawn = "spawn" + spawnPointNumber;
                while (mobConfig.getConfig().contains(world + ".spawnLocations." + nextSpawn))
                {
                    if (mobConfig.getConfig().getInt(world + ".spawnLocations." + nextSpawn + ".x") == closestPoint.getX() && mobConfig.getConfig().getInt(world + ".spawnLocations." + nextSpawn + ".y") == closestPoint.getY() && mobConfig.getConfig().getInt(world + ".spawnLocations." + nextSpawn + ".z") == closestPoint.getZ()) {
                        return (world + ".spawnLocations." + nextSpawn);
                    }
                    spawnPointNumber++;
                    nextSpawn = "spawn" + spawnPointNumber;
                }    
            }
        }
        return null;
        
    }   
    
    private int getLevel(Double distance, Location spawnLocation, String world, Location closestSpawn)
    {
        String closestPointName = getSpawnPointName(closestSpawn);
        int startLevel = 0;
        if (closestPointName != null) {
            startLevel = mobConfig.getConfig().getInt(closestPointName + ".startLevel");
        }
        int wave = 0;
        distancePerLevel = mobConfig.getConfig().getDouble(world + ".DistancePerLevel", 35.0);
        int maxLevel = mobConfig.getConfig().getInt(world + ".MaxLevel", 0);
        if (cqm.getMobArena() != null && mobConfig.getConfig().getBoolean(world + ".MobArenaWaveLeveling", true) && cqm.getMobArena().getArenaMaster().getArenaAtLocation(spawnLocation) != null)
        {
            wave = cqm.getMobArena().getArenaMaster().getArenaAtLocation(spawnLocation).getWaveManager().getWaveNumber();
        }
        
        if (maxLevel != 0 && ((distance / distancePerLevel) + wave + startLevel) > maxLevel)
        {
            return maxLevel;
        }
        else
        {
            return (int)((distance / distancePerLevel) + wave + startLevel);
        }
     
    }
    
    @EventHandler(priority=EventPriority.MONITOR)
    public void OnMobSpawn(CreatureSpawnEvent event)
    {
        if (event.getSpawnReason() == SpawnReason.SPAWNER) { 
            event.getEntity().setMetadata("Spawner", new FixedMetadataValue(cqm, true));
        }
        if (notExempt.contains(event.getEntityType()))
        {   
             ArrayList<Location> spawns = new ArrayList();
             List<String> worlds = mobConfig.getConfig().getStringList("Worlds");
             for (String world : worlds)
             {
                 if (event.getLocation().getWorld().toString().toLowerCase().contains("craftworld{name=" + world.toLowerCase() + "}"))
                 {
                    int spawnPointNumber = 1;
                    String nextSpawn = "spawn" + spawnPointNumber;
                    while (mobConfig.getConfig().contains(world + ".spawnLocations." + nextSpawn))
                    {
            
                        World currentWorld = Bukkit.getWorld(world);
                        Location newLoc = new Location(currentWorld, (double)mobConfig.getConfig().getInt(world + ".spawnLocations." + nextSpawn + ".x"), (double)mobConfig.getConfig().getInt(world + ".spawnLocations." + nextSpawn + ".y"), (double)mobConfig.getConfig().getInt(world + ".spawnLocations." + nextSpawn + ".z"));
                        spawns.add(newLoc);
                        spawnPointNumber++;
                        nextSpawn = "spawn" + spawnPointNumber;
                    }
                     
                 }
             }
             Location closestSpawn = getClosestSpawn(spawns, event.getLocation());
             if (closestSpawn != null)
             {
                 int level = getLevel(closestSpawn.distance(event.getLocation()), event.getLocation(), event.getLocation().getWorld().getName(), closestSpawn);
                 healthMultiplier = mobConfig.getConfig().getDouble(event.getLocation().getWorld().getName() + ".HealthMultiplier", 0.01);
                 if (event.getEntity().getCustomName() != null && !event.getEntity().getCustomName().toLowerCase().contains("null"))
                 {
                     event.getEntity().setCustomName(ChatColor.GOLD + "[Lvl: " + ChatColor.YELLOW + level + ChatColor.GOLD + "] " + ChatColor.WHITE + event.getEntity().getCustomName());
                 }
                 else
                 {
                    event.getEntity().setCustomName(ChatColor.GOLD + "[Lvl: " + ChatColor.YELLOW  + level + ChatColor.GOLD + "] " + ChatColor.WHITE + event.getEntityType().name());    
                 }
                 double oldHealth = event.getEntity().getHealth();
                 double newHealth = ((int)(oldHealth + oldHealth * (level * healthMultiplier)));
                 event.getEntity().setMaxHealth(newHealth);
                 event.getEntity().setHealth(newHealth-0.01);
                 if (mobConfig.getConfig().contains("NamePlatesAlwaysVisible") && mobConfig.getConfig().getBoolean("NamePlatesAlwaysVisible")) {
                    event.getEntity().setCustomNameVisible(true);
                 }
             }

                
             
        }
        
        
       

    }
    
}
