package io.riat.CTF;

import org.bukkit.Material;

import java.util.HashMap;

public class Utils {

    public static HashMap<String, Material> getTeamMap() {
        HashMap<String, Material> teams = new HashMap<>();

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

}
