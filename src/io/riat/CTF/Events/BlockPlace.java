package io.riat.CTF.Events;

import io.riat.CTF.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;

public class BlockPlace implements Listener {
    private HashMap<String, Material> banners = Utils.getTeamMaterialMap();

    private Plugin plugin;

    public BlockPlace(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlaceEvent(final BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Block b = e.getBlock();

        // Check if the placed block is a banner
        if (banners.containsValue(b.getType())) {

            // Check the the surrouding blocks is air (like 5x5)

            Location location = b.getLocation();

            for (int y = 1; y < 5; y++) {
                for (int x = 1; x < 5; x++) {
                    for (int z = 1; z < 5; z++) {
                        location.add(x, y, z);

                        //System.out.println(location.getBlock());

                        // Check if air
                        if (location.getBlock().getType() != Material.AIR) {
                            e.setCancelled(true);

                            player.sendMessage("[CTF] The flag should be placed with atleast 5 blocks of air");

                            return;
                        }

                        location.subtract(x, y, z);
                    }
                }
            }

        }

        if (b.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            e.setCancelled(true);
            player.sendMessage("[CTF] Trying to be smart, ey? That's not allowed.");
        }
    }
}
