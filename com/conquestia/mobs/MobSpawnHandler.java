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
 * Handles the spawning of mobs. Calculates a 
 * level depending on distance from the closes 
 * spawn point. Sets the mobs name to show the
 * corresponding level.
 * 
 * @author ferrago
 */
public class MobSpawnHandler implements Listener {

    private static ArrayList<EntityType> notExempt = new ArrayList<EntityType>(); //List of mobs that we want to have a level
    ConquestiaMobs cqm; //Instance of instantiating plugin, used for non static methods we might need access to.
    
    Config mobConfig; //Users configuration file used to load spawn points and other settings.
    
    //User configuration settings
    Double distancePerLevel;
    Double healthMultiplier;

    /**
     * Constructor for creation of this handler. Initialize variables
     * load config, and register events.
     * 
     * @param plugin Calling plugin that creates this handler.
     */
    public MobSpawnHandler(JavaPlugin plugin) {
        cqm = (ConquestiaMobs) plugin;
        mobConfig = new Config(plugin, "Spawning" + File.separator + "MobSpawns");
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        addNotExemptEntities();
    }
    
    /**
     * Getter method for retrieving which entities 
     * we want to alter the spawn of.
     * 
     * @return Non exempt entities. 
     */
    public static ArrayList<EntityType> getNotExemptEntities() {
        return notExempt;
    }

