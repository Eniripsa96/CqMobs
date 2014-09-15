package com.conquestia.mobs;

import com.gmail.filoghost.holograms.api.Hologram;
import com.gmail.filoghost.holograms.api.HolographicDisplaysAPI;
import java.text.DecimalFormat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

/**
 * Provides a set of tools for creating holograms.
 * 
 */
public class HoloUtils {

    Plugin plugin;

    public HoloUtils(Plugin plugin) {
        this.plugin = plugin;
    }

    public void sendDeathHologram(Location deathLoc, double exp, double money, long time) {
        DecimalFormat df = new DecimalFormat("#.##");
        final Hologram createHologram = HolographicDisplaysAPI.createHologram(plugin, deathLoc, ChatColor.WHITE + "+" + df.format(exp) + ChatColor.BLUE + " exp", ChatColor.GREEN + "+" + df.format(money) + ChatColor.WHITE + " Edens");

        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            public void run() {
                createHologram.delete();
            }
        }, time * 20);

    }

}
