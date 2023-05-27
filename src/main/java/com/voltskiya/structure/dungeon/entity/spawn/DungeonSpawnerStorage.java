package com.voltskiya.structure.dungeon.entity.spawn;

import com.voltskiya.structure.dungeon.entity.layout.DDungeonLayoutMob;
import com.voltskiya.structure.dungeon.entity.schematic.DDungeonMobWeight;
import com.voltskiya.structure.dungeon.entity.schematic.DDungeonSchemMob;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class DungeonSpawnerStorage {

    private static final String TAG_PREFIX = "dungeon.spawner.";
    private static final Random random = new Random();

    public static void summon(DDungeonSpawner spawner) {
        Location center = spawner.getCenter();
        if (center == null) throw new IllegalStateException(spawner.getName() + "'s center is null");
        String tag = getTag(spawner);
        center.getWorld().getEntities()
            .stream()
            .filter(entity -> entity.getScoreboardTags().contains(tag))
            .forEach(Entity::remove);
        spawner.getDungeon().getLayout().getMobs().forEach((mob) -> summonSpawner(mob, center, tag));
    }

    private static void summonSpawner(DDungeonLayoutMob layoutMob, Location center, String tag) {
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
            if (chance <= 0) continue;
            Location location = layoutMob.getLocation(center);
            if (location != null)
                weightMob.getEntity().spawn(location, e -> e.addScoreboardTag(tag));
            return;
        }
    }

    private static String getTag(DDungeonSpawner spawner) {
        return TAG_PREFIX + spawner.getDungeon().getName() + "." + spawner.getName();
    }
}
