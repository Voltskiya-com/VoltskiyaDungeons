package com.voltskiya.structure.lootchest.service;

import com.voltskiya.lib.timings.scheduler.VoltTask;
import com.voltskiya.lib.timings.scheduler.VoltTaskManager;
import com.voltskiya.structure.VoltskiyaPlugin;
import com.voltskiya.structure.database.DungeonDatabase;
import com.voltskiya.structure.lootchest.LootChestModule;
import com.voltskiya.structure.lootchest.LootChestModuleConfig;
import com.voltskiya.structure.lootchest.entity.chest.ChestStorage;
import com.voltskiya.structure.lootchest.entity.chest.DChest;
import com.voltskiya.structure.lootchest.entity.group.ChestGroupStorage;
import com.voltskiya.structure.lootchest.entity.group.DChestGroup;
import com.voltskiya.structure.lootchest.util.ChestNBT;
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
        int tooClose = LootChestModuleConfig.get().playersTooCloseToRestock();
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
        DungeonDatabase.get().getDB().saveAll(lootedGroups);
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
        DungeonDatabase.db().saveAll(chests);
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
        VoltskiyaPlugin.get().scheduleSyncDelayedTask(() -> {
            for (DChestGroup group : groupsToRestock) {
                List<DChest> chests = group.getChests();
                for (DChest chest : chests)
                    if (isTooClose(chest.getLocation())) return;
                for (DChest chest : chests) {
                    restock(chest, restockedAt);
                }
                group.save();
                Bukkit.getScheduler().runTaskAsynchronously(VoltskiyaPlugin.get(), () -> group.save());
            }
        });
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
