package com.voltskiya.structure.dungeon.wand;

import apple.mc.utilities.player.wand.WandType;
import com.voltskiya.structure.VoltskiyaPlugin;
import com.voltskiya.structure.dungeon.DungeonModule;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DungeonWandEntityListener implements Listener {

    public DungeonWandEntityListener() {
        VoltskiyaPlugin.get().registerEvents(this);
    }

    private static DungeonWand getPlayerWand(@NotNull Entity damager) {
        if (damager instanceof Player player) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType().isAir() || item.getItemMeta() == null) return null;
            WandType<DungeonWand> wandType = DungeonModule.get().dungeonWand();
            if (wandType.isItemWand(item)) {
                return wandType.getWand(player);
            }
        }
        return null;
    }

    @EventHandler
    public void onEntityClick(PlayerInteractAtEntityEvent event) {
        DungeonWand wand = getPlayerWand(event.getPlayer());
        if (wand != null) wand.onEntityClick(event.getRightClicked(), false);
    }

    @EventHandler
    public void onEntityClick(EntityDamageByEntityEvent event) {
        DungeonWand wand = getPlayerWand(event.getDamager());
        if (wand != null) {
            wand.onEntityClick(event.getEntity(), true);
            event.setCancelled(true);
        }
    }
}
