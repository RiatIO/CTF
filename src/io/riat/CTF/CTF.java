package io.riat.CTF;

import io.riat.CTF.Commands.CreateTeam;
import io.riat.CTF.Commands.InviteTeam;
import io.riat.CTF.Commands.LeaveTeam;
import io.riat.CTF.Events.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CTF extends JavaPlugin implements Listener {

    private Connection connection;

    @Override
    public void onEnable() {

        // Database
        Database db = new Database("localhost", 3306 ,"ctf_db", "deep", "test");
        try {
            connection = db.openConnection();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        generateMapZone();

        getLogger().info("Hello World 3!");

        // Register Scoreboard manager
        ScoreboardManager scoreboardManager = new ScoreboardManager(connection);

        // Register Events
        getServer().getPluginManager().registerEvents(new PlayerJoin(this, connection, scoreboardManager), this);
        getServer().getPluginManager().registerEvents(new BlockBreak(connection, scoreboardManager), this);
        getServer().getPluginManager().registerEvents(new BlockPlace(this), this);
        getServer().getPluginManager().registerEvents(new CraftItem(), this);
        getServer().getPluginManager().registerEvents(new EntityExplode(), this);

        // Register Commands
        getCommand("createteam").setExecutor(new CreateTeam(connection, this, scoreboardManager));
        getCommand("inviteteam").setExecutor(new InviteTeam(connection, scoreboardManager));
        getCommand("leaveteam").setExecutor(new LeaveTeam(connection, scoreboardManager));
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public void generateMapZone() {
        World world = getServer().getWorld("world");
        Location spawn = world.getSpawnLocation();

        int radius = 500;

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
