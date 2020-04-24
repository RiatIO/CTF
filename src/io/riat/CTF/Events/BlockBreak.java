package io.riat.CTF.Events;

import com.mysql.jdbc.Util;
import io.riat.CTF.ScoreboardManager;
import io.riat.CTF.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class BlockBreak implements Listener {

    private HashMap<String, Material> banners = Utils.getTeamMaterialMap();
    private Connection connection;

    private ScoreboardManager scoreboardManager;

    public BlockBreak(Connection connection, ScoreboardManager scoreboardManager) {
        this.connection = connection;
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block b = e.getBlock();
        World w = e.getPlayer().getWorld();

        // If the block getting destroyed is of type banner
        if (banners.containsValue(b.getType())) {
            String playerTeamColor = getPlayerTeam(player);

            // Check what team the player is in a team.
            if (playerTeamColor == null) {
                e.setCancelled(true);
                player.sendMessage("[CTF] You should probably be in a team before you do that.");
                return;
            }

            if (b.getType().equals(banners.get(playerTeamColor))) {
                 // Drop your own flag
                Collection<ItemStack> items = e.getBlock().getDrops();

                for (ItemStack it : items) {
                    w.dropItemNaturally(e.getBlock().getLocation().add(0.5,  1, 0.5), it);
                }
            } else {
                // If the banner is not the same team color as the player, blow the base
                player.sendMessage(ChatColor.GOLD + "[CTF] " + ChatColor.RESET + "Pick up the flag, and RUN!");
                blowTheBase(w, e);

                updateTeamScore(playerTeamColor);
            }
        }

        // Prevent people from destroying the area restriction.
        if (b.getType() == Material.WHITE_STAINED_GLASS_PANE) {
            e.setCancelled(true);
            player.sendMessage("[CTF] You can't break the dome, dude.");
        }
    }

    public String getPlayerTeam(Player player) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT t.color FROM teams t, users u WHERE u.uuid = ? AND u.team = t.id"
            );
            statement.setString(1, player.getUniqueId().toString());
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return result.getString(1);
            }
        } catch (SQLException e) {
            e.getStackTrace();
        }

        return null;
    }

    public void blowTheBase(World w, BlockBreakEvent e) {
        for (int i = 0; i < 10; i++) {
            w.spawn(e.getBlock().getLocation().add(i, i, 1), TNTPrimed.class).setFuseTicks(200);
            w.spawn(e.getBlock().getLocation().add(1, i, i), TNTPrimed.class).setFuseTicks(200);
            w.spawn(e.getBlock().getLocation().add(-1 * i, i, 1), TNTPrimed.class).setFuseTicks(200);
            w.spawn(e.getBlock().getLocation().add(1, i, -1 * i), TNTPrimed.class).setFuseTicks(200);
        }
        Bukkit.broadcastMessage("[CTF] - Black base self-destructing in 10 seconds...");
    }

    public void updateTeamScore(String color) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE teams SET score = score + 1 WHERE color = ?"
            );
            statement.setString(1, color);

            if (statement.executeUpdate() > 0) {
                System.out.println("UPDATED SCORE");
                scoreboardManager.updateScore(color);
            }

        } catch (SQLException e) {
            e.getStackTrace();
        }
    }
}
