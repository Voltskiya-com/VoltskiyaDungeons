package com.voltskiya.structure.database;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker.Std;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.voltskiya.lib.database.VoltskiyaDatabase;
import com.voltskiya.lib.database.config.VoltskiyaDatabaseConfig;
import com.voltskiya.lib.database.config.VoltskiyaMysqlConfig;
import com.voltskiya.structure.VoltskiyaPlugin;
import com.voltskiya.structure.dungeon.entity.DDungeon;
import com.voltskiya.structure.dungeon.entity.DungeonLookup;
import com.voltskiya.structure.dungeon.entity.layout.DDungeonLayout;
import com.voltskiya.structure.dungeon.entity.layout.DDungeonLayoutMob;
import com.voltskiya.structure.dungeon.entity.schematic.DDungeonMobWeight;
import com.voltskiya.structure.dungeon.entity.schematic.DDungeonSchemMob;
import com.voltskiya.structure.dungeon.entity.spawn.DDungeonSpawner;
import com.voltskiya.structure.dungeon.entity.spawn.EmbeddedLocation;
import com.voltskiya.structure.lootchest.entity.chest.ChestStorage;
import com.voltskiya.structure.lootchest.entity.chest.DChest;
import com.voltskiya.structure.lootchest.entity.group.ChestGroupStorage;
import com.voltskiya.structure.lootchest.entity.group.DChestGroup;
import com.voltskiya.structure.lootchest.entity.world.DWorldId;
import com.voltskiya.structure.lootchest.entity.world.WorldStorage;
import com.voltskiya.structure.lootchest.service.ChestRespawnTask;
import io.ebean.Database;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.NamespacedKey;

public class DungeonDatabase extends VoltskiyaDatabase {

    public static final String NAME = "Dungeon";
    private static DungeonDatabase instance;

    public DungeonDatabase() {
        instance = this;
    }

    public static DungeonDatabase get() {
        return instance;
    }

    public static Database db() {
        return get().getDB();
    }

    @Override
    protected List<Class<?>> getQueryBeans() {
        List<Class<?>> queryBeans = new ArrayList<>(
            List.of(ChestStorage.class, ChestRespawnTask.class, ChestGroupStorage.class, WorldStorage.class));
        queryBeans.add(DungeonLookup.class);
        return queryBeans;
    }

    @Override
    protected List<Class<?>> getEntities() {
        List<Class<?>> entities = new ArrayList<>(List.of(BaseEntity.class, DChestGroup.class, DChest.class, DWorldId.class));
        entities.addAll(
            List.of(DDungeon.class, EmbeddedLocation.class,
                DDungeonSpawner.class,
                DDungeonLayout.class, DDungeonLayoutMob.class,
                DDungeonSchemMob.class, DDungeonMobWeight.class));
        return entities;
    }

    @Override
    protected VoltskiyaDatabaseConfig getConfig() {
        File file = new File(VoltskiyaPlugin.get().getDataFolder(), "DatabaseConfig.json");
        return makeConfig(file, VoltskiyaMysqlConfig.class);
    }

    @Override
    protected Object getObjectMapper() {
        SimpleModule namespacedKeyModule = new SimpleModule()
            .addSerializer(new NamespaceSerializer())
            .addDeserializer(NamespacedKey.class, new NamespaceDeserializer());
        return new ObjectMapper()
            .registerModule(namespacedKeyModule)
            .setVisibility(new Std(Visibility.NONE)
                .withFieldVisibility(Visibility.ANY)
                .withGetterVisibility(Visibility.NONE)
                .withSetterVisibility(Visibility.NONE)
                .withCreatorVisibility(Visibility.NONE)
                .withIsGetterVisibility(Visibility.NONE));
    }

    @Override
    protected String getName() {
        return NAME;
    }
}
