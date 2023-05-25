package com.voltskiya.structure.lootchest.entity.world;

import com.google.common.collect.HashBiMap;
import com.voltskiya.structure.lootchest.entity.world.query.QDWorldId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldStorage {

    private static final Map<UUID, DWorldId> worlds = HashBiMap.create();

    public static void load() {
        List<DWorldId> queried = new QDWorldId().findList();
        for (DWorldId world : queried) {
            worlds.put(world.worldUUID, world);
        }
        for (World world : Bukkit.getWorlds()) {
            getWorld(world.getUID());
        }
    }

    public static DWorldId getWorld(UUID worldUUID) {
        DWorldId world;
        synchronized (worlds) {
            world = worlds.get(worldUUID);
        }
        if (world != null) return world;
        world = new DWorldId(worldUUID);
        world.save();
        synchronized (worlds) {
            worlds.put(worldUUID, world);
        }
        return world;
    }
}
