package io.riat.CTF.Events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class EntityExplode implements Listener {
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList().toArray(new Block[event.blockList().size()])) {
            if (block.getType() == Material.WHITE_STAINED_GLASS_PANE) {
                event.setCancelled(true);
            }
        }
    }
}
