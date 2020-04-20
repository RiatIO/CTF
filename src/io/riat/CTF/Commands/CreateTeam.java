package io.riat.CTF.Commands;

import io.riat.CTF.Utils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreateTeam implements CommandExecutor {

    private Connection connection;

    public CreateTeam(Connection connection) {
        this.connection = connection;
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

            try {

                // Check if user already has a team
                PreparedStatement checkUserStmt = connection.prepareStatement("SELECT * FROM users WHERE uuid = ?");
                checkUserStmt.setString(1, player.getUniqueId().toString());
                ResultSet userResult = checkUserStmt.executeQuery();

                if (userResult.next()) {
                    player.sendMessage("[CTF] You already have/own a team! Do /leaveteam if you wish to abandon your fellow comrades");

                    return true;
                }

                PreparedStatement statement = connection.prepareStatement("SELECT * FROM teams WHERE color = ?");
                statement.setString(1, teamColor);
                ResultSet result = statement.executeQuery();

                if (!result.next()) {
                    System.out.println("Empty");

                    // Insert into database
                    PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO teams (leader, color) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
                    insertStatement.setString(1, player.getUniqueId().toString());
                    insertStatement.setString(2, teamColor);

                    int insertRes = insertStatement.executeUpdate();

                    if (insertRes > 0) {
                        player.sendMessage("[CTF] Team has been created! Do /teaminv [PLAYER] to create a team");
                        ResultSet rs = insertStatement.getGeneratedKeys();
                        rs.next();

                        int teamPK = rs.getInt(1);

                        PreparedStatement insertUserStatement = connection.prepareStatement("INSERT INTO users (uuid, name, team) VALUES (?, ?, ?)");
                        insertUserStatement.setString(1, player.getUniqueId().toString());
                        insertUserStatement.setString(2, player.getDisplayName());
                        insertUserStatement.setInt(3, teamPK);

                        int insertUserRes = insertUserStatement.executeUpdate();

                    } else {
                        player.sendMessage("[CTF] Something went wrong while parsing the data, beep boop.");
                    }

                } else {
                    System.out.println("Not empty");
                    player.sendMessage("[CTF] Team is already selected, try again!");
                    return false;
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            player.sendMessage("HEI " + team.get(teamColor));

            player.getInventory().addItem(new ItemStack(team.get(teamColor)));

            // Check if team is registered
        }

        return true;
    }
}
