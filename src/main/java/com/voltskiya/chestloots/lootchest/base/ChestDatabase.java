package com.voltskiya.chestloots.lootchest.base;

import com.voltskiya.chestloots.VoltskiyaPlugin;
import com.voltskiya.chestloots.lootchest.entity.chest.ChestStorage;
import com.voltskiya.chestloots.lootchest.entity.chest.DChest;
import com.voltskiya.chestloots.lootchest.entity.group.ChestGroupStorage;
import com.voltskiya.chestloots.lootchest.entity.group.DChestGroup;
import com.voltskiya.chestloots.lootchest.entity.world.DWorldId;
import com.voltskiya.chestloots.lootchest.entity.world.WorldStorage;
import com.voltskiya.chestloots.lootchest.service.ChestRespawnTask;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import io.ebean.datasource.DataSourceConfig;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ChestDatabase {

    public static void load() {
        DataSourceConfig dataSourceConfig = configureDataSource();
        DatabaseConfig dbConfig = configureDatabase(dataSourceConfig);

        // We should use the classloader that loaded this plugin
        // because this plugin has our ebean dependencies
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader pluginClassLoader = VoltskiyaPlugin.class.getClassLoader();

        // create the DatabaseFactory with the classloader containing ebean dependencies
        DatabaseFactory.createWithContextClassLoader(dbConfig, pluginClassLoader);

        // Set the current thread's contextClassLoader to the classLoader with the ebean dependencies
        // This allows the class to initialize itself with access to the required class dependencies
        Thread.currentThread().setContextClassLoader(pluginClassLoader);

        // invoke the static initialization of every class that contains a querybean.
        // Note that any method in the class will initialize the class.
        for (Class<?> clazz : getQueryBeans()) {
            try {
                clazz.getConstructor().newInstance();
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        // Restore the contextClassLoader to what it was originally
        Thread.currentThread().setContextClassLoader(originalClassLoader);

        VoltskiyaPlugin.get().getLogger().info("Successfully created database");

    }

    @NotNull
    private static List<Class<?>> getQueryBeans() {
        return List.of(ChestStorage.class, ChestRespawnTask.class, ChestGroupStorage.class, WorldStorage.class);
    }

    @NotNull
    private static List<Class<?>> getEntities() {
        return List.of(DChestGroup.class, DChest.class, DWorldId.class);
    }

    @NotNull
    private static DatabaseConfig configureDatabase(DataSourceConfig dataSourceConfig) {
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setDataSourceConfig(dataSourceConfig);
        dbConfig.setRunMigration(true);

        dbConfig.addAll(getEntities());
        return dbConfig;
    }

    @NotNull
    private static DataSourceConfig configureDataSource() {
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        ChestDatabaseConfig loadedConfig = ChestDatabaseConfig.get();
        dataSourceConfig.setUrl(loadedConfig.getUrl());
        dataSourceConfig.setUsername("whyisthisneeeded");
        dataSourceConfig.setPassword("whyisthisneeeded");
        dataSourceConfig.setPlatform("sqlite");
        dataSourceConfig.setAutoCommit(true);
        return dataSourceConfig;
    }
}
