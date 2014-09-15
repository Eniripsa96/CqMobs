package com.conquestia.mobs.Commands;

import com.conquestia.mobs.Commands.CqmCommandHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * Interface for handlers for commands
 */
public interface CqmCommand {

    /**
     * Executes a command
     *
     * @param handler command handler
     * @param plugin  plugin reference
     * @param sender  sender of the command
     * @param args    command arguments
     */
    public void execute(CqmCommandHandler handler, Plugin plugin, CommandSender sender, String[] args);

    /**
     * @return permission needed for this command
     */
    public String getPermissionNode();

    /**
     * @return args string (e.g. <playerName> <message>)
     */
    public String getArgsString();

    /**
     * @return command description (be very brief)
     */
    public String getDescription();
    
    public String getHelp();

}
