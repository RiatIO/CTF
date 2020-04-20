package io.riat.CTF.Events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BlockBreak implements Listener {

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent e) {

        Player player = e.getPlayer();
        Block b = e.getBlock();

        if (b.getType() == Material.BLACK_BANNER) {
            Collection<ItemStack> items = e.getBlock().getDrops();
            player.sendMessage(ChatColor.RED + "[CTF] Pick up the flag, and RUN!");

            for (ItemStack it : items) {
                World w = e.getPlayer().getWorld();
                w.dropItemNaturally(e.getBlock().getLocation().add(0.5,  0, 0.5), it);

                for (int i = 0; i < 10; i++) {
                    w.spawn(e.getBlock().getLocation().add(i, i, 1), TNTPrimed.class).setFuseTicks(200);
                    w.spawn(e.getBlock().getLocation().add(1, i, i), TNTPrimed.class).setFuseTicks(200);
                    w.spawn(e.getBlock().getLocation().add(-1 * i, i, 1), TNTPrimed.class).setFuseTicks(200);
                    w.spawn(e.getBlock().getLocation().add(1, i, -1 * i), TNTPrimed.class).setFuseTicks(200);
                }
                Bukkit.broadcastMessage("[CTF] - Black base self-destructing in 10 seconds...");
            }
        }

        if (b.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            e.setCancelled(true);
            player.sendMessage("[CTF] You can't break the dome, dude.");
        }



        //getLogger().info("Broke" + theBlock.toString());

    }

}
