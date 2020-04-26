package io.riat.CTF.Events;

import io.riat.CTF.DatabaseManager;
import io.riat.CTF.ScoreboardManager;
import io.riat.CTF.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class PlayerJoin implements Listener {

    HashMap<String, ChatColor> colors = Utils.getTeamColorMap();

    private Plugin plugin;
    private ScoreboardManager scoreboardManager;
    private final DatabaseManager databaseManager;

    public PlayerJoin(Plugin plugin, DatabaseManager databaseManager, ScoreboardManager scoreboardManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        scoreboardManager.setPlayerTeamName(player);

        player.setScoreboard(scoreboardManager.getScoreboard());

        asyncUserStatus(player);
    }

    private void asyncUserStatus(Player player) {

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            if (databaseManager.queryInsertPlayer(player)) {
                player.sendMessage("Welcome to CTF, " + player.getDisplayName());
                Bukkit.broadcastMessage("[CTF] " + player.getDisplayName() + " joined the server for the first time!");
            } else {
                player.sendMessage("Welcome back, " + player.getDisplayName());
            }
        });
    }

}
