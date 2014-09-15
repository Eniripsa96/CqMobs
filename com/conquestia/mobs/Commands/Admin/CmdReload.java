package com.conquestia.mobs.Commands.Admin;

import com.conquestia.mobs.Commands.CqmCommand;
import com.conquestia.mobs.Commands.CqmCommandHandler;
import com.conquestia.mobs.ConquestiaMobs;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Command to reload the plugin data
 */
public class CmdReload implements CqmCommand {

    /**
     * Executes the command
     *
     * @param handler handler for the command
     * @param plugin  plugin reference
     * @param sender  sender of the command
     * @param args    arguments
     */
    @Override
    public void execute(CqmCommandHandler handler, Plugin plugin, CommandSender sender, String[] args)
    {
        if (args.length == 2 && args[1].contains("?"))
        {
            sender.sendMessage(getHelp());
            return;
        }
        plugin.getServer().getPluginManager().disablePlugin(plugin);
        plugin.getServer().getPluginManager().enablePlugin(plugin);
        ((ConquestiaMobs)plugin).RefreshMobs();
        sender.sendMessage(ChatColor.GOLD + "[" + ChatColor.BLUE + "Conquestia" + ChatColor.YELLOW + "Mobs" + ChatColor.GOLD + "]" + ChatColor.GREEN + " Succesfully reloaded mob config!");
    }

    /**
     * @return permission required for this command
     */
    @Override
    public String getPermissionNode() {
        return "conquestiamobs.admin";
    }

    /**
     * @return the description of this command
     */
    @Override
    public String getDescription() {
        return "Reloads ConquestiaMobs";
    }

    @Override
    public String getArgsString() {
        return "none";
    }
    
    @Override
    public String getHelp()
    {
        return ChatColor.GOLD + "[" + ChatColor.BLUE + "Conquestia" + ChatColor.YELLOW + "Mobs" + ChatColor.GOLD + "]" + ChatColor.LIGHT_PURPLE + " reload" + ChatColor.WHITE + " - requires no arguments and reloads the server. ";
    }
}
