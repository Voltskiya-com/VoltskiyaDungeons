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
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

@Entity
@Table(name = "dungeon_spawner")
public class DDungeonSpawner extends BaseEntity {

    @OneToOne
    protected DChestGroup group;
    @Id
    private UUID id;
    @ManyToOne
    private DDungeon dungeon;
    @Column(unique = true, nullable = false)
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
}
