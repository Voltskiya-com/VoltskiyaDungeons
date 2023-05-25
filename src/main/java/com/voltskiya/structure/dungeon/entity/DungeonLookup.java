package com.voltskiya.structure.dungeon.entity;

import com.voltskiya.structure.dungeon.entity.query.QDDungeon;
import com.voltskiya.structure.dungeon.entity.schematic.DDungeonSchemMob;
import com.voltskiya.structure.dungeon.entity.schematic.query.QDDungeonSchemMob;
import com.voltskiya.structure.dungeon.entity.spawn.query.QDDungeonSpawner;
import java.util.Collection;

public class DungeonLookup {


    public static Collection<String> dungeonNames() {
        return new QDDungeon()
            .select(QDDungeon.alias().name)
            .findSingleAttributeList();
    }

    public static Collection<String> findSpawner(String dungeon) {
        return new QDDungeonSpawner()
            .select(QDDungeonSpawner.alias().name)
            .where().dungeon.name.eq(dungeon)
            .findSingleAttributeList();
    }

    public static DDungeon findDungeon(String dungeon) {
        return new QDDungeon().where().name.ieq(dungeon).findOne();
    }

    public static DDungeonSchemMob findMob(DDungeon dungeon, String mob) {
        return new QDDungeonSchemMob()
            .where().and()
            .dungeon.eq(dungeon)
            .name.ieq(mob).findOne();
    }
}
