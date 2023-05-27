package com.voltskiya.structure.dungeon.entity;

import com.voltskiya.structure.database.BaseEntity;
import com.voltskiya.structure.dungeon.entity.layout.DDungeonLayout;
import com.voltskiya.structure.dungeon.entity.schematic.DDungeonSchemMob;
import com.voltskiya.structure.dungeon.entity.spawn.DDungeonSpawner;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "dungeon")
public class DDungeon extends BaseEntity {

    @Id
    protected UUID id;
    @Column(unique = true, nullable = false)
    protected String name;
    @OneToMany
    protected List<DDungeonSpawner> spawners = new ArrayList<>();
    @OneToMany
    protected List<DDungeonSchemMob> mobTypes = new ArrayList<>();
    @OneToOne
    protected DDungeonLayout layout;

    public DDungeon(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public List<DDungeonSchemMob> getMobTypes() {
        return this.mobTypes;
    }

    public DDungeonLayout getLayout() {
        return this.layout;
    }

    public DDungeon setLayout(DDungeonLayout layout) {
        this.layout = layout;
        return this;
    }

    public DDungeonSpawner getSpawner(String spawnerArg) {
        for (DDungeonSpawner spawner : spawners) {
            if (spawner.getName().equalsIgnoreCase(spawnerArg))
                return spawner;
        }
        return null;
    }

    public List<DDungeonSpawner> getSpawners() {
        return this.spawners;
    }
}
