package com.voltskiya.chestloots.lootchest.base;


import com.voltskiya.chestloots.lootchest.LootChestModule;

public class ChestDatabaseConfig {

    private static ChestDatabaseConfig instance;


    public ChestDatabaseConfig() {
        instance = this;
    }

    public static ChestDatabaseConfig get() {
        return instance;
    }

    public String getUrl() {
        return "jdbc:sqlite:" + LootChestModule.get().getFile("LootChest.sqlite").getAbsolutePath();
    }

}
