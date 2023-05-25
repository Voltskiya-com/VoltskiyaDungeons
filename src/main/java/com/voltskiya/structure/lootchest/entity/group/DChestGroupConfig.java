package com.voltskiya.structure.lootchest.entity.group;

import com.voltskiya.structure.lootchest.LootChestModuleConfig;
import com.voltskiya.structure.lootchest.entity.IChestGroupConfig;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public class DChestGroupConfig implements IChestGroupConfig {

    private static final Map<String, ChestGroupConfigProperty<DChestGroupConfig, Double>> configNumbers = Map.of(
        "fastRestockMin", new ChestGroupConfigProperty<>(
            DChestGroupConfig::getFastRestockMin,
            DChestGroupConfig::setFastRestockMin,
            LootChestModuleConfig.get()::verifyFastRestockMin),
        "slowRestockMin", new ChestGroupConfigProperty<>(
            DChestGroupConfig::getSlowRestockMin,
            DChestGroupConfig::setSlowRestockMin,
            LootChestModuleConfig.get()::verifySlowRestockMin));
    @Nullable
    protected Double fastRestockMin;
    @Nullable
    protected Double slowRestockMin;

    public DChestGroupConfig() {
    }

    private static LootChestModuleConfig getMainConfig() {
        return LootChestModuleConfig.get();
    }

    public static List<String> autoComplete() {
        return configNumbers.keySet().stream().toList();
    }

    public Double getFastRestockMin() {
        return fastRestockMin;
    }

    private void setFastRestockMin(Double fastRestockMin) {
        this.fastRestockMin = fastRestockMin;
    }

    public Double getSlowRestockMin() {
        return slowRestockMin;
    }

    private void setSlowRestockMin(Double slowRestockMin) {
        this.slowRestockMin = slowRestockMin;
    }

    public double verifyFastRestockMin() {
        if (fastRestockMin != null) return fastRestockMin;
        return getMainConfig().verifyFastRestockMin();
    }

    public double verifySlowRestockMin() {
        if (slowRestockMin != null) return slowRestockMin;
        return getMainConfig().verifySlowRestockMin();
    }

    @Override
    public int verifyPlayerCountAtFastRestock() {
        return getMainConfig().verifyPlayerCountAtFastRestock();
    }

    public boolean setConfig(String key, Double value) {
        ChestGroupConfigProperty<DChestGroupConfig, Double> prop = configNumbers.get(key);
        if (prop != null) {
            prop.set(this, value);
            return true;
        }
        return false;
    }

    public Double getConfig(String key) {
        ChestGroupConfigProperty<DChestGroupConfig, Double> prop = configNumbers.get(key);
        if (prop == null) return null;
        return prop.get(this);
    }

    public Double getConfigOrDefault(String key) {
        ChestGroupConfigProperty<DChestGroupConfig, Double> prop = configNumbers.get(key);
        if (prop == null) return null;
        return prop.getOrDefault(this);
    }
}