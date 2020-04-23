package io.riat.CTF.Events;

import io.riat.CTF.Commands.CreateTeam;
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
import org.bukkit.scoreboard.Score;
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

    public PlayerJoin(Plugin plugin, Connection connection) {
        this.plugin = plugin;
        this.connection = connection;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        createScoreboard(player);

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

    public void createScoreboard(Player player) {
        HashMap<String, ChatColor> colors = Utils.getTeamColorMap();

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        Objective objective = scoreboard.registerNewObjective("scoreboard", "dummy", "test");

        objective.setDisplayName(ChatColor.GOLD + "[CTF] Team Scoreboard");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        getRegisteredTeamsAndScore(objective);
        /*
        HashMap<String, Score> test = new HashMap<String, Score>();
        test.put("WHITE", objective.getScore(colors.get("WHITE") + "WHITE"));


        Score WHITE = objective.getScore(colors.get("WHITE") + "WHITE");
        Score ORANGE = objective.getScore(colors.get("ORANGE") + "ORANGE");
        Score MAGENTA = objective.getScore(colors.get("MAGENTA") + "MAGENTA");
        Score YELLOW = objective.getScore(colors.get("YELLOW") + "YELLOW");
        Score LIME = objective.getScore(colors.get("LIME") + "LIME");
        Score PINK = objective.getScore(colors.get("PINK") + "PINK");
        Score GRAY = objective.getScore(colors.get("GRAY") + "GRAY");
        Score CYAN = objective.getScore(colors.get("CYAN") + "CYAN");
        Score PURPLE = objective.getScore(colors.get("PURPLE") + "PURPLE");
        Score BLUE = objective.getScore(colors.get("BLUE") + "BLUE");
        Score BROWN = objective.getScore(colors.get("BROWN") + "BROWN");
        Score GREEN = objective.getScore(colors.get("GREEN") + "GREEN");
        Score RED = objective.getScore(colors.get("RED") + "RED");
        Score BLACK = objective.getScore(colors.get("BLACK") + "BLACK");

        test.get("WHITE").setScore(0);
        //WHITE.setScore(0);
        ORANGE.setScore(0);
        MAGENTA.setScore(0);
        YELLOW.setScore(0);
        LIME.setScore(0);
        PINK.setScore(0);
        GRAY.setScore(0);
        CYAN.setScore(0);
        PURPLE.setScore(0);
        BROWN.setScore(0);
        RED.setScore(0);
        BLUE.setScore(0);
        BLACK.setScore(1);
        GREEN.setScore(1);
        */

        setPlayerTeamName(player);
        player.setScoreboard(scoreboard);
    }

    public void getRegisteredTeamsAndScore(Objective objective) {

        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM teams"
            );
            ResultSet teamsResult = statement.executeQuery();

            //HashMap<String, Score> teams = new HashMap<>();

            while (teamsResult.next()) {
                String color = teamsResult.getString("color");
                int score = teamsResult.getInt("score");
                objective.getScore(colors.get(color) + color).setScore(score);
            }
        } catch (SQLException e) {
            e.getStackTrace();
        }
    }

    public void setPlayerTeamName(Player player) {


        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT t.color FROM teams t, users u WHERE u.uuid = ? AND u.team = t.id"
            );
            statement.setString(1, player.getUniqueId().toString());
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                String color = result.getString(1);

                player.setPlayerListName(
                        String.format(" %s %s[%s]", player.getDisplayName(), colors.get(color), color)
                );
            } else {
                player.setPlayerListName(
                        String.format(" %s %s[%s]", player.getDisplayName(), ChatColor.BOLD, "NO TEAM")
                );
            }


        } catch (SQLException e) {
            e.getStackTrace();
        }

    }

}
