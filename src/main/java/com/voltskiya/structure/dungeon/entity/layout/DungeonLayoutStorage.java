package com.voltskiya.structure.dungeon.entity.layout;

import com.voltskiya.structure.dungeon.entity.layout.query.QDDungeonLayoutMob;
import org.bukkit.Location;

public class DungeonLayoutStorage {

    public static int deleteMobAt(DDungeonLayout layout, Location location) {
        Location center = layout.getCenter();
        double x = location.getX() - center.getX();
        double y = location.getY() - center.getY();
        double z = location.getZ() - center.getZ();

        return new QDDungeonLayoutMob()
            .where()
            .layout.eq(layout)
            .relative.x.eq(x)
            .relative.y.eq(y)
            .relative.z.eq(z)
            .delete();
    }
}
