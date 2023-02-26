package com.voltskiya.chestloots.lootchest;

import com.voltskiya.chestloots.lootchest.base.ChestDatabase;
import com.voltskiya.chestloots.lootchest.base.ChestDatabaseConfig;
import com.voltskiya.chestloots.lootchest.listen.ChestLootListener;
import com.voltskiya.chestloots.lootchest.world.WorldApi;
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
        WorldApi.load();
        new ChestLootListener();
    }

    @Override
    public List<AppleConfigLike> getConfigs() {
        return List.of(configJson(ChestDatabaseConfig.class, "Database.config", "Config"));
    }

    @Override
    public String getName() {
        return "LootChest";
    }
}
