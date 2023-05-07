package com.voltskiya.chestloots.lootchest.entity.group;

import com.voltskiya.chestloots.lootchest.LootChestModuleConfig;
import com.voltskiya.chestloots.lootchest.base.IChestGroupConfig;
import org.jetbrains.annotations.Nullable;

public class DChestGroupConfig implements IChestGroupConfig {

    @Nullable
    protected Double defaultFastRestockMin;
    @Nullable
    protected Double defaultSlowRestockMin;

    public DChestGroupConfig() {
        this.defaultFastRestockMin = null;
        this.defaultSlowRestockMin = null;
    }

    private static LootChestModuleConfig getMainConfig() {
        return LootChestModuleConfig.get();
    }

    public double getFastRestockMin() {
        if (defaultFastRestockMin != null) return defaultFastRestockMin;
        return getMainConfig().getFastRestockMin();
    }

    public double getSlowRestockMin() {
        if (defaultSlowRestockMin != null) return defaultSlowRestockMin;
        return getMainConfig().getFastRestockMin();
    }

    @Override
    public int getPlayerCountAtFastRestock() {
        return getMainConfig().getPlayerCountAtFastRestock();
    }
}