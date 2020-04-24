package io.riat.CTF.Events;

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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class PlayerJoin implements Listener {

    HashMap<String, ChatColor> colors = Utils.getTeamColorMap();

    private Connection connection;
    private Plugin plugin;
    private ScoreboardManager scoreboardManager;

    public PlayerJoin(Plugin plugin, Connection connection, ScoreboardManager scoreboardManager) {
        this.plugin = plugin;
        this.connection = connection;
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

            try {
                PreparedStatement getPlayerStatement = connection.prepareStatement("SELECT * FROM users WHERE uuid = ?");
                getPlayerStatement.setString(1, player.getUniqueId().toString());
                ResultSet userResult = getPlayerStatement.executeQuery();

                if (userResult.next()) {
                    String name = userResult.getString("name");
                    player.sendMessage("Welcome back, " + name);
                } else {
                    PreparedStatement insertPlayerStatement = connection.prepareStatement(
                            "INSERT INTO users (uuid, name) VALUES (?, ?)"
                    );
                    insertPlayerStatement.setString(1, player.getUniqueId().toString());
                    insertPlayerStatement.setString(2, player.getDisplayName());

                    int usersResult = insertPlayerStatement.executeUpdate();

                    if (usersResult > 0) {
                        player.sendMessage("Welcome to CTF, " + player.getDisplayName());
                    }
                }

            } catch (SQLException e) {
                e.getStackTrace();
            }
        });
    }

}
