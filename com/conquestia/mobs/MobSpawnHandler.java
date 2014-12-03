package com.conquestia.mobs;

import com.conquestia.mobs.Config.Config;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handles the spawning of mobs. Calculates a level depending on distance from
 * the closes spawn point. Sets the mobs name to show the corresponding level.
 *
 * @author ferrago
 */
public class MobSpawnHandler implements Listener {

    private static ArrayList<EntityType> exempt = new ArrayList<EntityType>(); //List of mobs that we want to have a level
    private static final HashMap<String, Integer> levelMap = new HashMap<>();
    private static String format;

    ConquestiaMobs cqm; //Instance of instantiating plugin, used for non static methods we might need access to.
    Config mobConfig; //Users configuration file used to load spawn points and other settings.

    //User configuration settings
    Double distancePerLevel;
    Double healthMultiplier;

    /**
     * Constructor for creation of this handler. Initialize variables load
     * config, and register events.
     *
     * @param plugin Calling plugin that creates this handler.
     */
    public MobSpawnHandler(JavaPlugin plugin) {
        cqm = (ConquestiaMobs) plugin;
        mobConfig = new Config(plugin, "Spawning" + File.separator + "MobSpawns");
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        addExemptEntities();
        format = mobConfig.getConfig().getString("LevelNameFormat", "&6[Lvl: &e#&6]");
    }

    /**
     * Getter method for retrieving which entities we want to alter the spawn
     * of.
     *
     * @return Non exempt entities.
     */
    public static ArrayList<EntityType> getExemptEntities() {
        return exempt;
    }
    
    public static HashMap<String, Integer> getLevelMap() {
        return levelMap;
    }
    
    public static int getMobLevel(LivingEntity ent) {
        if (levelMap.containsKey(ent.getUniqueId().toString())) {
            return levelMap.get(ent.getUniqueId().toString());
        } 
        
        World world = ent.getWorld();
        LivingEntity newEnt = world.spawnCreature(ent.getLocation(), ent.getType());
        ent.remove();
        return 0;
        
    }

    //Just provide a consise way of creating the list of non exempt entites
    //Potentially might be used to allow users the option of which mobs to exempt.
    private static void addExemptEntities() {
        exempt.add(EntityType.BAT);
        exempt.add(EntityType.ARROW);
        exempt.add(EntityType.ARMOR_STAND);
        exempt.add(EntityType.BOAT);
        exempt.add(EntityType.CHICKEN);
        exempt.add(EntityType.COMPLEX_PART);
        exempt.add(EntityType.COW);
        exempt.add(EntityType.DROPPED_ITEM);
        exempt.add(EntityType.EGG);
        exempt.add(EntityType.ENDER_CRYSTAL);
        exempt.add(EntityType.ENDER_PEARL);
        exempt.add(EntityType.ENDER_SIGNAL);
        exempt.add(EntityType.EXPERIENCE_ORB);
        exempt.add(EntityType.FALLING_BLOCK);
        exempt.add(EntityType.FIREBALL);
        exempt.add(EntityType.FIREWORK);
        exempt.add(EntityType.FISHING_HOOK);
        exempt.add(EntityType.HORSE);
        exempt.add(EntityType.ITEM_FRAME);
        exempt.add(EntityType.LEASH_HITCH);
        exempt.add(EntityType.LIGHTNING);
        exempt.add(EntityType.MINECART);
        exempt.add(EntityType.MINECART_CHEST);
        exempt.add(EntityType.MINECART_COMMAND);
        exempt.add(EntityType.MINECART_FURNACE);
        exempt.add(EntityType.MINECART_HOPPER);
        exempt.add(EntityType.MINECART_MOB_SPAWNER);
        exempt.add(EntityType.MINECART_TNT);
        exempt.add(EntityType.MUSHROOM_COW);
        exempt.add(EntityType.OCELOT);
        exempt.add(EntityType.PAINTING);
        exempt.add(EntityType.PIG);
        exempt.add(EntityType.PLAYER);
        exempt.add(EntityType.PRIMED_TNT);
        exempt.add(EntityType.RABBIT);
        exempt.add(EntityType.SHEEP);
        exempt.add(EntityType.SMALL_FIREBALL);
        exempt.add(EntityType.SNOWBALL);
        exempt.add(EntityType.SPLASH_POTION);
        exempt.add(EntityType.SQUID);
        exempt.add(EntityType.THROWN_EXP_BOTTLE);
        exempt.add(EntityType.UNKNOWN);
        exempt.add(EntityType.VILLAGER);
        exempt.add(EntityType.WEATHER);
        exempt.add(EntityType.WITHER_SKULL);
    }

