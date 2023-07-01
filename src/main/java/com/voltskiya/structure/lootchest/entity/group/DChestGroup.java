package com.voltskiya.structure.lootchest.entity.group;

import com.voltskiya.structure.database.BaseEntity;
import com.voltskiya.structure.dungeon.entity.spawn.DDungeonSpawner;
import com.voltskiya.structure.lootchest.entity.chest.ChestStorage;
import com.voltskiya.structure.lootchest.entity.chest.DChest;
import com.voltskiya.structure.lootchest.entity.chest.DChestLootStatus;
import io.ebean.Transaction;
import io.ebean.annotation.DbJson;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

@Entity
@Table(name = "chest_group")
public class DChestGroup extends BaseEntity {

    @Id
    private UUID uuid;
    @OneToOne
    private DDungeonSpawner dungeonSpawner;

    @Column(unique = true)
    private String name;
    @DbJson
    @Column(nullable = false)
    private DChestGroupConfig config;

    @Column
    private Timestamp lootedAt;
    @Column
    private Timestamp restockedAt;
    @Column(nullable = false)
    private double timePassed;
    @Column
    @Enumerated(value = EnumType.STRING)
    private DChestLootStatus status;
    @Column
    @OneToMany(cascade = CascadeType.ALL)
    private List<DChest> chests;

    public DChestGroup(String name) {
        this.name = name;
        this.config = new DChestGroupConfig();
        this.chests = new ArrayList<>();
        this.status = DChestLootStatus.NEVER_TOUCHED;
    }

    public void addChest(Location location, @Nullable String lootTable, Transaction transaction) {
        boolean alreadyExists = this.chests.stream().anyMatch((c) -> c.getLocation().equals(location));
        if (alreadyExists) return;
        DChest foundChest = ChestStorage.findChestAt(location);
        if (foundChest == null) {
            if (lootTable == null) return;
            foundChest = new DChest(location, lootTable);
        }
        foundChest.setGroup(this, transaction);

        this.chests.add(foundChest);
        this.modifyLootedAt(foundChest);
    }

    private void modifyLootedAt(DChest chestToAdd) {
        if (chestToAdd.getStatus() != DChestLootStatus.LOOTED) return;

        this.status = DChestLootStatus.LOOTED;
        Instant chestLootedAt = chestToAdd.getLootedAt();
        if (chestLootedAt == null) return;
        boolean shouldUpdate = this.lootedAt == null || chestLootedAt.isBefore(this.getLootedAt());
        if (shouldUpdate)
            setLootedAt(chestLootedAt);
    }

    public Instant getLootedAt() {
        return this.lootedAt.toInstant();
    }

    private void setLootedAt(Instant l) {
        this.lootedAt = Timestamp.from(l);
    }

    public String getName() {
        return this.name;
    }

    public DChestGroupConfig getConfig() {
        return this.config;
    }


    public List<DChest> getChests() {
        return this.chests;
    }

    public void setLooted() {
        if (status != DChestLootStatus.LOOTED) {
            setLootedAt(Instant.now());
            this.timePassed = 0;
            this.status = DChestLootStatus.LOOTED;
        }
    }

    public void setRestocked(Instant restocked) {
        restockedAt = Timestamp.from(restocked);
        status = DChestLootStatus.RESTOCKED;
        for (DChest chest : getChests()) {
            chest.setRestocked(restocked);
        }
    }

    public DChestGroup passTime(int playerCount, int respawnIntervalTicks) {
        this.timePassed += config.normalizeTimePassedToPerc(playerCount, respawnIntervalTicks);
        return this;
    }

    public boolean shouldRestock() {
        return config.shouldRestock(this.timePassed);
    }


    public DChestGroup removeAllChests() {
        this.chests.clear();
        return this;
    }

    public DDungeonSpawner getSpawner() {
        return this.dungeonSpawner;
    }

    public void setSpawner(DDungeonSpawner spawner) {
        this.dungeonSpawner = spawner;
    }

}
