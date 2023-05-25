package com.voltskiya.structure.lootchest;

import com.voltskiya.lib.AbstractModule;
import com.voltskiya.lib.configs.factory.AppleConfigLike;
import com.voltskiya.structure.lootchest.command.ChestGroupCommand;
import com.voltskiya.structure.lootchest.entity.world.WorldStorage;
import com.voltskiya.structure.lootchest.service.ChestLootListener;
import com.voltskiya.structure.lootchest.service.ChestRespawnTask;
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
        WorldStorage.load();
        new ChestLootListener();
        ChestRespawnTask.start();
        new ChestGroupCommand();
        LootChestModuleConfig.get().validate();
    }

    @Override
    public List<AppleConfigLike> getConfigs() {
        return List.of(configJson(LootChestModuleConfig.class, "LootChest.config"));
    }

    @Override
    public String getName() {
        return "LootChest";
    }
}
