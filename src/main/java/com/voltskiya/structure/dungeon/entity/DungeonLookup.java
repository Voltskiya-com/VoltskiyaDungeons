package com.voltskiya.structure.dungeon.entity;

import com.voltskiya.structure.database.DungeonDatabase;
import com.voltskiya.structure.dungeon.entity.query.QDDungeon;
import com.voltskiya.structure.dungeon.entity.schematic.DDungeonSchemMob;
import com.voltskiya.structure.dungeon.entity.schematic.query.QDDungeonSchemMob;
import java.util.Collection;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public class DungeonLookup {


    public static Collection<String> dungeonNames() {
        return new QDDungeon()
            .select(QDDungeon.alias().name)
            .findSingleAttributeList();
    }

    @Nullable
    public static DDungeon findDungeon(String dungeon) {
        return new QDDungeon().where().name.ieq(dungeon).findOne();
    }

    public static DDungeonSchemMob findMob(DDungeon dungeon, String mob) {
        return new QDDungeonSchemMob()
            .where().and()
            .dungeon.eq(dungeon)
            .name.ieq(mob).findOne();
    }

    public static DDungeon findDungeon(UUID dungeon) {
        return DungeonDatabase.get().getDB().find(DDungeon.class, dungeon);
    }
}
