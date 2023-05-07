package com.voltskiya.chestloots.lootchest.service;

import com.voltskiya.chestloots.VoltskiyaPlugin;
import com.voltskiya.chestloots.lootchest.entity.chest.ChestStorage;
import com.voltskiya.chestloots.lootchest.util.ChestNBT;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.world.LootGenerateEvent;

public class ChestLootListener implements Listener {

    public ChestLootListener() {
        VoltskiyaPlugin.get().registerEvents(this);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onLoot(LootGenerateEvent event) {
        String lootTable = event.getLootTable().getKey().asString();
        Location location = event.getLootContext().getLocation();
        Bukkit.getScheduler().runTaskAsynchronously(VoltskiyaPlugin.get(), () -> ChestStorage.loot(location, lootTable));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event) {
        BlockState blockState = event.getBlock().getState();
        if (!(blockState instanceof Container chest)) return;

        Block block = chest.getBlock();
        String lootTable = ChestNBT.getLootTable(blockState);
        if (lootTable == null) return;

        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            Location location = block.getLocation();
            Bukkit.getScheduler().runTask(VoltskiyaPlugin.get(), () -> ChestStorage.delete(location));
        } else {
            event.setCancelled(true);
        }
    }

}
