package com.voltskiya.chestloots.lootchest.entity.chest;

import com.voltskiya.chestloots.lootchest.entity.chest.query.QDChest;
import com.voltskiya.chestloots.lootchest.entity.world.DWorldId;
import com.voltskiya.chestloots.lootchest.entity.world.WorldStorage;
import java.util.List;
import org.bukkit.Location;

public class ChestStorage {

    public static void loot(Location location, String lootTable) {
        DChest chest = findChestAt(location);
        if (chest == null)
            chest = new DChest(location, lootTable);
        chest.setLooted(lootTable);
        chest.save();
    }

    public static void delete(Location location) {
        DChest chest = findChestAt(location);
        if (chest != null) chest.delete();
    }

    public static DChest findChestAt(Location location) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        DWorldId world = WorldStorage.getWorld(location.getWorld().getUID());
        return new QDChest()
            .where().and()
            .world.eq(world)
            .x.eq(x)
            .y.eq(y)
            .z.eq(z).findOne();
    }

    public static List<DChest> listLootedLonerChests() {
        return new QDChest()
            .where()
            .and()
            .status.eq(DChestLootStatus.LOOTED)
            .group.isNull()
            .findList();
    }
}
