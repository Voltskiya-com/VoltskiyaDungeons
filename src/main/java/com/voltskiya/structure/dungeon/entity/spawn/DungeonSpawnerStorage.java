package com.voltskiya.structure.dungeon.entity.spawn;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.voltskiya.structure.VoltskiyaPlugin;
import com.voltskiya.structure.database.DungeonDatabase;
import com.voltskiya.structure.dungeon.entity.layout.DDungeonLayoutMob;
import com.voltskiya.structure.dungeon.entity.schematic.DDungeonMobWeight;
import com.voltskiya.structure.dungeon.entity.schematic.DDungeonSchemMob;
import io.ebean.Model;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import voltskiya.mob.system.storage.mob.clone.DMobClone;
import voltskiya.mob.system.storage.mob.clone.MobCloneStorage;

public class DungeonSpawnerStorage {


    private static final String TAG_PREFIX = "dungeon.spawner.";
    private static final Random random = new Random();

    public static void summon(@Nullable DDungeonSpawner spawner) {
        if (spawner == null) return;
        Location center = spawner.getCenter();
        if (center == null) throw new IllegalStateException(spawner.getName() + "'s center is null");
        String tag = getTag(spawner);
        kill(spawner).addListener(() -> {
            int delay = 0;
            for (DDungeonLayoutMob mob : spawner.getDungeon().getLayout().getMobs()) {
                VoltskiyaPlugin.get().scheduleSyncDelayedTask(() -> {
                    summonMob(mob, center, tag);
                }, delay / 20);
                delay++;
            }
        }, Executors.newSingleThreadExecutor());
    }


    private static void summonMob(DDungeonLayoutMob layoutMob, Location center, String tag) {
        DDungeonSchemMob schemMob = layoutMob.getSchemMob();
        int noSpawnWeight = schemMob.getNoSpawnWeight();
        double fullWeight = noSpawnWeight;
        for (DDungeonMobWeight weightMob : schemMob.getMobs()) {
            fullWeight += weightMob.getWeight();
        }
        double chance;
        synchronized (random) {
            chance = random.nextDouble();
        }
        chance -= noSpawnWeight / fullWeight;
        if (chance <= 0) return;
        for (DDungeonMobWeight weightMob : schemMob.getMobs()) {
            chance -= weightMob.getWeight() / fullWeight;
            if (chance > 0) continue;
            Location location = layoutMob.getLocation(center);
            if (location != null) {
                weightMob.getEntity().spawn(location, e -> {
                    MobCloneStorage.addCloneTags(e, List.of(tag));
                });
                location.getChunk().load();
            }
            return;
        }
    }

    private static String getTag(DDungeonSpawner spawner) {
        return TAG_PREFIX + spawner.getDungeon().getName() + "." + spawner.getName();
    }

    public static DDungeonSpawner findSpawner(UUID spawner) {
        return DungeonDatabase.db().find(DDungeonSpawner.class, spawner);
    }

    public static ListenableFuture<Void> kill(DDungeonSpawner spawner) {
        if (spawner == null) return Futures.immediateVoidFuture();
        Location center = spawner.getCenter();
        if (center == null) throw new IllegalStateException(spawner.getName() + "'s center is null");
        String tag = getTag(spawner);
        String fullTag = MobCloneStorage.getCloneTag(tag);
        center.getWorld().getEntities()
            .stream()
            .filter(entity -> entity.getScoreboardTags().contains(fullTag))
            .forEach(Entity::remove);
        return Futures.submit(() -> {
            killWithTag(tag);
        }, Executors.newSingleThreadExecutor());
    }

    private static synchronized void killWithTag(String tag) {
        List<DMobClone> mobs = MobCloneStorage.queryMobs(List.of(tag));
        mobs.forEach(Model::delete);
    }
}
