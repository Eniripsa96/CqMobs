package com.conquestia.mobs;

import com.sucy.skill.api.event.PlayerExperienceGainEvent;
import java.util.HashMap;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
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
    HashMap<EntityType, Double> typeCost = new HashMap();
    private final double xpScale;
    private final boolean maEnabled;
    private final double maScale;
    private final boolean moneyDrops;
    private final boolean debug;
    private boolean showDeath = true;
    private final Plugin plugin;
    
    public SkillAPIExperienceHandler(Plugin plugin, double experienceScale, boolean maEnabled, double maScale, boolean moneyDrops, boolean debug) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin); 
        this.xpScale = experienceScale;
        this.plugin = plugin;
        this.maEnabled = maEnabled;
        this.maScale = maScale;
        this.debug = debug;
        this.moneyDrops = moneyDrops;
        if (moneyDrops) {
            buildMoneyDrops();
        }
    }
    
    @EventHandler
    public void onExperienceGainEvent(PlayerExperienceGainEvent event) {
        if (!ConquestiaMobs.getEnabledWorlds().contains(event.getPlayerData().getPlayer().getWorld())) {
            return;
        }
        if (event.isCommandExp()) {
            return;
        }
        int xp = 0;
        if (mobKillMap.containsKey(event.getPlayerData().getPlayer().getUniqueId().toString())) {
            LivingEntity ent = mobKillMap.get(event.getPlayerData().getPlayer().getUniqueId().toString());
            int level = MobSpawnHandler.getMobLevel(ent);
            xp = (int)(event.getExp() + (level * xpScale));
            event.setExp(xp);
            
            if (maEnabled && ConquestiaMobs.getMobArena() != null) {
                if (((com.garbagemule.MobArena.MobArena)ConquestiaMobs.getMobArena()).getArenaMaster().getArenaWithMonster(ent) != null) {
                    int wave = ((com.garbagemule.MobArena.MobArena)ConquestiaMobs.getMobArena()).getArenaMaster().getArenaWithMonster(ent).getWaveManager().getWaveNumber();
                    xp = (int)(event.getExp() + (level * xpScale * maScale * event.getExp()));
                    event.setExp(xp);
                    showDeath = false;
                }
            }
            
            if (showDeath) {
                Random rand = new Random();
                Location loc = ent.getLocation().clone();
                loc.setY(loc.getY() + 2);
                double maxMoney = 0;
                double actualMoney = 0;
                if (typeCost.containsKey(mobKillMap.get(event.getPlayerData().getPlayer().getUniqueId().toString()))) {
                    double entityDrop = typeCost.get(mobKillMap.get(event.getPlayerData().getPlayer().getUniqueId().toString()));
                    maxMoney = entityDrop + entityDrop * level * 0.1;
                    actualMoney = maxMoney * rand.nextDouble();
                }
                
                ConquestiaMobs.getDisplay().DisplayStuff(loc, xp, actualMoney, event.getPlayerData().getPlayer(), null);
            } else {
                showDeath = true;
            }
            
            mobKillMap.remove(event.getPlayerData().getPlayer().getUniqueId().toString());
            MobSpawnHandler.getLevelMap().remove(ent.getUniqueId().toString());
        }
        
    }
    
    @EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=false)
    public void onEntityDeath(EntityDamageByEntityEvent event) {
        if (!ConquestiaMobs.getEnabledWorlds().contains(event.getEntity().getWorld())) {
            return;
        }
        if (event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity && ((LivingEntity)event.getEntity()).getHealth() - event.getDamage() <= 0 && ((LivingEntity)event.getEntity()).getCustomName() != null && ((LivingEntity)event.getEntity()).getCustomName().contains("Lvl")) {
            mobKillMap.put(((Player)event.getDamager()).getUniqueId().toString(), (LivingEntity)event.getEntity());
        }
    }
    
    public void buildMoneyDrops() {
        typeCost.put(EntityType.CAVE_SPIDER, 0.1);
        typeCost.put(EntityType.BLAZE, 0.1);
        typeCost.put(EntityType.CREEPER, 0.1);
        typeCost.put(EntityType.GIANT, 0.1);
        typeCost.put(EntityType.IRON_GOLEM, 0.1);
        typeCost.put(EntityType.MAGMA_CUBE, 0.1);
        typeCost.put(EntityType.ZOMBIE, 0.1);
        typeCost.put(EntityType.WITCH, 0.1);
        typeCost.put(EntityType.SPIDER, 0.1);
        typeCost.put(EntityType.PIG_ZOMBIE, 0.1);
        typeCost.put(EntityType.SKELETON, 0.1);
        typeCost.put(EntityType.SLIME, 0.05);
            
    }
    
}
