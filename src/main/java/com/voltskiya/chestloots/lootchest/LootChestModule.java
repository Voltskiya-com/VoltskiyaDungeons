package com.voltskiya.chestloots.lootchest;

import com.voltskiya.chestloots.lootchest.base.ChestDatabase;
import com.voltskiya.chestloots.lootchest.base.ChestDatabaseConfig;
import com.voltskiya.chestloots.lootchest.command.ChestGroupCommand;
import com.voltskiya.chestloots.lootchest.entity.world.WorldStorage;
import com.voltskiya.chestloots.lootchest.service.ChestLootListener;
import com.voltskiya.chestloots.lootchest.service.ChestRespawnTask;
import com.voltskiya.lib.AbstractModule;
import com.voltskiya.lib.configs.factory.AppleConfigLike;
import java.util.List;

public class LootChestModule extends AbstractModule {

    private static LootChestModule instance;

    public LootChestModule() {
        instance = this;
    }

    public static LootChestModule get() {
        return instance;
    }

    @Override
    public void enable() {
        ChestDatabase.load();
        WorldStorage.load();
        new ChestLootListener();
        ChestRespawnTask.start();
        new ChestGroupCommand();
        LootChestModuleConfig.get().validate();
    }

    @Override
    public List<AppleConfigLike> getConfigs() {
        return List.of(configJson(LootChestModuleConfig.class, "LootChest.config"),
            configJson(ChestDatabaseConfig.class, "Database.config"));
    }

    @Override
    public String getName() {
        return "LootChest";
    }
}
