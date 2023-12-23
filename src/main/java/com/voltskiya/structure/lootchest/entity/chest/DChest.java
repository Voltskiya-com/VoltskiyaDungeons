package com.voltskiya.structure.lootchest.entity.chest;

import com.voltskiya.structure.database.BaseEntity;
import com.voltskiya.structure.lootchest.LootChestModuleConfig;
import com.voltskiya.structure.lootchest.entity.group.DChestGroup;
import com.voltskiya.structure.lootchest.entity.world.DWorldId;
import com.voltskiya.structure.lootchest.entity.world.WorldStorage;
import io.ebean.Transaction;
import io.ebean.annotation.Identity;
import java.sql.Timestamp;
import java.time.Instant;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.bukkit.Location;

@Entity
@Table(name = "chest", uniqueConstraints = {@UniqueConstraint(name = "location", columnNames = {"world_id", "x", "y", "z"})})
public class DChest extends BaseEntity {

    @Id
    @Identity
    private long id;

    @Column(nullable = false)
    @ManyToOne(fetch = FetchType.EAGER)
    private DWorldId world;
    @Column(nullable = false)
    private int x;
    @Column(nullable = false)
    private int y;
    @Column(nullable = false)
    private int z;
    @Column
    private Timestamp lootedAt;
    @Column
    private Timestamp restockedAt;
    @Column(nullable = false)
    private double timePassed;
    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private DChestLootStatus status;
    @Column(nullable = false)
    private String lootTable;

    @Column
    @ManyToOne(cascade = CascadeType.ALL)
    private DChestGroup group;

    public DChest(Location location, String lootTable) {
        this.world = WorldStorage.getWorld(location.getWorld().getUID());
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.lootTable = lootTable;

        this.status = DChestLootStatus.NEVER_TOUCHED;
    }

    public void setLooted(String lootTable) {
        this.lootTable = lootTable;
        if (status != DChestLootStatus.LOOTED) {
            this.lootedAt = Timestamp.from(Instant.now());
            this.timePassed = 0;
            this.status = DChestLootStatus.LOOTED;
            if (this.group != null) this.group.setLooted();
        }
    }

    public void setGroup(DChestGroup group, Transaction transaction) {
        this.group = group;
        this.save(transaction);
    }

    public Location getLocation() {
        return new Location(this.world.getWorld(), this.x, this.y, this.z);
    }

    public String getLootTable() {
        return this.lootTable;
    }

    public void setRestocked(Instant restockedAt) {
        this.restockedAt = Timestamp.from(restockedAt);
        this.status = DChestLootStatus.RESTOCKED;
    }

    public DChest passTime(int playerCount, int respawnIntervalTicks) {
        this.timePassed += LootChestModuleConfig.get().normalizeTimePassedToPerc(playerCount, respawnIntervalTicks);
        return this;
    }

    public boolean shouldRestock() {
        return LootChestModuleConfig.get().shouldRestock(this.timePassed);
    }

    public DChestLootStatus getStatus() {
        return this.status;
    }

    public Instant getLootedAt() {
        return this.lootedAt.toInstant();
    }
}
