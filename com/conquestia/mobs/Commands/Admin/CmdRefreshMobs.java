/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.conquestia.mobs.Commands.Admin;

import com.conquestia.mobs.Commands.CqmCommand;
import com.conquestia.mobs.Commands.CqmCommandHandler;
import com.conquestia.mobs.ConquestiaMobs;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author ferrago
 */
public class CmdRefreshMobs implements CqmCommand{

    @Override
    public void execute(CqmCommandHandler handler, Plugin plugin, CommandSender sender, String[] args) {
        if (args.length == 2 && args[1].contains("?"))
        {
            sender.sendMessage(getHelp());
            return;
        }
        ((ConquestiaMobs)plugin).RefreshMobs();
        sender.sendMessage(ChatColor.GOLD + "[" + ChatColor.BLUE + "Conquestia" + ChatColor.YELLOW + "Mobs" + ChatColor.GOLD + "]" + ChatColor.GREEN + " Mobs Succesfully refreshed!");
    }

    @Override
    public String getPermissionNode() {
        return "conquestiamobs.admin";
    }

    @Override
    public String getArgsString() {
        return "none";
    }

    @Override
    public String getDescription() {
        return "Removes all mobs currently on the server.";
    }

    @Override
    public String getHelp() {
        return ChatColor.GOLD + "[" + ChatColor.BLUE + "Conquestia" + ChatColor.YELLOW + "Mobs" + ChatColor.GOLD + "]" + ChatColor.LIGHT_PURPLE + " refreshmobs" + ChatColor.WHITE + " - requires no arguments and refreshes mobs on the server. ";
    }
    
}
