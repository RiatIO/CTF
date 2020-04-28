package io.riat.CTF.Events;

import io.riat.CTF.DatabaseManager;
import io.riat.CTF.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class BlockPlace implements Listener {
    private HashMap<String, Material> banners = Utils.getTeamMaterialMap();

    private Plugin plugin;
    private final DatabaseManager databaseManager;

    public BlockPlace(Plugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    @EventHandler
    public void onBlockPlaceEvent(final BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Block b = e.getBlock();

        // Check if the placed block is a banner
        if (banners.containsValue(b.getType())) {

            // Player's cannot place flags which are not their team flag
            String team = getPlayerTeam(player);

            if (team == null || b.getType() != banners.get(team)) {
                player.sendMessage("[CTF] You have to be in a team, or have the right team banner to place it out");
                e.setCancelled(true);
                return;
            }

            // Check the the surrounding blocks is air (like 5x5)
            Location location = b.getLocation();

            for (int y = 1; y < 5; y++) {
                for (int x = 1; x < 5; x++) {
                    for (int z = 1; z < 5; z++) {
                        location.add(x, y, z);
                        // Check if air
                        if (location.getBlock().getType() != Material.AIR) {
                            e.setCancelled(true);

                            player.sendMessage("[CTF] The flag should be placed with at least 5 blocks of air");

                            return;
                        }
                        location.subtract(x, y, z);

                        location.add(x*-1, y, z*-1);
                        // Check if air
                        if (location.getBlock().getType() != Material.AIR) {
                            e.setCancelled(true);

                            player.sendMessage("[CTF] The flag should be placed with at least 5 blocks of air");

                            return;
                        }
                        location.subtract(x*-1, y, z*-1);
                    }
                }
            }

            databaseManager.flagPlaced(player);
        }

        if (b.getType() == Material.WHITE_STAINED_GLASS_PANE) {
            e.setCancelled(true);
            player.sendMessage("[CTF] Trying to be smart, ey? That's not allowed.");
        }
    }


    public String getPlayerTeam(Player player) {
        return databaseManager.queryPlayerTeamColor(player);
    }
}
