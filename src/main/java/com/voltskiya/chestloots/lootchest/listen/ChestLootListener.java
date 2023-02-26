package com.voltskiya.chestloots.lootchest.listen;

import com.voltskiya.chestloots.VoltskiyaPlugin;
import com.voltskiya.chestloots.lootchest.chest.LootChestApi;
import de.tr7zw.nbtapi.NBTBlock;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.BlockInventoryHolder;

public class ChestLootListener implements Listener {

    public ChestLootListener() {
        VoltskiyaPlugin.get().registerEvents(this);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onLoot(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof BlockInventoryHolder holder) {
            Block block = holder.getBlock();
            String lootTable = new NBTBlock(block).getData().getString("LootTable");
            if (lootTable == null) return;
            Runnable run = () -> LootChestApi.loot(block.getLocation(), lootTable);
            Bukkit.getScheduler().runTask(VoltskiyaPlugin.get(), run);
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!(event.getBlock() instanceof Container chest)) return;

        Block block = chest.getBlock();
        String lootTable = new NBTBlock(block).getData().getString("LootTable");
        if (lootTable == null) return;

        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            Bukkit.getScheduler().runTask(VoltskiyaPlugin.get(), () -> LootChestApi.delete(block.getLocation()));
        } else {
            event.setCancelled(true);
        }
    }
}
