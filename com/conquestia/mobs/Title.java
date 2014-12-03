package com.conquestia.mobs;

import java.text.DecimalFormat;
import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.EnumTitleAction;
import net.minecraft.server.v1_8_R1.IChatBaseComponent;
import net.minecraft.server.v1_8_R1.PacketPlayOutTitle;
import org.bukkit.Bukkit;

import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * Minecraft 1.8 Title
 * 
* @version 1.0.4
 * @author Maxim Van de Wynckel
 */
public class Title {
    /* Title text and color */
    private String title = "";
    private String titleJSON = "";
    /* Subtitle text and color */
    private String subtitle = "";
    private String subtitleJSON = "";
    /* Title timings */
    private int fadeInTime = -1;
    private int stayTime = -1;
    private int fadeOutTime = -1;
    private double xp = -1;
    private double money = -1;
    private boolean ticks = false;
    private DecimalFormat df = new DecimalFormat("#.##");

    /**
     * Create a new 1.8 title
     *
     * @param title Title
     */
    public Title(String title) {
        this.title = title;
    }

    /**
     * Create a new 1.8 title
     *
     * @param title Title text
     * @param subtitle Subtitle text
     */
    public Title(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    /**
     * Copy 1.8 title
     *
     * @param title Title
     */
    public Title(Title title) {
        // Copy title
        this.title = title.title;
        this.subtitle = title.subtitle;
        this.fadeInTime = title.fadeInTime;
        this.fadeOutTime = title.fadeOutTime;
        this.stayTime = title.stayTime;
        this.ticks = title.ticks;
    }

    /**
     * Create a new 1.8 title
     *
     * @param title Title text
     * @param subtitle Subtitle text
     * @param fadeInTime Fade in time
     * @param stayTime Stay on screen time
     * @param fadeOutTime Fade out time
     */
    public Title(String title, String subtitle, int fadeInTime, int stayTime, int fadeOutTime) {
        this.title = title;
        this.subtitle = subtitle;
        this.fadeInTime = fadeInTime;
        this.stayTime = stayTime;
        this.fadeOutTime = fadeOutTime;
    }

    /**
     * Set title text
     *
     * @param title Title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get title text
     *
     * @return Title text
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Set subtitle text
     *
     * @param subtitle Subtitle text
     */
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    /**
     * Get subtitle text
     *
     * @return Subtitle text
     */
    public String getSubtitle() {
        return this.subtitle;
    }


    /**
     * Set title fade in time
     *
     * @param time Time
     */
    public void setFadeInTime(int time) {
        this.fadeInTime = time;
    }

    /**
     * Set title fade out time
     *
     * @param time Time
     */
    public void setFadeOutTime(int time) {
        this.fadeOutTime = time;
    }

    /**
     * Set title stay time
     *
     * @param time Time
     */
    public void setStayTime(int time) {
        this.stayTime = time;
    }
    
    public void setMoney(double money) {
       this.money = money; 
    }
    
    public void setXp(double xp) {
        this.xp = xp;
    }
    
    public void sendDisplay(Player player) {
        //resetTitle(player);
        
        titleJSON = "{\"text\":\"\",\"extra\":[{\"text\":\"+#\",\"color\":\"white\"},{\"text\":\" Exp\",\"color\":\"dark_blue\"}]}";
        titleJSON = titleJSON.replace("#", df.format(xp));
        subtitleJSON = "{\"text\":\"\",\"extra\":[{\"text\":\"+#\",\"color\":\"white\"},{\"text\":\" Edens\",\"color\":\"green\"}]}";
        subtitleJSON = subtitleJSON.replace("#", df.format(money));
        
        String singleTitle1JSON = "{\"text\":\"\",\"extra\":[{\"text\":\"+# \"},{\"text\":\"Exp\\\n\",\"color\":\"dark_blue\"";
        String singleTitle2JSON = "\"},{\"text\":\"+$ \"},{\"text\":\"Edens\",\"color\":\"green\"}";
        String endJSON = "]}";
        String finalJSON = "{\"text\":\"\",\"extra\":[{\"text\":\" +# \"},{\"text\":\"Exp\",\"color\":\"blue\"},{\"text\":\" / \"},{\"text\":\"+$ \"},{\"text\":\"Edens\",\"color\":\"green\"}]}";
        
        if (xp == -1) {
            title = "";
            //finalJSON += singleTitle1JSON;
        }
        
        if (money == -1) {
            subtitle = "";
            //finalJSON += singleTitle2JSON;
        }
        
        //finalJSON += endJSON;
        
        finalJSON = finalJSON.replace("#", df.format(xp)).replace("$", df.format(money));
            
            IChatBaseComponent msg1 = ChatSerializer.a("");
            IChatBaseComponent msg = ChatSerializer.a(finalJSON);
            PacketPlayOutTitle packet1 = new PacketPlayOutTitle(EnumTitleAction.TITLE, msg1, 10, 2*20, 10);
            PacketPlayOutTitle packet = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, msg, 10, 2*20, 10);
            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet1); 
            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }
}
