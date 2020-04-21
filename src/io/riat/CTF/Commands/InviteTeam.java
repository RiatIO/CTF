package io.riat.CTF.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InviteTeam implements CommandExecutor {

    private Connection connection;

    public InviteTeam(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            Integer team = getPlayerTeam(player);

            // Check if current player is in a team
            if (team == null) {
                player.sendMessage("[CTF] You're not in a team?! Create a team first (hint: /createteam <COLOR>");
                return false;
            }

            // Check if invited player is on the server (if not error)
            if (strings.length == 0) {
                return false;
            }

            String newPlayer = strings[0];

            if (!addNewPlayerToTeam(team, newPlayer)) {
                player.sendMessage(
                    "[CTF] The player you're trying to invite hasn't connected to the server before or is in a team!"
                );

                return false;
            }

            // Update the invited player team
            player.sendMessage(String.format("[CTF] %S has now been added to your team!", newPlayer));
        }

        return true;
    }

    /**
     * Check if user is in a team
     *
     * @param player current player instance.
     * @return in the team or not.
     */
    private Integer getPlayerTeam(Player player) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE uuid = ?");
            statement.setString(1, player.getUniqueId().toString());
            ResultSet userResult = statement.executeQuery();

            if (userResult.next()) {
                Integer team = (Integer) userResult.getObject("team");

                // User is already in a team!
                return team;
            }

            // This should never happen, because users should be inserted into the database on join.
            return null;

        } catch (SQLException e) {
            e.getStackTrace();
        }

        return null;
    }

    private boolean addNewPlayerToTeam(Integer team, String player) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE name = ?");
            statement.setString(1, player);
            ResultSet userResult = statement.executeQuery();

            if (userResult.next()) {
                int userPK = userResult.getInt(1);  // New player ID
                Integer getTeam = (Integer) userResult.getObject("team");

                System.out.println(getTeam + " " + userPK);

                if (getTeam != null) {
                    return false;
                }

                PreparedStatement updatePlayerStatement = connection.prepareStatement(
                        "UPDATE users SET team = ? WHERE id = ?");

                updatePlayerStatement.setInt(1, team);
                updatePlayerStatement.setInt(2, userPK);

                if (updatePlayerStatement.executeUpdate() > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.getStackTrace();
        }

        return false;
    }
}
