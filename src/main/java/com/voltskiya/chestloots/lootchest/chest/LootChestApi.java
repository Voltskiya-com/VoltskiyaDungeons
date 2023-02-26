package com.voltskiya.chestloots.lootchest.chest;

import com.voltskiya.chestloots.lootchest.world.DWorldId;
import com.voltskiya.chestloots.lootchest.world.WorldApi;
import io.ebean.DB;
import io.ebean.Query;
import org.bukkit.Location;

public class LootChestApi {

    public static void loot(Location location, String lootTable) {
        DWorldId world = WorldApi.getWorld(location.getWorld().getUID());
        DLootChest chest = findLootChest(location, world).findOne();
        if (chest == null) {
            DLootChest lootChest = new DLootChest(world, location, lootTable);
            DB.insert(lootChest);
        } else {
            chest.update(lootTable);
        }
    }

    public static void delete(Location location) {
        DWorldId world = WorldApi.getWorld(location.getWorld().getUID());
        findLootChest(location, world).delete();
    }

    private static Query<DLootChest> findLootChest(Location location, DWorldId world) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        String queryString = """
            where
                world = :world and
                x = :x and
                y = :y and
                z = :z
            """;
        Query<DLootChest> query = DB.createQuery(DLootChest.class, queryString);
        return query.setParameter("world", world).setParameter("x", x).setParameter("y", y).setParameter("z", z);
    }
}
