package io.riat.CTF;

import io.riat.CTF.Commands.CreateTeam;
import io.riat.CTF.Commands.InviteTeam;
import io.riat.CTF.Commands.LeaveTeam;
import io.riat.CTF.Events.BlockBreak;
import io.riat.CTF.Events.PlayerJoin;
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
            Statement statement = connection.createStatement();

            ResultSet result = statement.executeQuery("SELECT * FROM users");

            while (result.next()) {
                System.out.println("RES " + result.getString("id"));
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        generateMapZone();

        getLogger().info("Hello World 3!");

        // Register Events
        getServer().getPluginManager().registerEvents(new PlayerJoin(this, connection), this);
        getServer().getPluginManager().registerEvents(new BlockBreak(), this);

        // Register Commands
        getCommand("createteam").setExecutor(new CreateTeam(connection, this));
        getCommand("inviteteam").setExecutor(new InviteTeam(connection));
        getCommand("leaveteam").setExecutor(new LeaveTeam(connection));
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public void generateMapZone() {
        World world = getServer().getWorld("world");
        Location spawn = world.getSpawnLocation();

        spawn.getBlock().setType(Material.SAND);

        for (int z = 0; z < 10; z++) {
            world.getBlockAt(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ() + z).setType(Material.GRAY_STAINED_GLASS_PANE);
            for (int y = 0; y <= 10; y++) {
                world.getBlockAt(spawn.getBlockX(), spawn.getBlockY() + 1, spawn.getBlockZ() + z).setType(Material.GRAY_STAINED_GLASS_PANE);
                world.getBlockAt(spawn.getBlockX(), spawn.getBlockY() + 2, spawn.getBlockZ() + z).setType(Material.GRAY_STAINED_GLASS_PANE);
            }
        }
    }

}
