package com.conquestia.mobs;

import com.gmail.filoghost.holograms.api.Hologram;
import com.gmail.filoghost.holograms.api.HolographicDisplaysAPI;
import java.text.DecimalFormat;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
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

    /**
     * Creates a hologram that is displayed for everyone within
     * the default show distance from the hologram.
     * 
     * @param deathLoc where the hologram will be (typically use location of eyes or hologram shows up in ground).
     * @param exp the exp amount to show.
     * @param money the money amount to show (if this is 0 will omit this line).
     * @param time how long to leave the hologram up(in seconds).
     */
    public void sendDeathHologram(Location deathLoc, double exp, double money, long time) {
        DecimalFormat df = new DecimalFormat("#.##");
        final Hologram createHologram;
        if (money != 0) {
            createHologram = HolographicDisplaysAPI.createHologram(plugin, deathLoc, ChatColor.WHITE + "+" + df.format(exp) + ChatColor.BLUE + " exp", ChatColor.GREEN + "+" + df.format(money) + ChatColor.WHITE + " Edens");
        } else {
            createHologram = HolographicDisplaysAPI.createHologram(plugin, deathLoc, ChatColor.WHITE + "+" + df.format(exp) + ChatColor.BLUE + " exp");
            
        }
        
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            public void run() {
                createHologram.delete();
            }
        }, time * 20);

    }
    
    
    /**
     * Creates a hologram that is displayed only to a
     * specific player.
     * 
     * @param deathLoc Where to display the hologram.
     * @param eyes Which player should see this hologram.
     * @param exp how much xp the player is receiving.
     * @param money how much money the player is receiving (if 0 omits line).
     * @param time how long to show the hologram.
     */
    public void sendSoloHologram(Location deathLoc, Player eyes, double exp, double money, long time) {
        DecimalFormat df = new DecimalFormat("#.##");
        final Hologram createHologram;
        if (money != 0) {
            createHologram = HolographicDisplaysAPI.createIndividualHologram(plugin, deathLoc, eyes, ChatColor.WHITE + "+" + df.format(exp) + ChatColor.BLUE + " exp", ChatColor.GREEN + "+" + df.format(money) + ChatColor.WHITE + " Edens");
        } else {
            createHologram = HolographicDisplaysAPI.createIndividualHologram(plugin, deathLoc, eyes, ChatColor.WHITE + "+" + df.format(exp) + ChatColor.BLUE + " exp");
            
        }
        
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            public void run() {
                createHologram.delete();
            }
        }, time * 20);    
    }
    
    
    /**
     * Creates a hologram that is displayed to an
     * entire hero party.
     * 
     * @param deathLoc Where to display the hologram.
     * @param party the list of which players should see the party.
     * @param exp how much xp the player is receiving.
     * @param money how much money the player is receiving (if 0 omits line).
     * @param time how long to show the hologram.
     */
    public void sendPartyHologram(Location deathLoc, List<Player> party, double exp, double money, long time) {
        DecimalFormat df = new DecimalFormat("#.##");
        final Hologram createHologram;
        if (money != 0) {
            createHologram = HolographicDisplaysAPI.createIndividualHologram(plugin, deathLoc, party , ChatColor.WHITE + "+" + df.format(exp) + ChatColor.BLUE + " exp", ChatColor.GREEN + "+" + df.format(money) + ChatColor.WHITE + " Edens");
        } else {
            createHologram = HolographicDisplaysAPI.createIndividualHologram(plugin, deathLoc, party, ChatColor.WHITE + "+" + df.format(exp) + ChatColor.BLUE + " exp");
            
        }
        
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            public void run() {
                createHologram.delete();
            }
        }, time * 20);    
    }
    
    

}
