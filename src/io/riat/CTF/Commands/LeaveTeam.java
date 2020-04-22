package io.riat.CTF.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LeaveTeam implements CommandExecutor {

    private Connection connection;

    public LeaveTeam(Connection connection) {
        this.connection = connection;
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

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE uuid = ?");
            statement.setString(1, player.getUniqueId().toString());

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                Integer team = (Integer) result.getObject("team");

                return team;
            }

        } catch (SQLException e) {
            e.getStackTrace();
        }

        return null;
    }

    private void leaveTeam(Integer team, Player player) {

        try {
            PreparedStatement teamStatement = connection.prepareStatement("SELECT * FROM teams WHERE id = ?");
            teamStatement.setInt(1, team);
            ResultSet teamResult = teamStatement.executeQuery();

            if (teamResult.next()) {
                String leader = teamResult.getString("leader");
                String color = teamResult.getString("color");
                int teamID = teamResult.getInt("id");

                if (player.getUniqueId().toString().equals(leader)) {
                    // if leader, kick everyone out.
                    PreparedStatement playerStatement = connection.prepareStatement(
                            "SELECT * FROM users WHERE team = ?"
                    );
                    playerStatement.setInt(1, team);
                    ResultSet playerResult = playerStatement.executeQuery();
                    System.out.println("team " + team);
                    System.out.println("player res " + playerResult.getFetchSize());

                    while (playerResult.next()) {
                        String playerUUID = playerResult.getString("uuid");
                        String playerName = playerResult.getString("name");
                        if (!removePlayerFromTeam(playerUUID)) {
                            player.sendMessage("[CTF] Player " + playerName + " had troubles leaving, wops?");
                        }
                    }

                    // Delete the team too
                    PreparedStatement deleteTeamStatement = connection.prepareStatement(
                            "DELETE FROM teams WHERE id = ?"
                    );
                    deleteTeamStatement.setInt(1, teamID);

                    if (deleteTeamStatement.executeUpdate() > 0) {
                        player.sendMessage(
                                "[CTF] You took down the whole team " + color + " and its players, hope you're happy"
                        );
                    }
                } else {
                    // if not leader, just leave the team.
                    if (removePlayerFromTeam(player.getUniqueId().toString())) {
                        player.sendMessage("[CTF] You have left the " + color + " team, farewell!");
                    }
                }
            }

        } catch (SQLException e) {
            e.getStackTrace();
        }
    }

    private boolean removePlayerFromTeam(String uuid) {

        try {
            PreparedStatement playerStatement = connection.prepareStatement(
                    "UPDATE users SET team = ? WHERE uuid = ?");
            playerStatement.setObject(1, null);
            playerStatement.setString(2, uuid);

            if (playerStatement.executeUpdate() > 0) {

                return true;
            }

        } catch (SQLException e) {
            e.getStackTrace();
        }

        return false;
    }

}
