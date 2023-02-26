package com.voltskiya.chestloots.lootchest.base;

import com.voltskiya.chestloots.VoltskiyaPlugin;
import com.voltskiya.chestloots.lootchest.LootChestModule;
import com.voltskiya.chestloots.lootchest.chest.DLootChest;
import com.voltskiya.chestloots.lootchest.world.DWorldId;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import io.ebean.datasource.DataSourceConfig;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ChestDatabase {

    public static void load() {
        DataSourceConfig dataSourceConfig = configureDataSource(ChestDatabaseConfig.get());

        ClassLoader originalCL = Thread.currentThread().getContextClassLoader();
        ClassLoader cl = VoltskiyaPlugin.class.getClassLoader();

        DatabaseConfig dbConfig = configureDatabase(dataSourceConfig);
        Thread.currentThread().setContextClassLoader(cl);
        DatabaseFactory.createWithContextClassLoader(dbConfig, cl);

        Thread.currentThread().setContextClassLoader(originalCL);
        LootChestModule.get().logger().info("Successfully created database");

    }

    @NotNull
    private static DatabaseConfig configureDatabase(DataSourceConfig dataSourceConfig) {
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setDataSourceConfig(dataSourceConfig);
        dbConfig.setDdlGenerate(true);
        dbConfig.setDdlRun(ChestDatabaseConfig.get().getDDLRun());

        dbConfig.addAll(List.of(DLootChest.class, DWorldId.class));
        return dbConfig;
    }

    @NotNull
    private static DataSourceConfig configureDataSource(ChestDatabaseConfig loadedConfig) {
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setUrl(loadedConfig.getUrl());
        dataSourceConfig.setUsername("whyisthisneeeded");
        dataSourceConfig.setPassword("whyisthisneeeded");
        dataSourceConfig.setPlatform("sqlite");
        dataSourceConfig.setAutoCommit(true);
        return dataSourceConfig;
    }
}
