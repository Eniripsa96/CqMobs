package com.conquestia.mobs;

import org.bukkit.Bukkit;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

/**
 * Handles the experience orb drop when a monster dies. While this class is
 * always used, depending on the users settings, one of the other experience
 * handlers might be used.
 *
 * @author Ferrago
 */
public class MobExperienceHandler implements Listener {

    private double xpScale; //Used to modify experience by some factor

    /**
     * Register this class as a listener
     *
     * @param plugin the calling plugin.
     * @param experienceScale the scale to modify experience by.
     */
    public MobExperienceHandler(Plugin plugin, double experienceScale, boolean moneyDrop, boolean hologram) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.xpScale = experienceScale;
    }

    /**
     * Handles the mob death event. This experience handler is for the users
     * that use default experience and levels.
     *
     * @param event The triggering event.
     */
    @EventHandler
    public void onLeveledMobDeath(EntityDeathEvent event) {
        if (event.getEntity().getCustomName() == null) {
            return;
        }
        if (event.getEntity() instanceof Monster) {

            int level = MobSpawnHandler.getMobLevel(event.getEntity());
            event.setDroppedExp((int) (event.getDroppedExp() + (event.getDroppedExp() * level * xpScale)));
        }
    }

}
