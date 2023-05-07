package com.voltskiya.chestloots.lootchest.entity.group;

import com.voltskiya.chestloots.lootchest.LootChestModuleConfig;
import com.voltskiya.chestloots.lootchest.base.IChestGroupConfig;
import org.jetbrains.annotations.Nullable;

public class DChestGroupConfig implements IChestGroupConfig {

    @Nullable
    protected Double fastRestockMin;
    @Nullable
    protected Double slowRestockMin;

    public DChestGroupConfig() {
    }

    private static LootChestModuleConfig getMainConfig() {
        return LootChestModuleConfig.get();
    }

    public double getFastRestockMin() {
        if (fastRestockMin != null) return fastRestockMin;
        return getMainConfig().getFastRestockMin();
    }

    public double getSlowRestockMin() {
        if (slowRestockMin != null) return slowRestockMin;
        return getMainConfig().getFastRestockMin();
    }

    @Override
    public int playerCountAtFastRestock() {
        return getMainConfig().playerCountAtFastRestock();
    }
}