    /**
     * Uses a basic algorithm to compute the closest designated spawn location
     * to the monster that is spawning.
     *
     * @param spawnPoints List of spawn points to use in calculations
     * @param eventLoc Where the monster spawned
     * @return The location from the spawnPoints list that is closest to our
     * event location. Returns null if spawns is empty.
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
     * Method takes a location and returns the config file name for the spawn
     * point.
     *
     * @param closestPoint The location we wish to know the name of.
     * @return The config name of the input location. Returns null if the point
     * is not in the config.
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

    private void setName(LivingEntity ent, int level) {
        String newName = "";
        if (ent.getCustomName() != null && !ent.getCustomName().toLowerCase().contains("null")) {
            newName = ent.getCustomName();
            ConquestiaMobs.debug("Mob's name is a custom name!");
        } else {
            newName = ent.getType().toString();
            newName = newName.substring(0, 1).toUpperCase() + newName.substring(1).toLowerCase();
            ConquestiaMobs.debug("Mobs name is default name");
        }
        
        boolean prefix = mobConfig.getConfig().getBoolean("UsePrefix", true);
        boolean suffix = mobConfig.getConfig().getBoolean("UseSuffix", false);
        
        if (prefix) {
            newName = ChatColor.translateAlternateColorCodes('&', format.replace("#", level + "")) + " " + ChatColor.WHITE + newName;
        }
        
        if (suffix) {
            newName += " " + ChatColor.translateAlternateColorCodes('&', format.replace("#", level + ""));
        }
        
        ent.setCustomName(newName);
    }

    //Event handler for the mob spawn event. Passes off necessary information off to appropriate methods.
    @EventHandler(priority = EventPriority.MONITOR)
    public void OnMobSpawn(CreatureSpawnEvent event) {
        if (event.getCreatureType() == null) {
            return;
        }

        ConquestiaMobs.debug("Handling spawning of " + event.getCreatureType().getName());

        if (event.getSpawnReason() == SpawnReason.SPAWNER) {
            event.getEntity().setMetadata("Spawner", new FixedMetadataValue(cqm, true));
            ConquestiaMobs.debug("Mob spawned from spawner, marking mob!");
        }

        if (!exempt.contains(event.getEntityType())) {
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

            ConquestiaMobs.debug("Found closest spawn point, using " + closestSpawn.toString());

            if (closestSpawn != null) {
                int level = getLevel(closestSpawn.distance(event.getLocation()), event.getLocation(), event.getLocation().getWorld().getName(), closestSpawn);
                levelMap.put(event.getEntity().getUniqueId().toString(), level);
                healthMultiplier = mobConfig.getConfig().getDouble(event.getLocation().getWorld().getName() + ".HealthMultiplier", 0.01);
                setName(event.getEntity(), level);

                double oldHealth = event.getEntity().getHealth();
                double newHealth = ((oldHealth + oldHealth * (level * healthMultiplier)));

                ConquestiaMobs.debug("Spawned Health: " + oldHealth + " NewHealth: " + newHealth);

                if (newHealth > 1) {
                    newHealth += 2.0;
                }
                event.getEntity().setMaxHealth(newHealth);
                event.getEntity().setHealth(newHealth - 0.5);
                if (mobConfig.getConfig().contains("NamePlatesAlwaysVisible") && mobConfig.getConfig().getBoolean("NamePlatesAlwaysVisible")) {
                    event.getEntity().setCustomNameVisible(true);
                    ConquestiaMobs.debug("Made mob's name plate visilbe");
                }
            }

        }

    }

    //Really only used to clean up
    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if (levelMap.containsKey(event.getEntity().getUniqueId().toString())) {
            final String ent = event.getEntity().getUniqueId().toString();
            Bukkit.getScheduler().runTaskLater(cqm, new Runnable() {
                @Override
                public void run() {
                    levelMap.remove(ent);
                }
            }, 5);

        }
    }
}
