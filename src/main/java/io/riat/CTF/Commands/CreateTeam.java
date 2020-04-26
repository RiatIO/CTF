package io.riat.CTF.Commands;

import io.riat.CTF.ScoreboardManager;
import io.riat.CTF.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import java.sql.*;
import java.util.HashMap;

public class CreateTeam implements CommandExecutor {

    private HashMap<String, ChatColor> colors = Utils.getTeamColorMap();

    private Connection connection;
    private Plugin plugin;
    private ScoreboardManager scoreboardManager;

    public CreateTeam(Connection connection, Plugin plugin, ScoreboardManager scoreboardManager) {
        this.connection = connection;
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        HashMap<String, Material> team = Utils.getTeamMaterialMap();

        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if (strings.length == 0) {
                return false;
            }

            String teamColor = strings[0].toUpperCase();

            if (!team.containsKey(teamColor)) {
                return false;
            }

            if (isPlayerInTeam(player)) {
                player.sendMessage("[CTF] You're already in a team! Leave by typing /leaveteam");
                return true;
            }

            if (isTeamColorUsed(teamColor)) {
                player.sendMessage("[CTF] Team is already selected, try again!");
                return false;
            }


            if (!createTeam(player, teamColor)) {
                player.sendMessage("[CTF] Something went wrong while parsing the data, beep boop.");
                return false;
            }

            player.sendMessage(String.format(
                    "[CTF] Team (%s) has been created! Do /teaminvite [PLAYER] to invite other players",
                    teamColor
                )
            );
            player.getInventory().addItem(new ItemStack(team.get(teamColor)));

            // Update the scoreboard for all of the connected users.
            scoreboardManager.addTeam(player, teamColor);
        }

        return true;
    }

    /**
     * Check if user is in a team
     *
     * @param player current player instance.
     * @return in the team or not.
     */
    private boolean isPlayerInTeam(Player player) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE uuid = ?")) {
            statement.setString(1, player.getUniqueId().toString());

            try (ResultSet userResult = statement.executeQuery()) {
                if (userResult.next()) {
                    Integer team = (Integer) userResult.getObject("team");

                    // If the user is not in a team (is null), then return false | User is already in a team!
                    return team != null;
                }
            }
            // This should never happen, because users should be inserted into the database on join.
            return false;

        } catch (SQLException e) {
            e.getStackTrace();
        }

        return false;
    }

    private boolean isTeamColorUsed(String color) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM teams WHERE color = ?");
            statement.setString(1, color);
            ResultSet result = statement.executeQuery();

            return result.next();

        } catch (SQLException e) {
            e.getStackTrace();
        }

        return true;
    }

    private boolean createTeam(Player player, String color) {
        try {
            // Insert new team into table, and return the primary key
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO teams (leader, color, score, flag_placed) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, color);
            statement.setInt(3, 0);
            statement.setInt(4, 0);

            int teamsResult = statement.executeUpdate();

            if (teamsResult > 0) {
                ResultSet rs = statement.getGeneratedKeys();
                rs.next();

                int teamPK = rs.getInt(1);

                // Update the user team field, with the primary key of the team.
                PreparedStatement insertUserStatement = connection.prepareStatement(
                        "UPDATE users SET team = ? WHERE uuid = ?"
                );

                insertUserStatement.setInt(1, teamPK);
                insertUserStatement.setString(2, player.getUniqueId().toString());

                int usersResult = insertUserStatement.executeUpdate();

                if (usersResult > 0) {
                    return true;
                }
            }

        } catch (SQLException e) {
            e.getStackTrace();
        }
        return false;
    }
}