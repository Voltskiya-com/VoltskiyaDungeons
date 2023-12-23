package com.voltskiya.structure.lootchest.service;

import com.voltskiya.lib.timings.scheduler.VoltTask;
import com.voltskiya.lib.timings.scheduler.VoltTaskManager;
import com.voltskiya.structure.VoltskiyaPlugin;
import com.voltskiya.structure.database.DungeonDatabase;
import com.voltskiya.structure.dungeon.entity.spawn.DungeonSpawnerStorage;
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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import voltskiya.apple.utilities.minecraft.player.PlayerUtils;

public class ChestRespawnTask implements Runnable {

    public static final int RESPAWN_INTERVAL = 60 * 20;

    private static void scheduleTask() {
        VoltTaskManager taskManager = LootChestModule.get().getTaskManager();
        VoltTask.cancelingAsyncTask(new ChestRespawnTask()).start(taskManager);
    }

    public static void start() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(VoltskiyaPlugin.get(), ChestRespawnTask::scheduleTask, 0, RESPAWN_INTERVAL);
    }

    private static boolean isTooClose(Location location, List<Location> players) {
        int tooClose = LootChestModuleConfig.get().playersTooCloseToRestock();
        return players.stream().anyMatch(p -> isPlayerTooClose(location, tooClose, p));
    }

    private static boolean isPlayerTooClose(Location location, int tooClose, Location p) {
        return p.getWorld().getUID().equals(location.getWorld().getUID()) && p.distance(location) > tooClose;
    }


    public static void restock(DChest dChest, Instant restockedAt, boolean save) {
        Location location = dChest.getLocation();
        if (!(location.getBlock().getState() instanceof Container chest)) {
            dChest.delete();
            return;
        }
        chest.getInventory().clear();
        ChestNBT.setLootTable(chest, dChest.getLootTable());
        if (save) {
            VoltskiyaPlugin.get().runTaskAsync(() -> {
                dChest.setRestocked(restockedAt);
                dChest.save();
            });
        }
    }

    public static void restockGroup(Instant restockedAt, DChestGroup group) {
        for (DChest chest : group.getChests()) {
            restock(chest, restockedAt, false);
        }
        DungeonSpawnerStorage.summon(group.getSpawner());
        LootChestModule.get().logger().info("Restocked: " + group.getName());
        VoltskiyaPlugin.get().runTaskAsync(() -> {
            group.setRestocked(restockedAt);
            group.save();
        });
    }

    @NotNull
    private List<DChestGroup> passTimeForGroups(int timeToPass) {
        int playerCount = Bukkit.getOnlinePlayers().size();
        List<DChestGroup> groupsToRestock = new ArrayList<>();
        List<DChestGroup> lootedGroups = ChestGroupStorage.listLootedGroups();
        for (DChestGroup group : lootedGroups) {
            boolean shouldRestock = group.passTime(playerCount, timeToPass).shouldRestock();
            if (shouldRestock) groupsToRestock.add(group);
        }
        DungeonDatabase.get().getDB().saveAll(lootedGroups);
        return groupsToRestock;
    }

    private List<DChest> passTimeForLoners(int timeToPass) {
        int playerCount = Bukkit.getOnlinePlayers().size();
        List<DChest> chestsToRestock = new ArrayList<>();
        List<DChest> chests = ChestStorage.listLootedLonerChests();
        for (DChest chest : chests) {
            boolean shouldRestock = chest.passTime(playerCount, timeToPass).shouldRestock();
            if (shouldRestock) chestsToRestock.add(chest);
        }
        DungeonDatabase.db().saveAll(chests);
        return chestsToRestock;
    }

    @Override
    public void run() {
        passTime(RESPAWN_INTERVAL);
    }

    public void passTime(int timeToPass) {
        List<DChestGroup> groupsToRestock = passTimeForGroups(timeToPass);
        List<DChest> chestsToRestock = passTimeForLoners(timeToPass);
        Instant restockedAt = Instant.now();
        VoltskiyaPlugin.get().scheduleSyncDelayedTask(() -> {
            List<Location> players = Bukkit.getOnlinePlayers().stream()
                .filter(PlayerUtils::isSurvival)
                .map(Player::getLocation)
                .toList();
            restockGroups(groupsToRestock, restockedAt, List.copyOf(players));
            restockChests(chestsToRestock, restockedAt, players);
        });
    }

    private void restockChests(List<DChest> chestsToRestock, Instant restockedAt, List<Location> players) {
        VoltskiyaPlugin.get().runTaskAsync(() -> {
            for (DChest chest : chestsToRestock) {
                if (isTooClose(chest.getLocation(), players)) continue;
                VoltskiyaPlugin.get().scheduleSyncDelayedTask(() -> restock(chest, restockedAt, true));
            }
        });
    }

    private void restockGroups(List<DChestGroup> groupsToRestock, Instant restockedAt, List<Location> players) {
        VoltskiyaPlugin.get().runTaskAsync(() -> {
            for (DChestGroup group : groupsToRestock) {
                for (DChest chest : group.getChests())
                    if (isTooClose(chest.getLocation(), players)) break;
                VoltskiyaPlugin.get().scheduleSyncDelayedTask(() -> restockGroup(restockedAt, group));
            }
        });
    }
}
