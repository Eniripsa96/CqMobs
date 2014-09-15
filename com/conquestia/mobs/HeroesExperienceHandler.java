package com.conquestia.mobs;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.events.ExperienceChangeEvent;
import com.herocraftonline.heroes.api.events.HeroKillCharacterEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass.ExperienceType;
import java.util.HashMap;
import java.util.Random;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import static org.bukkit.Bukkit.getServer;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 *
 * @author ferrago
 */
public class HeroesExperienceHandler implements Listener {

    private final double xpScale;
    Plugin plugin;
    private final HashMap<Player, LivingEntity> mobKillMap = new HashMap<>();
    private final HashMap<EntityType, Double> typeCost = new HashMap<>();
    double levelCost = 0.1;
    public static HashMap<Player, LivingEntity> mobArenaKillMap = new HashMap();
    private final boolean maEnabled;
    private final double maScale;
    private Economy econ;
    private final Heroes heroes;

    public HeroesExperienceHandler(Plugin plugin, double experienceScale, boolean maEnabled, double maScale) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.xpScale = experienceScale;
        this.plugin = plugin;
        this.maEnabled = maEnabled;
        this.maScale = maScale;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            econ = rsp.getProvider();
        }
        buildMoneyDrops();
        heroes = (Heroes) Bukkit.getPluginManager().getPlugin("Heroes");
    }

    @EventHandler
    public void onHeroMobDeath(HeroKillCharacterEvent event) {
        if (!(event.getDefender().getEntity() instanceof Player) && !event.getDefender().getEntity().hasMetadata("Spawner")) {
            boolean showDeath = true;
            String entityName = ChatColor.stripColor(event.getDefender().getEntity().getCustomName());
            
            if (event.getDefender().getEntity() instanceof Monster && entityName != null && entityName.toLowerCase().contains("lvl:")) {
                mobKillMap.put(event.getAttacker().getPlayer(), event.getDefender().getEntity());
                double maxMoneyDrop = 0;
                int level = Integer.parseInt(entityName.substring(entityName.indexOf(":") + 2, entityName.indexOf("]")));
                Random rand = new Random();
                maxMoneyDrop = (level * levelCost * typeCost.get(event.getDefender().getEntity().getType())) + typeCost.get(event.getDefender().getEntity().getType());
                double moneyDrop = maxMoneyDrop * rand.nextDouble();

                if (event.getAttacker().hasParty()) {
                    for (Hero hero : event.getAttacker().getParty().getMembers()) {
                        if (event.getAttacker().getPlayer().getLocation().distanceSquared(hero.getPlayer().getLocation()) < 900) {
                            mobKillMap.put(hero.getPlayer(), event.getDefender().getEntity());
                            if (econ != null) {
                                
                                moneyDrop = (moneyDrop / event.getAttacker().getParty().getMembers().size());
                                econ.depositPlayer(hero.getPlayer().getName(), moneyDrop);
                            }
                        }
                    }
                } else {
                    econ.depositPlayer(event.getAttacker().getPlayer().getName(), moneyDrop);
                }

                if (ConquestiaMobs.getMobArena() != null && ConquestiaMobs.getMobArena().getArenaMaster().getArenaAtLocation(event.getDefender().getEntity().getLocation()) != null) {
                    mobArenaKillMap.put(event.getAttacker().getPlayer(), event.getDefender().getEntity());
                    Arena arena = ConquestiaMobs.getMobArena().getArenaMaster().getArenaWithPlayer(event.getAttacker().getPlayer());
                    moneyDrop = 0;
                    showDeath = false;
                    for (Hero hero : event.getAttacker().getParty().getMembers()) {

                        if (ConquestiaMobs.getMobArena().getArenaMaster().getArenaWithPlayer(hero.getPlayer()) != null && ConquestiaMobs.getMobArena().getArenaMaster().getArenaWithPlayer(hero.getPlayer()) == arena) {
                            mobArenaKillMap.put(hero.getPlayer(), event.getDefender().getEntity());
                        }

                    }
                }
                
                if (showDeath) {
                    double xp =  heroes.getCharacterManager().getMonster(event.getDefender().getEntity()).getExperience() * level * xpScale;
                    xp = xp * -3;
                    
                    if (event.getAttacker().hasParty()) {
                        xp = (xp * 2) / event.getAttacker().getParty().getMembers().size();
                    }
                    
                    ConquestiaUtilities.getHoloUtil().sendDeathHologram(event.getDefender().getEntity().getEyeLocation(), xp, moneyDrop, 5);
                }

            }
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHeroExpChange(ExperienceChangeEvent event) {
        if (event.getSource() == ExperienceType.KILLING && mobKillMap.containsKey(event.getHero().getPlayer())) {
            LivingEntity ent = mobKillMap.get(event.getHero().getPlayer());
            String entityName = ChatColor.stripColor(ent.getCustomName());
            int level = Integer.parseInt(entityName.substring(entityName.indexOf(":") + 2, entityName.indexOf("]")));
            if (mobArenaKillMap.containsKey(event.getHero().getPlayer())) {
                event.setExpGain(event.getExpChange() * level * xpScale * maScale);
            } else {
                event.setExpGain(event.getExpChange() * level * xpScale);
            }
            mobKillMap.remove(event.getHero().getPlayer());
        }
    }

}
