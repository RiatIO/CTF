package io.riat.CTF;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class ScoreboardManager {
    private final HashMap<String, ChatColor> colors = Utils.getTeamColorMap();

    private final DatabaseManager databaseManager;

    private final Scoreboard scoreboard;
    private Objective objective;

    public ScoreboardManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
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

    private void createNewTeam(String name) {
        Team team = scoreboard.registerNewTeam(name);
        team.setAllowFriendlyFire(false);
        team.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
    }

    private void createNoTeam() {
        Team team = scoreboard.registerNewTeam("NO TEAM");
        team.setAllowFriendlyFire(true);
        team.setNameTagVisibility(NameTagVisibility.NEVER);
    }

    public void updateTeam(Player player, String oldTeam, String newTeam) {

        Team teamOld = scoreboard.getTeam(oldTeam);
        Team teamNew = scoreboard.getTeam(newTeam);

        teamOld.removePlayer(player);
        teamNew.addPlayer(player);
    }

    public void getRegisteredTeamsAndScore(Objective objective) {

        try {
            PreparedStatement statement = databaseManager.getConnection().prepareStatement(
                    "SELECT * FROM teams"
            );
            ResultSet teamsResult = statement.executeQuery();

            while (teamsResult.next()) {
                String color = teamsResult.getString("color");
                int score = teamsResult.getInt("score");
                objective.getScore(colors.get(color) + color).setScore(score);
                createNewTeam(color);
            }

            createNoTeam();
        } catch (SQLException e) {
            e.getStackTrace();
        }
    }

    public void updateScore(int teamID) {
        String color = databaseManager.queryTeamColor(teamID);
        if (color == null) return;

        updateScore(color, 1);
    }

    public void updateScore(String team, int points) {
        Score score = objective.getScore(colors.get(team) + team);
        score.setScore(score.getScore() + points);
    }

    public void addTeam(Player player, String team) {
        objective.getScore(colors.get(team) + team).setScore(0);
        updatePlayerListName(player, team);
        createNewTeam(team);

        updateTeam(player, "NO TEAM", team);
    }

    public void removeTeam(String team) {
        scoreboard.resetScores(colors.get(team) + team);
        // Get all in that team, and remove them from the team.
        scoreboard.getTeam(team).unregister();
    }

    public void setPlayerTeamName(Player player) {

        String color = databaseManager.queryPlayerTeamColor(player);

        if (color == null) {
            updatePlayerListName(player, "NO TEAM");
        } else {
            updatePlayerListName(player, color);
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

        updatePlayerListName(player, team);
    }

    public void updatePlayerListName(Player player, String team) {

        player.setPlayerListName(
                String.format(" %s %s[%s]", player.getDisplayName(), colors.get(team), team)
        );

        Team t = scoreboard.getTeam(team);
        if (t == null) return;
        t.addPlayer(player);

        /*
        System.out.println("Team " + team);
        Team t = scoreboard.getTeam(team);
        if (t == null) return;

        if (team.equalsIgnoreCase("NO TEAM")) {
            System.out.println("removing " + player.getDisplayName() + " to " + team);
            t.removePlayer(player);
        } else {
            System.out.println("ADDING " + player.getDisplayName() + " to " + team);
            t.addPlayer(player);
            System.out.println(t.getNameTagVisibility());
        }*/

    }

    public void updatePlayerListName(String newPlayer, Integer team) {
        Player player = null;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getDisplayName().equalsIgnoreCase(newPlayer)) {
                player = p;
                break;
            }
        }
        if (player == null) return;

        String color = databaseManager.queryTeamColor(team);
        if (color == null) return;

        updatePlayerListName(player, color);
    }

    public Objective getObjective() {
        return objective;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }
}
