package io.riat.CTF;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.HashMap;

public class Utils {

    public static HashMap<String, Material> getTeamMaterialMap() {
        HashMap<String, Material> teams = new HashMap<>();

        // 14 teams
        teams.put("WHITE", Material.WHITE_BANNER);
        teams.put("ORANGE", Material.ORANGE_BANNER);
        teams.put("MAGENTA", Material.MAGENTA_BANNER);
        teams.put("YELLOW", Material.YELLOW_BANNER);
        teams.put("LIME", Material.LIME_BANNER);
        teams.put("PINK", Material.PINK_BANNER);
        teams.put("GRAY", Material.GRAY_BANNER);
        teams.put("CYAN", Material.CYAN_BANNER);
        teams.put("PURPLE", Material.PURPLE_BANNER);
        teams.put("BLUE", Material.BLUE_BANNER);
        teams.put("BROWN", Material.BROWN_BANNER);
        teams.put("GREEN", Material.GREEN_BANNER);
        teams.put("RED", Material.RED_BANNER);
        teams.put("BLACK", Material.BLACK_BANNER);

        return teams;
    }
    
    public static HashMap<String, ChatColor> getTeamColorMap() {
        HashMap<String, ChatColor> colors = new HashMap<>();

        colors.put("WHITE", ChatColor.WHITE);
        colors.put("ORANGE", ChatColor.RED);
        colors.put("MAGENTA", ChatColor.LIGHT_PURPLE);
        colors.put("YELLOW", ChatColor.YELLOW);
        colors.put("LIME", ChatColor.GREEN);
        colors.put("PINK", ChatColor.DARK_AQUA);
        colors.put("GRAY", ChatColor.GRAY);
        colors.put("CYAN", ChatColor.AQUA);
        colors.put("PURPLE", ChatColor.DARK_PURPLE);
        colors.put("BLUE", ChatColor.BLUE);
        colors.put("BROWN", ChatColor.DARK_BLUE);
        colors.put("GREEN", ChatColor.DARK_GREEN);
        colors.put("RED", ChatColor.DARK_RED);
        colors.put("BLACK", ChatColor.BLACK);

        colors.put("NO TEAM", ChatColor.BOLD);

        return colors;
    }


}
