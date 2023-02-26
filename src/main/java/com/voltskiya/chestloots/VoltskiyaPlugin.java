package com.voltskiya.chestloots;

import com.voltskiya.chestloots.lootchest.LootChestModule;
import com.voltskiya.lib.AbstractModule;
import com.voltskiya.lib.AbstractVoltPlugin;
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
    public Collection<AbstractModule> getModules() {
        return List.of(new LootChestModule());
    }
}
