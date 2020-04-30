package io.riat.CTF.Events;

import io.riat.CTF.DatabaseManager;
import io.riat.CTF.ScoreboardManager;
import io.riat.CTF.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;

import static io.riat.CTF.Utils.CTF_TAG;

public class PlayerJoin implements Listener {

    HashMap<String, ChatColor> colors = Utils.getTeamColorMap();

    private Plugin plugin;
    private ScoreboardManager scoreboardManager;
    private final DatabaseManager databaseManager;

    public PlayerJoin(Plugin plugin, DatabaseManager databaseManager, ScoreboardManager scoreboardManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        scoreboardManager.setPlayerTeamName(player);

        player.setScoreboard(scoreboardManager.getScoreboard());

        asyncUserStatus(player);
    }

    private void asyncUserStatus(Player player) {

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (databaseManager.queryInsertPlayer(player)) {
                World w = plugin.getServer().getWorld("world");
                Location spawn = w.getSpawnLocation();

                player.sendMessage("Welcome to CTF, " + player.getDisplayName());
                Bukkit.broadcastMessage(CTF_TAG + player.getDisplayName() + " joined the server for the first time!");
            } else {
                player.sendMessage("Welcome back, " + player.getDisplayName());
            }
        });
    }

}
