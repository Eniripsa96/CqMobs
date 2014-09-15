/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.conquestia.mobs.Commands;

import com.conquestia.mobs.Commands.Admin.CmdRefreshMobs;
import com.conquestia.mobs.Commands.Admin.CmdReload;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author ferrago
 */
public class CqmCommandHandler implements CommandExecutor {
    
    HashMap<String, CqmCommand> commandList = new HashMap();
    HashMap<String, String> usageList = new HashMap();
    ArrayList<Integer> pageNumbers = new ArrayList();
    ArrayList<String> cmdHelpBook = new ArrayList();
    JavaPlugin callingPlugin;
    
    public CqmCommandHandler(JavaPlugin plugin)
    {
        callingPlugin = plugin;
        plugin.getCommand("cqm").setExecutor(this);
        registerCommands();
        cmdHelpBook = buildHelpBook();
        plugin.getLogger().info("Listening for ConquestiaMobs Comamnds");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && !((Player)sender).hasPermission("conquestiamobs.admin"))
        {
            return true;
        }
        if (args.length == 0)
        {
            sender.sendMessage(cmdHelpBook.get(0));    
        }
        else if (pageNumbers.contains(args[0])) //If they are looking for a help page
        {
            sender.sendMessage(cmdHelpBook.get(Integer.parseInt(args[0]) - 1));
        }
        else if (commandList.containsKey(args[0])) //If it is a valid subcommand
        {
            commandList.get(args[0]).execute(this, callingPlugin, sender, args);
        }
        else //If it is not a valid subcommand nor a correct help page
        {
            sender.sendMessage(ChatColor.GOLD + "[" + ChatColor.BLUE + "Conquestia" + ChatColor.YELLOW + "Mobs" + ChatColor.GOLD + "]"+  ChatColor.DARK_RED + " Sub-command not found!");
        }
        
        
        return true;
    }
    
    public void registerCommands()
    {
        //Reload Command
        CmdReload reload = new CmdReload();
        commandList.put("reload", reload);
        usageList.put("reload", reload.getDescription());
        //RefreshMobs Command
        CmdRefreshMobs refreshmobs = new CmdRefreshMobs();
        commandList.put("refreshmobs", refreshmobs);
        usageList.put("refreshmobs", refreshmobs.getDescription());
        //Next Command
    }
    
    
    public ArrayList<String> buildHelpBook()
    {
        ArrayList<String> helpBook = new ArrayList();
        ArrayList<String> tempHelpBook = new ArrayList();
        
        for (String cmd : usageList.keySet())
        {
            tempHelpBook.add("  " + ChatColor.BLUE + "/cqm " + ChatColor.YELLOW + cmd + ChatColor.WHITE + " - " + usageList.get(cmd) + '\n');    
        }
        
        int cmdsAdded = 0;
        int pagesAdded = 1;
        int totalPages = (int)Math.ceil(tempHelpBook.size()/8);
        
        if (totalPages == 0)
        {
            totalPages = 1;
        }
        
        String pageParser = "";
        String helpPageHeader = ChatColor.GOLD + "------[" + ChatColor.BLUE + "Conquestia" + ChatColor.YELLOW + "Mobs" + ChatColor.WHITE + " Help <" + pagesAdded + "/" + totalPages + ">" + ChatColor.GOLD + "]------\n";
        String helpPageFooter = ChatColor.DARK_RED + "For extra help with a command type: " + ChatColor.WHITE + "/cqm <sub-command> ?";
        
        while (!tempHelpBook.isEmpty())
        {
            if (cmdsAdded % 8 == 0 && cmdsAdded != 0)
            {
                helpBook.add(helpPageHeader + pageParser + helpPageFooter);
                pageParser = "";
            }
            pageParser += tempHelpBook.get(0);
            tempHelpBook.remove(0); 
        }
        
        if (!helpBook.contains(helpPageHeader + pageParser + helpPageFooter)) {
            helpBook.add(helpPageHeader + pageParser + helpPageFooter);
        }
        
        return helpBook;
    }
    
    
    
    
    
}
