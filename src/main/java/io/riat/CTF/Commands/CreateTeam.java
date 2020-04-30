package io.riat.CTF.Commands;

import io.riat.CTF.DatabaseManager;
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

import static io.riat.CTF.Utils.CTF_TAG;

public class CreateTeam implements CommandExecutor {

    private HashMap<String, ChatColor> colors = Utils.getTeamColorMap();

    private DatabaseManager databaseManager;
    private Plugin plugin;
    private ScoreboardManager scoreboardManager;

    public CreateTeam(DatabaseManager databaseManager, Plugin plugin, ScoreboardManager scoreboardManager) {
        this.databaseManager = databaseManager;
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
                player.sendMessage(CTF_TAG + "You're already in a team! Leave by typing /leaveteam");
                return true;
            }

            if (isTeamColorUsed(teamColor)) {
                player.sendMessage(CTF_TAG + "Team is already selected, try again!");
                return false;
            }


            if (!createTeam(player, teamColor)) {
                player.sendMessage(CTF_TAG + "Something went wrong while parsing the data, beep boop.");
                return false;
            }

            player.sendMessage(String.format(
                    CTF_TAG + "Team (%s) has been created! Do /teaminvite [PLAYER] to invite other players",
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
        Integer team = databaseManager.queryPlayerTeam(player);
        System.out.println("Is player in team " + team);
        return team != null;
    }

    private boolean isTeamColorUsed(String color) {
        boolean res = databaseManager.queryTeamColor(color);
        System.out.println("Team Color used " + res);
        return res;
    }

    private boolean createTeam(Player player, String color) {
        boolean res = databaseManager.inertTeam(player, color);
        System.out.println("Create team " + res);
        return res;
    }
}
