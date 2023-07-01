package com.voltskiya.structure.dungeon.entity.layout;

import com.voltskiya.structure.database.BaseEntity;
import com.voltskiya.structure.dungeon.entity.schematic.DDungeonSchemMob;
import com.voltskiya.structure.dungeon.entity.spawn.EmbeddedLocation;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

@Entity
@Table(name = "dungeon_layout_mob")
public class DDungeonLayoutMob extends BaseEntity {

    @Id
    protected UUID id;
    @ManyToOne(optional = false)
    protected DDungeonLayout layout;

    @Column
    @Embedded(prefix = "relative_")
    protected EmbeddedLocation relative;
    @ManyToOne
    protected DDungeonSchemMob mob;

    public DDungeonLayoutMob(DDungeonLayout layout, Location relativeLocation, DDungeonSchemMob mob) {
        this.layout = layout;
        this.mob = mob;
        this.relative = new EmbeddedLocation(relativeLocation);
    }

    @Nullable
    public Location getLocation(Location center) {
        Location relative = this.relative.toLocation();
        if (center == null || relative == null) return null;
        relative.setWorld(center.getWorld());
        return relative.add(center);
    }

    public DDungeonSchemMob getSchemMob() {
        return this.mob;
    }
}
