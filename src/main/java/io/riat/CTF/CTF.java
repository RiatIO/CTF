package io.riat.CTF;

import com.google.common.collect.Iterables;
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

    private DatabaseManager databaseManager;

    private final int radius = 500;

    private final ArrayList<Block> generatedChests = new ArrayList<>();


    @Override
    public void onEnable() {
        // Database
        Database db;

        boolean production = false;

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

        databaseManager = new DatabaseManager(db);

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
        System.out.println("Time: " + System.currentTimeMillis() / 1000L);
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling plugin");
        databaseManager.close();

        for (Block b : generatedChests) {
            b.setType(Material.AIR);
        }
    }

    public void generateMapZone() {
        getLogger().info("Generating the map zone");

        World world = getServer().getWorld("world");
        Location spawn = world.getSpawnLocation();

        spawn.subtract(radius / 2, 0, radius / 2);

        for (int x = 0; x < radius; x++) {
            for (int y = 0; y < 256; y++) {
                world.getBlockAt(spawn.getBlockX() + x, y, spawn.getBlockZ()).setType(Material.WHITE_STAINED_GLASS_PANE);
                world.getBlockAt(spawn.getBlockX() + x, y, spawn.getBlockZ() + radius - 1).setType(Material.WHITE_STAINED_GLASS_PANE);
            }
        }

        for (int z = 0; z < radius; z++) {
            for (int y = 0; y < 256; y++) {
                world.getBlockAt(spawn.getBlockX(), y, spawn.getBlockZ() + z).setType(Material.WHITE_STAINED_GLASS_PANE);
                world.getBlockAt(spawn.getBlockX() + radius - 1, y, spawn.getBlockZ() + z).setType(Material.WHITE_STAINED_GLASS_PANE);
            }
        }

        getLogger().info("Map zone has been generated");
    }

    public void generateChests() {
        getLogger().info("Generating Chests");
        World world = getServer().getWorld("world");
        Location spawn = world.getSpawnLocation();

        ArrayList<ItemStack> rare = new ArrayList<ItemStack>() {
            {
                add(new ItemStack(Material.DIAMOND_SWORD, 1));
                add(new ItemStack(Material.GOLDEN_APPLE, 1));
                add(new ItemStack(Material.ENDER_PEARL, 2));
            }
        };

        ArrayList<ItemStack> normal = new ArrayList<ItemStack>() {
            {
                add(new ItemStack(Material.IRON_SWORD, 1));
                add(new ItemStack(Material.COOKED_BEEF, 4));
                add(new ItemStack(Material.STONE_AXE, 1));
                add(new ItemStack(Material.STONE_PICKAXE, 1));
                add(new ItemStack(Material.TORCH, 8));
                add(new ItemStack(Material.RED_BED, 1));
            }
        };

        // y = 150 - (rare chests) X 5
        addChest(world, spawn, new int[] {125, 130}, rare, 2, null);

        // Y = 63 - (normals chests scattered around the map (within the generated zone) X 10
        addChest(world, spawn, new int[] {63, 90}, normal, 20, new ArrayList<Material>() {
            {
                add(Material.GRASS_BLOCK);
                add(Material.WATER);
                add(Material.STONE);
                add(Material.OAK_LEAVES);
                add(Material.SAND);
            }
        });

        // Y = 11 - (rare chests) X 10

        getLogger().info("Chests has been generated");
    }

    public void addChest(World world, Location spawn, int[] yMinMax, ArrayList<ItemStack> items, int interations, ArrayList<Material> conditions) {
        int i = 0;

        while (i < interations) {
            Random rn = new Random();
            int low = -radius / 2;
            int high = radius / 2;

            int xRandom = rn.nextInt(high - low) + low;
            int yRandom = rn.nextInt(yMinMax[1] - yMinMax[0]) + yMinMax[0];
            int zRandom = rn.nextInt(high - low) + low;

            Block block = world.getBlockAt(
                    spawn.getBlockX() + xRandom,
                    yRandom,
                    spawn.getBlockZ() + zRandom
            );

            if (conditions != null) {
                Block blockBelow = world.getBlockAt(
                        block.getLocation().getBlockX(),
                        block.getLocation().getBlockY() - 1,
                        block.getLocation().getBlockZ()
                );

                if (!conditions.contains(blockBelow.getType())) continue;
            }

            block.setType(Material.CHEST);
            Chest chest = (Chest) block.getState();

            generatedChests.add(block);

            Inventory inventory = chest.getBlockInventory();
            inventory.addItem(Iterables.toArray(items, ItemStack.class));

            i++;
        }
    }


}
