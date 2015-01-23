package com.conquestia.mobs;

import java.text.DecimalFormat;
import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.entity.Player;



/**
 *
 * @author Ferrago
 */
public class DisplayUtil {
    
    private boolean spigot1_8 = false;
    private boolean moneyDrops = false;
    private boolean holographicMoney = false;
    private boolean holographicXp = false;
    private boolean titleMoney = false;
    private boolean titleXp = false;
    private DecimalFormat df = new DecimalFormat("#.##");
    
    
    public DisplayUtil(boolean spigot1_8, boolean moneyDrops, boolean hologram, boolean titleMoney, boolean titleXp) {
        this.spigot1_8 = spigot1_8;
        this.moneyDrops = moneyDrops;
        this.holographicMoney = hologram && moneyDrops;
        this.holographicXp = hologram;
        this.titleMoney = titleMoney && spigot1_8;
        this.titleXp = titleXp && spigot1_8;
    }
    
    public void DisplayStuff(Location loc, double exp, double money, Player player, ArrayList<Player> partyMembers) {
        if (holographicXp) {
            if (!moneyDrops) {
                money = 0;
            }
            
            com.gmail.filoghost.holograms.api.Hologram holo;
            
            if (partyMembers != null) {
                holo = ConquestiaMobs.getHoloUtil().sendPartyHologram(loc, partyMembers, exp, money, 5);
            } else {
                holo = ConquestiaMobs.getHoloUtil().sendSoloHologram(loc, player, exp, money, 5);
            }
        }
        
        if (titleMoney || titleXp) {
            Title title = new Title("I Don't Matter");

            if (titleXp) {
                title.setXp(exp);
            }
            if (titleMoney) {
                title.setMoney(money);
            }
            if (partyMembers != null) {
                for (Player member : partyMembers) {
                    title.sendDisplay(member);
                }
            }
            title.sendDisplay(player);
        }
        
        if (money != 0.0) {
          if (partyMembers != null) {  
              for (Player member : partyMembers) {
                  MoneyUtil.getInstance().dropMoney(member, money);
              }
              MoneyUtil.getInstance().dropMoney(player, money);
          }
        }
        
        
    }
    
    public void DisplayStuffHeroes(Location loc, double exp, double money, Player player) {
        com.herocraftonline.heroes.characters.Hero hero = com.herocraftonline.heroes.Heroes.getInstance().getCharacterManager().getHero(player);
        ArrayList<Player> players = new ArrayList<>();
        if (hero.hasParty()) {
            for (com.herocraftonline.heroes.characters.Hero member : hero.getParty().getMembers()) {
                players.add(member.getPlayer());
            }
        }
        
        DisplayStuff(loc, exp, money, player, players);
    }
    
}
