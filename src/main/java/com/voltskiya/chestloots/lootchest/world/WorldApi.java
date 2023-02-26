package com.voltskiya.chestloots.lootchest.world;

import com.google.common.collect.HashBiMap;
import io.ebean.DB;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WorldApi {

    private static final Map<UUID, DWorldId> worlds = HashBiMap.create();

    public static void load() {
        List<DWorldId> queried = DB.find(DWorldId.class).findList();
        for (DWorldId world : queried) {
            worlds.put(world.worldUUID, world);
        }
    }

    public static DWorldId getWorld(UUID worldUUID) {
        DWorldId world;
        synchronized (worlds) {
            world = worlds.get(worldUUID);
        }
        if (world != null) return world;
        world = new DWorldId(worldUUID);
        DB.insert(world);
        synchronized (worlds) {
            worlds.put(worldUUID, world);
        }
        return world;
    }
}
