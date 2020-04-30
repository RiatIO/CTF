package io.riat.CTF.Events;

import io.riat.CTF.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.HashMap;

import static io.riat.CTF.Utils.CTF_TAG;

public class CraftItem implements Listener {
    private HashMap<String, Material> banners = Utils.getTeamMaterialMap();

    @EventHandler
    public void onCraftItem(PrepareItemCraftEvent e) {
        Recipe recipe = e.getRecipe();

        if (recipe != null) {
            Material itemType = recipe.getResult().getType();

            if (banners.containsValue(itemType) || itemType == Material.WHITE_STAINED_GLASS_PANE) {
                e.getInventory().setResult(new ItemStack(Material.AIR));

                for (HumanEntity he : e.getViewers()) {
                    if (he instanceof Player) {
                        ((Player)he).sendMessage(CTF_TAG + "You cheeky lad! You cannot craft this!");
                    }
                }
            }
        }
    }
}
