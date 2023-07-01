package com.voltskiya.structure.dungeon.entity.schematic;

import com.voltskiya.structure.database.BaseEntity;
import com.voltskiya.structure.dungeon.entity.DDungeon;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "dungeon_schem_mob", uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "dungeon_id"})})
public class DDungeonSchemMob extends BaseEntity {

    @Id
    private UUID id;
    @ManyToOne(optional = false)
    private DDungeon dungeon;
    @Column(nullable = false)
    private String name;
    @OneToMany(cascade = CascadeType.ALL)
    private List<DDungeonMobWeight> mobs;
    @Column
    private int noSpawnWeight;

    public DDungeonSchemMob(DDungeon dungeon, String name) {
        this.dungeon = dungeon;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public DDungeonSchemMob setName(String name) {
        this.name = name;
        return this;
    }

    public DDungeon getDungeon() {
        return dungeon;
    }

    public List<DDungeonMobWeight> getMobs() {
        return mobs.stream().sorted(Comparator.comparingInt(DDungeonMobWeight::getWeight).reversed()).toList();
    }

    public DDungeonSchemMob setMobs(List<DDungeonMobWeight> mobs) {
        this.mobs = mobs;
        return this;
    }

    public int getNoSpawnWeight() {
        return noSpawnWeight;
    }

    public DDungeonSchemMob setNoSpawnWeight(int noSpawnWeight) {
        this.noSpawnWeight = noSpawnWeight;
        return this;
    }

}
