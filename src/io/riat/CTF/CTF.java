package io.riat.CTF;

import io.riat.CTF.Commands.CreateTeam;
import io.riat.CTF.Commands.InviteTeam;
import io.riat.CTF.Commands.LeaveTeam;
import io.riat.CTF.Events.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CTF extends JavaPlugin implements Listener {
    private FileConfiguration config = getConfig();
    private Connection connection;

    private boolean production = false;

    @Override
    public void onEnable() {
        // Database

        Database db;

        if (!production) {
            db = new Database("localhost", 3306, "ctf_db", "deep", "test");
        } else {
            db = new Database(
                    config.getString("host"),
                    config.getInt("port"),
                    config.getString("database"),
                    config.getString("username"),
                    config.getString("password")
            );
        }

        try {
            connection = db.openConnection();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        generateMapZone();

        getLogger().info("Hello World 3!");

        System.out.println(config.getBoolean("test"));
        System.out.println(config.getString("db"));

        // Register Scoreboard manager
        ScoreboardManager scoreboardManager = new ScoreboardManager(connection);

        // Register Events
        getServer().getPluginManager().registerEvents(new PlayerJoin(this, connection, scoreboardManager), this);
        getServer().getPluginManager().registerEvents(new BlockBreak(connection, scoreboardManager), this);
        getServer().getPluginManager().registerEvents(new BlockPlace(this, connection), this);
        getServer().getPluginManager().registerEvents(new CraftItem(), this);
        getServer().getPluginManager().registerEvents(new EntityExplode(), this);
        getServer().getPluginManager().registerEvents(new PlayerAttack(connection, scoreboardManager), this);

        // Register Commands
        getCommand("createteam").setExecutor(new CreateTeam(connection, this, scoreboardManager));
        getCommand("inviteteam").setExecutor(new InviteTeam(connection, scoreboardManager));
        getCommand("leaveteam").setExecutor(new LeaveTeam(connection, scoreboardManager));
    }

    @Override
    public void onDisable() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.getStackTrace();
        }
    }

    public void generateMapZone() {
        World world = getServer().getWorld("world");
        Location spawn = world.getSpawnLocation();

        int radius = 200;

        spawn.subtract(radius / 2, 0, radius / 2);

        for (int x = 0; x < radius; x++) {
            for (int y = 0; y < 256; y++) {
                world.getBlockAt(spawn.getBlockX() + x, y, spawn.getBlockZ()).setType(Material.WHITE_STAINED_GLASS_PANE);
            }
        }

        for (int x = 0; x < radius; x++) {
            for (int y = 0; y < 256; y++) {
                world.getBlockAt(spawn.getBlockX() + x, y, spawn.getBlockZ() + radius - 1).setType(Material.WHITE_STAINED_GLASS_PANE);
            }
        }

        for (int z = 0; z < radius; z++) {
            for (int y = 0; y < 256; y++) {
                world.getBlockAt(spawn.getBlockX(), y, spawn.getBlockZ() + z).setType(Material.WHITE_STAINED_GLASS_PANE);
            }
        }

        for (int z = 0; z < radius; z++) {
            for (int y = 0; y < 256; y++) {
                world.getBlockAt(spawn.getBlockX() + radius - 1, y, spawn.getBlockZ() + z).setType(Material.WHITE_STAINED_GLASS_PANE);
            }
        }
    }
}
