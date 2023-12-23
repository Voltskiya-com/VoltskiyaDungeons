package com.voltskiya.structure.dungeon.entity.spawn;

import com.voltskiya.structure.database.BaseEntity;
import com.voltskiya.structure.dungeon.entity.DDungeon;
import com.voltskiya.structure.lootchest.entity.group.DChestGroup;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

@Entity
@Table(name = "dungeon_spawner", uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "dungeon_id"})})
public class DDungeonSpawner extends BaseEntity {

    public static final String DEFAULT_NAME = "default";
    @Id
    private UUID id;
    @OneToOne(mappedBy = "dungeonSpawner")
    private DChestGroup group;
    @ManyToOne
    private DDungeon dungeon;
    @Column(nullable = false)
    private String name;
    @Embedded(prefix = "center_")
    private EmbeddedLocation center;

    public DDungeonSpawner(DDungeon dungeon, String name) {
        this.dungeon = dungeon;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }


    @Nullable
    public Location getCenter() {
        if (this.center == null) return null;
        return center.toLocation();
    }

    public void setCenter(Location location) {
        this.center = new EmbeddedLocation(location);
    }

    public DDungeon getDungeon() {
        return this.dungeon;
    }

    public String getFullName() {
        return this.dungeon.getName() + "." + getName();
    }

    public DChestGroup getChestGroup() {
        return group;
    }

    public DDungeonSpawner setChestGroup(DChestGroup group) {
        this.group = group;
        return this;
    }

    public UUID getId() {
        return this.id;
    }
}
