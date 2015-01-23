/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.conquestia.mobs;

import net.milkbowl.vault.economy.Economy;
import static org.bukkit.Bukkit.getServer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 *
 * @author Ferrago
 */
public class MoneyUtil {

    private Economy econ = null;
    private static MoneyUtil instance = null;

    private MoneyUtil() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            econ = rsp.getProvider();
        } else {
            ConquestiaMobs.debug("Error grabbing economy!");
        }
    }
    
    public static MoneyUtil getInstance() {
        if (instance == null) {
            instance = new MoneyUtil();
        }
        
        return instance;
    }

    public void dropMoney(Player player, double amount) {
        econ.depositPlayer(player, amount);
    }

}
