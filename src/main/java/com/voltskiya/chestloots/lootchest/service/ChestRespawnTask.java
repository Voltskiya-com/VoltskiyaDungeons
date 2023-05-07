package com.voltskiya.chestloots.lootchest.service;

import com.voltskiya.chestloots.VoltskiyaPlugin;
import com.voltskiya.chestloots.lootchest.LootChestModule;
import com.voltskiya.chestloots.lootchest.LootChestModuleConfig;
import com.voltskiya.chestloots.lootchest.entity.chest.ChestStorage;
import com.voltskiya.chestloots.lootchest.entity.chest.DChest;
import com.voltskiya.chestloots.lootchest.entity.group.ChestGroupStorage;
import com.voltskiya.chestloots.lootchest.entity.group.DChestGroup;
import com.voltskiya.chestloots.lootchest.util.ChestNBT;
import com.voltskiya.lib.timings.scheduler.VoltTask;
import com.voltskiya.lib.timings.scheduler.VoltTaskManager;
import io.ebean.DB;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Container;
import org.jetbrains.annotations.NotNull;

public class ChestRespawnTask implements Runnable {

    public static final int RESPAWN_INTERVAL = 60 * 20;

    private static void scheduleTask() {
        VoltTaskManager taskManager = LootChestModule.get().getTaskManager();
        VoltTask.cancelingAsyncTask(new ChestRespawnTask()).start(taskManager);
    }

    public static void start() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(VoltskiyaPlugin.get(), ChestRespawnTask::scheduleTask, 0, RESPAWN_INTERVAL);
    }

    private static boolean isTooClose(Location location) {
        int tooClose = LootChestModuleConfig.get().getPlayersTooCloseToRestock();
        return !location.getNearbyPlayers(tooClose).isEmpty();
    }

    @NotNull
    private List<DChestGroup> passTimeForGroups() {
        int playerCount = Bukkit.getOnlinePlayers().size();
        List<DChestGroup> groupsToRestock = new ArrayList<>();
        List<DChestGroup> lootedGroups = ChestGroupStorage.listLootedGroups();
        for (DChestGroup group : lootedGroups) {
            boolean shouldRestock = group.passTime(playerCount, RESPAWN_INTERVAL).shouldRestock();
            if (shouldRestock) groupsToRestock.add(group);
        }
        DB.saveAll(lootedGroups);
        return groupsToRestock;
    }

    private List<DChest> passTimeForLoners() {
        int playerCount = Bukkit.getOnlinePlayers().size();
        List<DChest> chestsToRestock = new ArrayList<>();
        List<DChest> chests = ChestStorage.listLootedLonerChests();
        for (DChest chest : chests) {
            boolean shouldRestock = chest.passTime(playerCount, RESPAWN_INTERVAL).shouldRestock();
            if (shouldRestock) chestsToRestock.add(chest);
        }
        DB.saveAll(chests);
        return chestsToRestock;
    }

    @Override
    public void run() {
        List<DChestGroup> groupsToRestock = passTimeForGroups();
        Instant restockedAt = Instant.now();
        restockGroups(groupsToRestock, restockedAt);
        List<DChest> chestsToRestock = passTimeForLoners();
        restockChests(chestsToRestock, restockedAt);
    }

    private void restockChests(List<DChest> chestsToRestock, Instant restockedAt) {
        VoltskiyaPlugin.get().scheduleSyncDelayedTask(() -> {
            for (DChest chest : chestsToRestock) {
                restock(chest, restockedAt);
                Bukkit.getScheduler().runTaskAsynchronously(VoltskiyaPlugin.get(), () -> chest.save());
            }
        });
    }

    private void restockGroups(List<DChestGroup> groupsToRestock, Instant restockedAt) {
        for (DChestGroup group : groupsToRestock) {
            List<DChest> chests = group.getChests();
            VoltskiyaPlugin.get().scheduleSyncDelayedTask(() -> {
                for (DChest chest : chests)
                    if (isTooClose(chest.getLocation())) return;
                for (DChest chest : chests) {
                    restock(chest, restockedAt);
                }
                Bukkit.getScheduler().runTaskAsynchronously(VoltskiyaPlugin.get(), () -> group.save());
            });
        }
    }

    private void restock(DChest dChest, Instant restockedAt) {
        Location location = dChest.getLocation();
        if (isTooClose(location)) return;
        if (!(location.getBlock().getState() instanceof Container chest)) {
            dChest.delete();
            return;
        }
        chest.getInventory().clear();
        ChestNBT.setLootTable(chest, dChest.getLootTable());
        dChest.setRestocked(restockedAt);
    }
}
