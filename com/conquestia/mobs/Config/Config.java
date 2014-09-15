package com.conquestia.mobs.Config;

//Credits to Config class go to Steven Sucy

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config
{
  private final String fileName;
  private final JavaPlugin plugin;
  private File configFile;
  private FileConfiguration fileConfiguration;

  public Config(JavaPlugin plugin, String name)
  {
    this.plugin = plugin;
    this.fileName = (name + ".yml");

    this.configFile = new File(plugin.getDataFolder(), this.fileName);
    try {
      String path = this.configFile.getAbsolutePath();
      if (new File(path.substring(0, path.lastIndexOf(File.separator))).mkdirs())
        plugin.getLogger().info("Created a new folder for config files");
    }
    catch (Exception e)
    {
    }
  }

  public File getConfigFile()
  {
    return this.configFile;
  }

  public void reloadConfig()
  {
    this.fileConfiguration = YamlConfiguration.loadConfiguration(this.configFile);
    InputStream defConfigStream = this.plugin.getResource(this.fileName);
    if (defConfigStream != null) {
      YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
      this.fileConfiguration.setDefaults(defConfig);
    }
  }

  public FileConfiguration getConfig()
  {
    if (this.fileConfiguration == null) {
      reloadConfig();
    }
    return this.fileConfiguration;
  }

  public void saveConfig()
  {
    if ((this.fileConfiguration != null) || (this.configFile != null))
      try {
        getConfig().save(this.configFile);
      } catch (IOException ex) {
        this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.configFile, ex);
      }
  }

  public void saveDefaultConfig()
  {
    if (this.configFile == null) {
      this.configFile = new File(this.plugin.getDataFolder().getAbsolutePath() + "/" + this.fileName);
    }
    if (!this.configFile.exists())
      this.plugin.saveResource(this.fileName, false);
  }
}