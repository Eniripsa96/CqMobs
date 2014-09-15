/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.conquestia.mobs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author ferrago
 */
public class MobExperienceHandler implements Listener {
    private double xpScale;
    public MobExperienceHandler(Plugin plugin, double experienceScale)
    {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin); 
        this.xpScale = experienceScale;
    }
    
    @EventHandler
    public void onLeveledMobDeath(EntityDeathEvent event) {
        if (event.getEntity().getCustomName() == null) {
            return;
        }
        String entityName = ChatColor.stripColor(event.getEntity().getCustomName());
        if (event.getEntity() instanceof Monster && entityName != null && entityName.toLowerCase().contains("lvl:")) {
            
            int level = Integer.parseInt(entityName.substring(entityName.indexOf(":")+2, entityName.indexOf("]")));
            event.setDroppedExp((int)(event.getDroppedExp() + (event.getDroppedExp() * level * xpScale)));
            //This is just a basic test.
        }
    }
    
    
}
