package io.riat.CTF;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class ScoreboardManager {
    private HashMap<String, ChatColor> colors = Utils.getTeamColorMap();

    private Connection connection;

    private Scoreboard scoreboard;
    private Objective objective;

    public ScoreboardManager(Connection connection) {
        this.connection = connection;
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        init();
    }

    private void init() {
        objective = scoreboard.getObjective("scoreboard");

        if (objective == null) {
            objective = scoreboard.registerNewObjective("scoreboard", "dummy", "test");
        }

        objective.setDisplayName(ChatColor.GOLD + "[CTF] Team Scoreboard");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        getRegisteredTeamsAndScore(objective);
    }

    public void getRegisteredTeamsAndScore(Objective objective) {

        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM teams"
            );
            ResultSet teamsResult = statement.executeQuery();

            while (teamsResult.next()) {
                String color = teamsResult.getString("color");
                int score = teamsResult.getInt("score");
                objective.getScore(colors.get(color) + color).setScore(score);
            }
        } catch (SQLException e) {
            e.getStackTrace();
        }
    }

    public void updateScore(String team) {
        Score score = objective.getScore(colors.get(team) + team);
        score.setScore(score.getScore() + 1);
    }

    public void addTeam(Player player, String team) {
        objective.getScore(colors.get(team) + team).setScore(0);
        updatePlayerListName(player, team);
    }

    public void removeTeam(String team) {
        scoreboard.resetScores(colors.get(team) + team);

        // Get all in that team, and remove them from the team.
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
                updatePlayerListName(player, color);
            } else {
                updatePlayerListName(player, "NO TEAM");
            }

        } catch (SQLException e) {
            e.getStackTrace();
        }

    }

    public void updatePlayerListName(String uPlayer, String team) {
        Player player = null;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getDisplayName().equals(uPlayer)) {
                player = p;
                break;
            }
        }

        if (player == null) return;

        player.setPlayerListName(
                String.format(" %s %s[%s]", player.getDisplayName(), colors.get(team), team)
        );

    }

    public void updatePlayerListName(Player player, String team) {
        player.setPlayerListName(
                String.format(" %s %s[%s]", player.getDisplayName(), colors.get(team), team)
        );
    }

    public void updatePlayerListName(String newPlayer, Integer team) {
        Player player = null;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getDisplayName().equals(newPlayer)) {
                player = p;
                break;
            }
        }

        if (player == null) return;

        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM teams WHERE id = ?"
            );
            statement.setInt(1, team);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                String teamName = result.getString("color");

                updatePlayerListName(player, teamName);
            }

        } catch(SQLException e) {
            e.getStackTrace();
        }
    }

    public Objective getObjective() {
        return objective;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }
}
