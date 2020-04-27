package io.riat.CTF.Commands;

import io.riat.CTF.DatabaseManager;
import io.riat.CTF.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LeaveTeam implements CommandExecutor {

    private final ScoreboardManager scoreboardManager;
    private final DatabaseManager databaseManager;

    public LeaveTeam(DatabaseManager databaseManager, ScoreboardManager scoreboardManager) {
        this.databaseManager = databaseManager;
        this.scoreboardManager = scoreboardManager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            // Check if in team.
            Integer team = isPlayerInTeam(player);

            if (team == null) {
                player.sendMessage("[CTF] You can't leave a team, if your not in a team!");
                return false;
            }

            leaveTeam(team, player);
        }

        return true;
    }

    private Integer isPlayerInTeam(Player player) {
        return databaseManager.queryPlayerTeam(player);
    }

    private void leaveTeam(Integer team, Player player) {
        String color = databaseManager.queryTeamColor(team);
        if (color == null) return;;

        if (removePlayerFromTeam(player.getUniqueId().toString())) {
            player.sendMessage("[CTF] You have left the " + color + " team, farewell!");
            scoreboardManager.updatePlayerListName(player, "NO TEAM");
            scoreboardManager.updateTeam(player, color, "NO TEAM");
        }

        // Check how many users that are in the team
        if (!doesTeamHaveAnyPlayers(color)) {
            // Delete the team too

            if (databaseManager.deleteTeam(color)) {
                Bukkit.broadcastMessage("[CTF] Team " + color + " no longer exists!");
                scoreboardManager.removeTeam(color);
            }
        }
    }

    private boolean removePlayerFromTeam(String uuid) {
        return databaseManager.updatePlayerTeam(uuid, null);
    }

    private boolean doesTeamHaveAnyPlayers(String color) {
        return databaseManager.queryPlayersInTeam(color);
    }

}
