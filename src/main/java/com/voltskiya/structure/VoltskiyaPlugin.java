package com.voltskiya.structure;

import com.voltskiya.lib.AbstractModule;
import com.voltskiya.lib.AbstractVoltPlugin;
import com.voltskiya.structure.database.DungeonDatabase;
import com.voltskiya.structure.dungeon.DungeonModule;
import com.voltskiya.structure.lootchest.LootChestModule;
import java.util.Collection;
import java.util.List;

public class VoltskiyaPlugin extends AbstractVoltPlugin {

    private static VoltskiyaPlugin instance;

    public VoltskiyaPlugin() {
        instance = this;
    }

    public static VoltskiyaPlugin get() {
        return instance;
    }

    @Override
    public void onEnablePre() {
        new DungeonDatabase();
    }

    @Override
    public Collection<AbstractModule> getModules() {
        return List.of(new LootChestModule(), new DungeonModule());
    }
}
