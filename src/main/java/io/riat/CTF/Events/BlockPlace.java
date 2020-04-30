package io.riat.CTF.Events;

import io.riat.CTF.DatabaseManager;
import io.riat.CTF.ScoreboardManager;
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
    private final ScoreboardManager scoreboardManager;

    public BlockPlace(Plugin plugin, DatabaseManager databaseManager, ScoreboardManager scoreboardManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler
    public void onBlockPlaceEvent(final BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Block b = e.getBlock();
        World world = plugin.getServer().getWorld("world");

        // Check if there are banners around the block, if it is, dont place it.
        if (isBlockPlacedAroundBanner(b)) {
            player.sendMessage("[CTF] You cannot place a block around the flag!");
            e.setCancelled(true);
        }

        // Check if the placed block is a banner
        if (banners.containsValue(b.getType())) {

            // Check height of the flag (max 200 Y)
            if (b.getY() > 150) {
                player.sendMessage("[CTF] Flag has to be placed a bit lower...");
                e.setCancelled(true);
                return;
            }

            // Player's cannot place flags which are not their team flag
            String team = getPlayerTeam(player);

            if (team == null) {
                player.sendMessage("[CTF] You have to be in a team, or have the right team banner to place it out");
                e.setCancelled(true);
                return;
            }


            // Check if the team flag is in the area to score points
            if (b.getType() != banners.get(team)) {
                if (!isBlockEnemyFlagPlacedInBase(world, b, banners.get(team))) {
                    player.sendMessage("[CTF] The enemy flag has to be placed next to your own flag!");
                    e.setCancelled(true);
                } else {
                    // Remove the block
                    b.setType(Material.AIR);

                    // give points
                    updateTeamScore(team, 5);
                }
                return;
            }

            // Check if team has placed a flag
            String flagLocation = databaseManager.queryFlagPlaced(team);

            if (flagLocation != null) {
                // Remove the placed flag.
                databaseManager.flagRemoved(team);

                // Remove the flag from the world
                String[] locationDeserialization = flagLocation.split(":");
                String flagWorld = locationDeserialization[0];
                double flagX = Double.parseDouble(locationDeserialization[1]);
                double flagY = Double.parseDouble(locationDeserialization[2]);
                double flagZ = Double.parseDouble(locationDeserialization[3]);

                Location flag = new Location(
                        plugin.getServer().getWorld(flagWorld),
                        flagX,
                        flagY,
                        flagZ
                );
                flag.getBlock().setType(Material.AIR);

                updateTeamScore(team, -2);
            }

            // Check the the surrounding blocks is air (like 5x5)
            Location location = b.getLocation();

            for (int y = 0; y < 5; y++) {
                for (int x = -4; x < 5; x++) {
                    for (int z = -4; z < 5; z++) {
                        Block current = world.getBlockAt(
                                location.getBlockX() + x,
                                location.getBlockY() + y,
                                location.getBlockZ() + z
                        );

                        if (current.getType() == b.getType()) continue;

                        if (current.getType() != Material.AIR && current.getType() != Material.CAVE_AIR && current.getType() != Material.VOID_AIR) {
                            player.sendMessage("[CTF] The flag should be placed with at least 5 blocks of air around");
                            e.setCancelled(true);
                            return;
                        }

                    }
                }
            }

            databaseManager.flagPlaced(player, b.getLocation());
        }


        // The dome is made out of White stained glass, can't allow players to place that material.
        if (b.getType() == Material.WHITE_STAINED_GLASS_PANE) {
            e.setCancelled(true);
            player.sendMessage("[CTF] Trying to be smart, ey? That's not allowed.");
        }
    }


    public String getPlayerTeam(Player player) {
        return databaseManager.queryPlayerTeamColor(player);
    }


    private boolean isBlockPlacedAroundBanner(Block block) {
        World world = plugin.getServer().getWorld("world");
        Location location = block.getLocation();

        // Allow placing the flag.
        if (banners.containsValue(block.getType())) {
            return false;
        }

        for (int x = -5; x < 5; x++) {
            for (int y = -5; y < 5; y++) {
                for (int z = -5; z < 5; z++) {
                    Block current = world.getBlockAt(
                            location.getBlockX() + x,
                            location.getBlockY() + y,
                            location.getBlockZ() + z
                    );

                    if (banners.containsValue(current.getType())) {
                        return true;
                    }
                }
            }
        }


        return false;
    }

    private boolean isBlockEnemyFlagPlacedInBase(World world, Block b, Material flag) {

        Location location = b.getLocation();

        boolean isFlagFound = false;

        for (int x = -5; x < 5; x++) {
            for (int y = -5; y < 5; y++) {
                for (int z = -5; z < 5; z++) {
                    Block current = world.getBlockAt(
                            location.getBlockX() + x,
                            location.getBlockY() + y,
                            location.getBlockZ() + z
                    );

                    if (current.getType() == flag) {
                        isFlagFound = true;
                        break;
                    }
                }
            }
        }

        return isFlagFound;
    }

    public void updateTeamScore(String color, int points) {
        if (databaseManager.updateTeamScore(color, points)) {
            scoreboardManager.updateScore(color, points);
        }
    }

}
