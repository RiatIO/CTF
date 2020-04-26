package io.riat.CTF.Commands;

import io.riat.CTF.DatabaseManager;
import io.riat.CTF.ScoreboardManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InviteTeam implements CommandExecutor {

    private final ScoreboardManager scoreboardManager;
    private final DatabaseManager databaseManager;

    public InviteTeam(DatabaseManager databaseManager, ScoreboardManager scoreboardManager) {
        this.databaseManager = databaseManager;
        this.scoreboardManager = scoreboardManager;
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
            scoreboardManager.updatePlayerListName(newPlayer, team);
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
        return databaseManager.queryPlayerTeam(player);
    }

    private boolean addNewPlayerToTeam(Integer team, String player) {
        return databaseManager.insertPlayerToTeam(team, player);
    }
}
