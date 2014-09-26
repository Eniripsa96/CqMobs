package com.conquestia.mobs;

import com.sucy.skill.api.event.PlayerExperienceGainEvent;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author ferrago
 */
public class SkillAPIExperienceHandler implements Listener {
    HashMap<String, LivingEntity> mobKillMap = new HashMap();
    
    private final double xpScale;
    private final Plugin plugin;
    
    public SkillAPIExperienceHandler(Plugin plugin, double experienceScale) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin); 
        this.xpScale = experienceScale;
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onExperienceGainEvent(PlayerExperienceGainEvent event) {
        if (event.isCommandExp()) {
            return;
        }
        
        if (mobKillMap.containsKey(event.getPlayerData().getPlayer().getUniqueId().toString())) {
            LivingEntity ent = mobKillMap.get(event.getPlayerData().getPlayer().getUniqueId().toString());
            String entityName = ChatColor.stripColor(ent.getCustomName());
            int level = Integer.parseInt(entityName.substring(entityName.indexOf(":")+2, entityName.indexOf("]")));
            event.setExp((int)(event.getExp() + (level * xpScale)));
            mobKillMap.remove(event.getPlayerData().getPlayer().getUniqueId().toString());
        }
        
    }
    
    @EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=false)
    public void onEntityDeath(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity && ((LivingEntity)event.getEntity()).getHealth() - event.getDamage() <= 0 && ((LivingEntity)event.getEntity()).getCustomName() != null && ((LivingEntity)event.getEntity()).getCustomName().contains("Lvl")) {
            mobKillMap.put(((Player)event.getDamager()).getUniqueId().toString(), (LivingEntity)event.getEntity());
        }
    }
    
}