    //Just provide a consise way of creating the list of non exempt entites
    //Potentially might be used to allow users the option of which mobs to exempt.
    private static void addNotExemptEntities() {
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

    /**
     * Uses a basic algorithm to compute the closest designated spawn location
     * to the monster that is spawning.
     * 
     * @param spawnPoints List of spawn points to use in calculations
     * @param eventLoc Where the monster spawned
     * @return The location from the spawnPoints list that is closest to our event location. Returns null if spawns is empty.
     */
    private Location getClosestSpawn(ArrayList<Location> spawnPoints, Location eventLoc) {
        ArrayList<Location> spawns = spawnPoints;
        Location closestSpawn = new Location(eventLoc.getWorld(), eventLoc.getX(), eventLoc.getY(), eventLoc.getZ());
        int lowestDistance = 999999999; //Hard Coded for ease
        if (!spawns.isEmpty()) {
            for (Location loc : spawns) {
                if (loc.distanceSquared(eventLoc) < lowestDistance) {
                    lowestDistance = (int) loc.distanceSquared(eventLoc);
                    closestSpawn.setX(loc.getX());
                    closestSpawn.setY(loc.getY());
                    closestSpawn.setZ(loc.getZ());
                }
            }
            return closestSpawn;
        } else {
            return null;
        }
    }

    /**
     * Method takes a location and returns the config file name for the spawn point.
     * 
     * @param closestPoint The location we wish to know the name of.
     * @return The config name of the input location. Returns null if the point is not in the config.
     */
    private String getSpawnPointName(Location closestPoint) {
        List<String> worlds = mobConfig.getConfig().getStringList("Worlds");

        for (String world : worlds) {
            if (world.equals(closestPoint.getWorld().getName())) {
                int spawnPointNumber = 1;
                String nextSpawn = "spawn" + spawnPointNumber;
                while (mobConfig.getConfig().contains(world + ".spawnLocations." + nextSpawn)) {
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

    /**
     * Calculates the appropriate level of the mob.
     * 
     * @param distance How far away is the closest spawn point?
     * @param spawnLocation Where did this mob spawn?
     * @param world Which world did the spawn occur in?
     * @param closestSpawn The closest spawn location.
     * @return The level of the mob.
     */
    private int getLevel(Double distance, Location spawnLocation, String world, Location closestSpawn) {
        String closestPointName = getSpawnPointName(closestSpawn);
        int startLevel = 0;
        if (closestPointName != null) {
            startLevel = mobConfig.getConfig().getInt(closestPointName + ".startLevel");
        }
        int wave = 0;
        distancePerLevel = mobConfig.getConfig().getDouble(world + ".DistancePerLevel", 35.0);
        int maxLevel = mobConfig.getConfig().getInt(world + ".MaxLevel", 0);
        if (cqm.getMobArena() != null && mobConfig.getConfig().getBoolean(world + ".MobArenaWaveLeveling", true) && ((com.garbagemule.MobArena.MobArena) cqm.getMobArena()).getArenaMaster().getArenaAtLocation(spawnLocation) != null) {
            wave = ((com.garbagemule.MobArena.MobArena) cqm.getMobArena()).getArenaMaster().getArenaAtLocation(spawnLocation).getWaveManager().getWaveNumber();
        }

        if (maxLevel != 0 && ((distance / distancePerLevel) + wave + startLevel) > maxLevel) {
            return maxLevel;
        } else {
            return (int) ((distance / distancePerLevel) + wave + startLevel);
        }

    }

    //Event handler for the mob spawn event. Passes off necessary information off to appropriate methods.
    @EventHandler(priority = EventPriority.MONITOR)
    public void OnMobSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == SpawnReason.SPAWNER) {
            event.getEntity().setMetadata("Spawner", new FixedMetadataValue(cqm, true));
        }
        if (notExempt.contains(event.getEntityType())) {
            ArrayList<Location> spawns = new ArrayList();
            List<String> worlds = mobConfig.getConfig().getStringList("Worlds");
            for (String world : worlds) {
                if (event.getLocation().getWorld().toString().toLowerCase().contains("craftworld{name=" + world.toLowerCase() + "}")) {
                    int spawnPointNumber = 1;
                    String nextSpawn = "spawn" + spawnPointNumber;
                    while (mobConfig.getConfig().contains(world + ".spawnLocations." + nextSpawn)) {

                        World currentWorld = Bukkit.getWorld(world);
                        Location newLoc = new Location(currentWorld, (double) mobConfig.getConfig().getInt(world + ".spawnLocations." + nextSpawn + ".x"), (double) mobConfig.getConfig().getInt(world + ".spawnLocations." + nextSpawn + ".y"), (double) mobConfig.getConfig().getInt(world + ".spawnLocations." + nextSpawn + ".z"));
                        spawns.add(newLoc);
                        spawnPointNumber++;
                        nextSpawn = "spawn" + spawnPointNumber;
                    }

                }
            }
            Location closestSpawn = getClosestSpawn(spawns, event.getLocation());
            if (closestSpawn != null) {
                int level = getLevel(closestSpawn.distance(event.getLocation()), event.getLocation(), event.getLocation().getWorld().getName(), closestSpawn);
                healthMultiplier = mobConfig.getConfig().getDouble(event.getLocation().getWorld().getName() + ".HealthMultiplier", 0.01);
                if (event.getEntity().getCustomName() != null && !event.getEntity().getCustomName().toLowerCase().contains("null")) {
                    event.getEntity().setCustomName(ChatColor.GOLD + "[Lvl: " + ChatColor.YELLOW + level + ChatColor.GOLD + "] " + ChatColor.WHITE + event.getEntity().getCustomName());
                } else {
                    event.getEntity().setCustomName(ChatColor.GOLD + "[Lvl: " + ChatColor.YELLOW + level + ChatColor.GOLD + "] " + ChatColor.WHITE + event.getEntityType().name());
                }
                double oldHealth = event.getEntity().getHealth();
                double newHealth = ((int) (oldHealth + oldHealth * (level * healthMultiplier)));
                event.getEntity().setMaxHealth(newHealth);
                event.getEntity().setHealth(newHealth - 0.01);
                if (mobConfig.getConfig().contains("NamePlatesAlwaysVisible") && mobConfig.getConfig().getBoolean("NamePlatesAlwaysVisible")) {
                    event.getEntity().setCustomNameVisible(true);
                }
            }

        }

    }

}
