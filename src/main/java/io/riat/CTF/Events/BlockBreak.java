package io.riat.CTF.Events;

import com.mysql.jdbc.*;
import io.riat.CTF.DatabaseManager;
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

    private final HashMap<String, Material> banners = Utils.getTeamMaterialMap();

    private final ScoreboardManager scoreboardManager;
    private final DatabaseManager databaseManager;

    public BlockBreak(DatabaseManager databaseManager, ScoreboardManager scoreboardManager) {
        this.databaseManager = databaseManager;
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block b = e.getBlock();
        World w = e.getPlayer().getWorld();

        if (w.getBlockAt(b.getLocation().add(0, 1, 0)).getType().toString().contains("BANNER")) {
            e.setCancelled(true);
        }

        // If the block getting destroyed is of type banner
        if (banners.containsValue(b.getType())) {
            String playerTeamColor = getPlayerTeam(player);

            // Check what team the player is in a team.
            if (playerTeamColor == null) {
                e.setCancelled(true);
                player.sendMessage("[CTF] You should probably be in a team before you do that.");
                return;
            }

            String flag = b.getType().toString().split("_")[0];

            // If the banner is not the same team color as the player, blow the base
            if (!b.getType().equals(banners.get(playerTeamColor))) {
                blowTheBase(w, e);
                e.setDropItems(false);

                // Give the flag killer the flag
                player.getInventory().addItem(new ItemStack(b.getType()));
                player.sendMessage("[CTF] Bring back the enemy flag to your own base, and place it to get 5 points!");

                Bukkit.broadcastMessage(String.format("[CTF] Team %s just took down team %s flag!", playerTeamColor, flag));
            }

            databaseManager.flagRemoved(flag);
        }

        // Prevent people from destroying the area restriction.
        if (b.getType() == Material.WHITE_STAINED_GLASS_PANE) {
            e.setCancelled(true);
            player.sendMessage("[CTF] You can't break the dome, dude.");
        }
    }

    public String getPlayerTeam(Player player) {
        return databaseManager.queryPlayerTeamColor(player);
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
        if (databaseManager.updateTeamScore(color, 5)) {
            scoreboardManager.updateScore(color, 5);
        }
    }
}
