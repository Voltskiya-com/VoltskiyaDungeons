package com.voltskiya.chestloots.lootchest;

import com.voltskiya.chestloots.lootchest.base.IChestGroupConfig;

public class LootChestModuleConfig implements IChestGroupConfig {

    private static LootChestModuleConfig instance;
    protected int playerCountAtFastRestock = 10;
    protected double defaultFastRestockMin = 20;
    protected double defaultSlowRestockMin = 60;
    protected int playersTooCloseToRestock = 32;

    public LootChestModuleConfig() {
        instance = this;
    }

    public static LootChestModuleConfig get() {
        return instance;
    }

    @Override
    public double verifyFastRestockMin() {
        return defaultFastRestockMin;
    }

    @Override
    public double verifySlowRestockMin() {
        return defaultSlowRestockMin;
    }

    @Override
    public int verifyPlayerCountAtFastRestock() {
        return playerCountAtFastRestock;
    }

    public int playersTooCloseToRestock() {
        return playersTooCloseToRestock;
    }
}
