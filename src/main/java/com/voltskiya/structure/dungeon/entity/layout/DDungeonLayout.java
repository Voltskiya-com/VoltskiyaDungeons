package com.voltskiya.structure.dungeon.entity.layout;

import com.voltskiya.structure.database.BaseEntity;
import com.voltskiya.structure.dungeon.entity.DDungeon;
import com.voltskiya.structure.dungeon.entity.spawn.EmbeddedLocation;
import java.util.List;
import java.util.UUID;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.bukkit.Location;

@Entity
@Table(name = "dungeon_layout")
public class DDungeonLayout extends BaseEntity {

    @Id
    protected UUID id;
    @OneToOne(optional = false, mappedBy = "layout")
    protected DDungeon dungeon;
    @Embedded(prefix = "center_")
    protected EmbeddedLocation center;
    @OneToMany
    protected List<DDungeonLayoutMob> mobs;

    public DDungeonLayout(DDungeon dungeon, Location center) {
        this.dungeon = dungeon;
        this.center = new EmbeddedLocation(center);
    }

    public Location getCenter() {
        return this.center.toLocation();
    }

    public void setCenter(Location center) {
        this.center = new EmbeddedLocation(center);
    }

    public List<DDungeonLayoutMob> getMobs() {
        return this.mobs;
    }
}
