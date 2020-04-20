package io.riat.CTF.Commands;

import io.riat.CTF.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.HashMap;

public class CreateTeam implements CommandExecutor {

    private Connection connection;
    private Plugin plugin;

    public CreateTeam(Connection connection, Plugin plugin) {
        this.connection = connection;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        HashMap<String, Material> team = Utils.getTeamMap();

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

            player.sendMessage(String.format("[CTF] Team (%s) has been created! Do /teaminv [PLAYER] to create a team", teamColor));
            player.getInventory().addItem(new ItemStack(team.get(teamColor)));
        }

        return true;
    }

    private boolean isPlayerInTeam(Player player) {
        // Check if user already has a team
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE uuid = ?");
            statement.setString(1, player.getUniqueId().toString());

            ResultSet userResult = statement.executeQuery();

            return userResult.next();

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
            // Insert into database
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO teams (leader, color) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );

            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, color);

            int teamsResult = statement.executeUpdate();

            if (teamsResult > 0) {
                ResultSet rs = statement.getGeneratedKeys();
                rs.next();

                int teamPK = rs.getInt(1);

                PreparedStatement insertUserStatement = connection.prepareStatement(
                        "INSERT INTO users (uuid, name, team) VALUES (?, ?, ?)"
                );
                insertUserStatement.setString(1, player.getUniqueId().toString());
                insertUserStatement.setString(2, player.getDisplayName());
                insertUserStatement.setInt(3, teamPK);

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


    private void asyncIsPlayerInTeam(Player player, Callback<Boolean> callback) {

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            final Boolean[] res = new Boolean[1];

            try {
                PreparedStatement checkUserStmt = connection.prepareStatement("SELECT * FROM users WHERE uuid = ?");
                checkUserStmt.setString(1, player.getUniqueId().toString());
                ResultSet userResult = checkUserStmt.executeQuery();

                res[0] = userResult.next();
            } catch (SQLException e) {
                callback.onFailure(e);
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                callback.onSuccess(res[0]);
            });
        });
    }

    public interface Callback<T> {
        boolean onSuccess(T done);
        void onFailure(Throwable cause);
    }


}
