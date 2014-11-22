package com.conquestia.mobs;

import com.conquestia.mobs.Config.Config;
import java.io.File;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Golem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handles mob damages. Adding modified damage values
 * for higher level mobs.
 * 
 * @author ferrago
 */
public class MobDamageHandler implements Listener {

    ConquestiaMobs cqm; // Calling plugin
    Config mobConfig; // Main config file
    boolean dynamicFireDamage; //Modify fire damage?

    
    /**
     * Initialize the config variable and cqm.
     * Register the required listeners.
     * 
     * @param plugin Initializing plugin
     */
    public MobDamageHandler(JavaPlugin plugin) {
        mobConfig = new Config(plugin, "Spawning" + File.separator + "MobSpawns");
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        dynamicFireDamage = mobConfig.getConfig().getBoolean("DynamicFireDamage");
        cqm = (ConquestiaMobs) plugin;
    }

    /**
     * Retrieve the monsters level from it's name string.
     * 
     * @param entityName the name to parse for the level
     * @return returns the monster's level
     */
    private int getMobLevel(String entityName) {
        return Integer.parseInt(entityName.substring(entityName.indexOf(":") + 2, entityName.indexOf("]")));
    }

    /**
     * Handles fire damage
     * @param event 
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onFireDamage(EntityDamageEvent event) {
        //If we don't want fire damage modifed then just exit.
        if (!dynamicFireDamage) {
            return;
        } else if(event.getEntity() instanceof LivingEntity && event.getCause() == DamageCause.FIRE_TICK) {

            event.setDamage(((LivingEntity) event.getEntity()).getMaxHealth() * 0.02);
        }
    }

    /**
     * Handles mob damage, computes 
     * level damage, as well as item in
     * the mobs hand. (if applicable)
     * 
     * @param event the triggering event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void OnMobDamage(EntityDamageByEntityEvent event) {
        if (!mobConfig.getConfig().contains(event.getEntity().getWorld().getName() + ".DamageModifierEnabled") || !mobConfig.getConfig().getBoolean(event.getEntity().getWorld().getName() + ".DamageModifierEnabled")) {
            return;
        }

        if (event.getDamager() instanceof Golem || event.getDamager() instanceof Monster) {
            LivingEntity le = (LivingEntity) event.getDamager();
            if (le.getCustomName() != null && le.getCustomName().toLowerCase().contains("lvl")) {
                int damageFromHand = 0;
                if (le != null && le.getEquipment() != null && le.getEquipment().getItemInHand() != null && le.getEquipment().getItemInHand().getItemMeta() != null && le.getEquipment().getItemInHand().getItemMeta().getLore() != null) {
                    List<String> loreFromHand = le.getEquipment().getItemInHand().getItemMeta().getLore();
                    for (String lore : loreFromHand) {
                        if (ChatColor.stripColor(lore).toLowerCase().contains("damage:")) {
                            damageFromHand = Integer.parseInt(ChatColor.stripColor(lore).substring(ChatColor.stripColor(lore).toLowerCase().indexOf("+") + 1));
                        }
                    }
                }
                int level = getMobLevel(ChatColor.stripColor(le.getCustomName()));
                double damageMultiplier = mobConfig.getConfig().getDouble(event.getEntity().getWorld().getName() + ".DamageMultiplier", 0.1);
                double newDamage = (event.getDamage() + (event.getDamage() * (level * damageMultiplier)));
                newDamage += damageFromHand;
                event.setDamage(newDamage);
                
                ConquestiaMobs.debug("Damage Debug: " + ChatColor.AQUA + le.getCustomName() + ChatColor.WHITE + " dealt " + ChatColor.DARK_GREEN + newDamage + ChatColor.WHITE + " with a damageMultiplier setting of " + ChatColor.GOLD + damageMultiplier);

            }
        }
        if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Monster) {
            LivingEntity le = (LivingEntity) (((Projectile) (event.getDamager())).getShooter());
            if (le.getCustomName() != null && le.getCustomName().toLowerCase().contains("lvl")) {
                int damageFromHand = 0;
                if (le.getEquipment().getItemInHand() != null && le.getEquipment().getItemInHand().getItemMeta() != null && le.getEquipment().getItemInHand().getItemMeta().getLore() != null) {
                    List<String> loreFromHand = le.getEquipment().getItemInHand().getItemMeta().getLore();
                    for (String lore : loreFromHand) {
                        if (ChatColor.stripColor(lore).toLowerCase().contains("damage:")) {
                            damageFromHand = Integer.parseInt(ChatColor.stripColor(lore).substring(ChatColor.stripColor(lore).toLowerCase().indexOf("+") + 1));
                        }
                    }
                }
                int level = getMobLevel(ChatColor.stripColor(le.getCustomName()));
                double damageMultiplier = mobConfig.getConfig().getDouble(event.getEntity().getWorld().getName() + ".DamageMultiplier", 0.1);
                double newDamage = (event.getDamage() + (event.getDamage() * (level * damageMultiplier)));

                int wave = 0;

                //If MobArena is on the server & is enabled
                if (cqm.getMobArena() != null && mobConfig.getConfig().getBoolean("MobArenaWaveLeveling", false) && ((com.garbagemule.MobArena.MobArena) cqm.getMobArena()).getArenaMaster().getArenaAtLocation(event.getDamager().getLocation()) != null) {
                    wave = ((com.garbagemule.MobArena.MobArena) cqm.getMobArena()).getArenaMaster().getArenaAtLocation(event.getDamager().getLocation()).getWaveManager().getWaveNumber();
                }

                newDamage += damageFromHand;
                newDamage += wave;
                event.setDamage(newDamage);
                ConquestiaMobs.debug("Damage Debug: " + ChatColor.AQUA + le.getCustomName() + ChatColor.WHITE + " dealt " + ChatColor.DARK_GREEN + newDamage + ChatColor.WHITE + " with a damageMultiplier setting of " + ChatColor.GOLD + damageMultiplier);
            }
        }

        if (event.getDamager() instanceof Blaze || event.getDamager() instanceof SmallFireball || event.getDamager() instanceof Fireball) {
            int level = 0;
            LivingEntity le = null;
            if (event.getDamager() instanceof Blaze) {
                le = (LivingEntity) event.getDamager();
                level = getMobLevel(ChatColor.stripColor((le.getCustomName())));
            } else {
                le = (LivingEntity) ((Fireball) (event.getDamager())).getShooter();
                level = getMobLevel(ChatColor.stripColor(le.getCustomName()));
            }
            double damageMultiplier = mobConfig.getConfig().getDouble(event.getEntity().getWorld().getName() + ".DamageMultiplier", 0.1);
            double newDamage = (event.getDamage() + (event.getDamage() * (level * damageMultiplier)));

            int wave = 0;

            //If MobArena is on the server & is enabled
            if (cqm.getMobArena() != null && mobConfig.getConfig().getBoolean("MobArenaWaveLeveling", false) && ((com.garbagemule.MobArena.MobArena) cqm.getMobArena()).getArenaMaster().getArenaAtLocation(event.getDamager().getLocation()) != null) {
                wave = ((com.garbagemule.MobArena.MobArena) cqm.getMobArena()).getArenaMaster().getArenaAtLocation(event.getDamager().getLocation()).getWaveManager().getWaveNumber();
            }

            newDamage += wave;
            event.setDamage(newDamage);
            ConquestiaMobs.debug("Damage Debug: " + ChatColor.AQUA + le.getCustomName() + ChatColor.WHITE + " dealt " + ChatColor.DARK_GREEN + newDamage + ChatColor.WHITE + " with a damageMultiplier setting of " + ChatColor.GOLD + damageMultiplier);
        }
    }
}
