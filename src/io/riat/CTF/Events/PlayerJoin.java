package io.riat.CTF.Events;

import io.riat.CTF.Commands.CreateTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerJoin implements Listener {

    private Connection connection;
    private Plugin plugin;

    public PlayerJoin(Plugin plugin, Connection connection) {
        this.plugin = plugin;
        this.connection = connection;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        asyncUserStatus(player);
    }

    private void asyncUserStatus(Player player) {

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            try {
                PreparedStatement getPlayerStatement = connection.prepareStatement("SELECT * FROM users WHERE uuid = ?");
                getPlayerStatement.setString(1, player.getUniqueId().toString());
                ResultSet userResult = getPlayerStatement.executeQuery();

                if (userResult.next()) {
                    String name = userResult.getString("name");
                    player.sendMessage("Welcome back, " + name);
                } else {
                    PreparedStatement insertPlayerStatement = connection.prepareStatement(
                            "INSERT INTO users (uuid, name) VALUES (?, ?)"
                    );
                    insertPlayerStatement.setString(1, player.getUniqueId().toString());
                    insertPlayerStatement.setString(2, player.getDisplayName());

                    int usersResult = insertPlayerStatement.executeUpdate();

                    if (usersResult > 0) {
                        player.sendMessage("Welcome to CTF, " + player.getDisplayName());
                    }
                }

            } catch (SQLException e) {
                e.getStackTrace();
            }
        });
    }

    public interface Callback<T> {
        void onSuccess(T done);
        void onFailure(Throwable cause);
    }

}
