package com.voltskiya.structure.dungeon.entity.layout;

import com.voltskiya.structure.dungeon.entity.layout.query.QDDungeonLayoutMob;
import org.bukkit.Location;

public class DungeonLayoutStorage {

    private static final double SAME_MOB_DISTANCE = .125;

    public static int deleteMobAt(DDungeonLayout layout, Location location) {
        return queryMobAt(layout, location).delete();
    }

    public static QDDungeonLayoutMob queryMobAt(DDungeonLayout layout, Location location) {
        Location center = layout.getCenter();
        double x = location.getX() - center.getX();
        double y = location.getY() - center.getY();
        double z = location.getZ() - center.getZ();

        return new QDDungeonLayoutMob()
            .where()
            .layout.eq(layout)
            .relative.x.between(x - SAME_MOB_DISTANCE, x + SAME_MOB_DISTANCE)
            .relative.y.between(y - SAME_MOB_DISTANCE, y + SAME_MOB_DISTANCE)
            .relative.z.between(z - SAME_MOB_DISTANCE, z + SAME_MOB_DISTANCE);
    }

    public static DDungeonLayoutMob findMobAt(DDungeonLayout layout, Location relative) {
        return queryMobAt(layout, relative).findOne();
    }
}
