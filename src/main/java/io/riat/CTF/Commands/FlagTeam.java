package io.riat.CTF.Commands;

import io.riat.CTF.DatabaseManager;
import io.riat.CTF.ScoreboardManager;
import io.riat.CTF.Utils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

import static io.riat.CTF.Utils.CTF_TAG;

public class FlagTeam implements CommandExecutor  {

    private final HashMap<String, Material> banners = Utils.getTeamMaterialMap();

    private final DatabaseManager databaseManager;
    private final ScoreboardManager scoreboardManager;

    public FlagTeam(DatabaseManager databaseManager, ScoreboardManager scoreboardManager) {
        this.databaseManager = databaseManager;
        this.scoreboardManager = scoreboardManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Get the team that the player is in.
            String team = databaseManager.queryPlayerTeamColor(player);

            if (team == null) {
                player.sendMessage(CTF_TAG + "You need to be in a team in order to get a flag.");
                return false;
            }

            // Give the player the flag
            player.getInventory().addItem(new ItemStack(banners.get(team)));

            // Deduct 3 points from the player's team.
            if (databaseManager.updateTeamScore(team, -3)) {
                player.sendMessage(CTF_TAG + "You got flag; however, your team lost 3 points...");

                scoreboardManager.updateScore(team, -3);
                return true;
            }
        }

        return true;
    }
}
