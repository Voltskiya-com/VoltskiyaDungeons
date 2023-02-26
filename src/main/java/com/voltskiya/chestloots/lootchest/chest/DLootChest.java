package com.voltskiya.chestloots.lootchest.chest;

import com.voltskiya.chestloots.lootchest.world.DWorldId;
import io.ebean.annotation.Identity;
import java.sql.Timestamp;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.bukkit.Location;

@Entity
@Table(name = "loot_chest", uniqueConstraints = {@UniqueConstraint(name = "location", columnNames = {"world_id", "x", "y", "z"})})
public class DLootChest {

    @Id
    @Identity
    private long id;

    @Column
    @ManyToOne(fetch = FetchType.EAGER)
    private DWorldId world;
    @Column
    private int x;
    @Column
    private int y;
    @Column
    private int z;
    @Column
    private Timestamp lastLooted;
    private DLootChestStatus status;
    private String lootTable;


    public DLootChest(DWorldId world, Location location, String lootTable) {
        this.world = world;
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.lootTable = lootTable;

        this.status = DLootChestStatus.LOOTED;
        lastLooted = Timestamp.from(Instant.now());
    }

    public void update(String lootTable) {
        this.lootTable = lootTable;
        lastLooted = Timestamp.from(Instant.now());
    }
}
