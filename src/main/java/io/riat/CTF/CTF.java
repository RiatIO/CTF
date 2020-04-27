package io.riat.CTF;

import io.riat.CTF.Commands.CreateTeam;
import io.riat.CTF.Commands.InviteTeam;
import io.riat.CTF.Commands.LeaveTeam;
import io.riat.CTF.Events.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class CTF extends JavaPlugin implements Listener {
    private final FileConfiguration config = getConfig();
    private Connection connection;

    private boolean production = false;

    private DatabaseManager databaseManager;

    private final int radius = 500;

    private ArrayList<Block> generatedChests = new ArrayList<>();


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
            databaseManager = new DatabaseManager(db);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        generateMapZone();
        generateChests();

        getLogger().info("Hello World 3!");

        // Register Scoreboard manager
        ScoreboardManager scoreboardManager = new ScoreboardManager(databaseManager);

        // Register Events
        getServer().getPluginManager().registerEvents(new PlayerJoin(this, databaseManager, scoreboardManager), this);
        getServer().getPluginManager().registerEvents(new BlockBreak(databaseManager, scoreboardManager), this);
        getServer().getPluginManager().registerEvents(new BlockPlace(this, databaseManager), this);
        getServer().getPluginManager().registerEvents(new CraftItem(), this);
        getServer().getPluginManager().registerEvents(new EntityExplode(), this);
        getServer().getPluginManager().registerEvents(new PlayerAttack(databaseManager, scoreboardManager), this);

        // Register Commands
        getCommand("createteam").setExecutor(new CreateTeam(databaseManager, this, scoreboardManager));
        getCommand("inviteteam").setExecutor(new InviteTeam(databaseManager, scoreboardManager));
        getCommand("leaveteam").setExecutor(new LeaveTeam(databaseManager, scoreboardManager));

        //getCommand("flag").setExecutor(new Flag());
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling plugin");
        System.out.println("Disabling");
        databaseManager.close();

        for (Block b : generatedChests) {
            b.setType(Material.AIR);
        }
    }

    public void generateChests() {
        getLogger().info("Generating Chests");
        World world = getServer().getWorld("world");
        Location spawn = world.getSpawnLocation();

        //spawn.add(0, 50, 0);
        //Block block = spawn.getBlock();


        // y = 150 - (rare chests) X 5
        for (int i = 0; i < 5; i++) {
            Random rn = new Random();
            int low = -radius / 2;
            int high = radius / 2;

            int xRandom = rn.nextInt(high - low) + low;
            int zRandom = rn.nextInt(high - low) + low;

            Block block = world.getBlockAt(spawn.getBlockX() + xRandom, 150, spawn.getBlockZ() + zRandom);
            block.setType(Material.CHEST);
            Chest chest = (Chest) block.getState();

            generatedChests.add(block);

            Inventory inv = chest.getBlockInventory();
            ItemStack diamond_sword = new ItemStack(Material.DIAMOND_SWORD, 1);
            ItemStack golden_apple = new ItemStack(Material.GOLDEN_APPLE, 1);
            ItemStack ender_pearl = new ItemStack(Material.ENDER_PEARL, 2);
            inv.addItem(diamond_sword, golden_apple, ender_pearl);
        }
        // Y = 63 - (normals chests scattered around the map (within the generated zone) X 10

        int normalBoxCount = 0;

        while (normalBoxCount < 50) {
            Random rn = new Random();
            int low = -radius / 2;
            int high = radius / 2;

            int yLow = 63;
            int yHigh = 90;

            int xRandom = rn.nextInt(high - low) + low;
            int zRandom = rn.nextInt(high - low) + low;

            int yRandom = rn.nextInt(yHigh - yLow) + yLow;

            Block block = world.getBlockAt(
                    spawn.getBlockX() + xRandom,
                    yRandom,
                    spawn.getBlockZ() + zRandom
            );

            Block checkBlock = world.getBlockAt(
                    block.getLocation().getBlockX(),
                    block.getLocation().getBlockY() - 1,
                    block.getLocation().getBlockZ()
            );

            System.out.println(checkBlock.getType());

            if (checkBlock.getType() == Material.GRASS_BLOCK
                    || checkBlock.getType() == Material.WATER
                    || checkBlock.getType() == Material.STONE
                    || checkBlock.getType() == Material.OAK_LEAVES
            ) {
                block.setType(Material.CHEST);
                Chest chest = (Chest) block.getState();

                generatedChests.add(block);

                Inventory inv = chest.getBlockInventory();
                ItemStack item1 = new ItemStack(Material.IRON_SWORD, 1);
                ItemStack item2 = new ItemStack(Material.COOKED_BEEF, 10);
                ItemStack item3 = new ItemStack(Material.IRON_PICKAXE, 2);
                inv.addItem(item1, item2, item3);
                System.out.println(block.getLocation());
                normalBoxCount++;
            }
        }

        //Location{world=CraftWorld{name=world},x=-140.0,y=63.0,z=-58.0,pitch=0.0,yaw=0.0}

        getLogger().info("Chests has been generated");
        // Y = 11 - (rare chests) X 10
    }

    public void generateMapZone() {

        getLogger().info("Generating the map zone");
        World world = getServer().getWorld("world");
        Location spawn = world.getSpawnLocation();


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
        getLogger().info("Map zone has been generated");

    }
}
