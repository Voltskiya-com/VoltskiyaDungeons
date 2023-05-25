package com.voltskiya.structure.dungeon.entity;

import com.voltskiya.structure.database.BaseEntity;
import com.voltskiya.structure.dungeon.entity.schematic.DDungeonSchemMob;
import com.voltskiya.structure.dungeon.entity.spawn.DDungeonSpawner;
import com.voltskiya.structure.lootchest.entity.group.DChestGroup;
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
    @OneToOne
    protected DChestGroup group;
    @OneToMany
    protected List<DDungeonSpawner> spawners = new ArrayList<>();
    @OneToMany
    protected List<DDungeonSchemMob> mobs = new ArrayList<>();

    public DDungeon(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
