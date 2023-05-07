package com.voltskiya.chestloots.lootchest.entity.group;

import com.voltskiya.chestloots.lootchest.entity.chest.ChestStorage;
import com.voltskiya.chestloots.lootchest.entity.chest.DChest;
import com.voltskiya.chestloots.lootchest.entity.chest.DChestLootStatus;
import io.ebean.Model;
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
import javax.persistence.Table;
import org.bukkit.Location;

@Entity
@Table(name = "chest_group")
public class DChestGroup extends Model {

    @Id
    private UUID uuid;
    @Column
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
        status = DChestLootStatus.NEVER_TOUCHED;
    }

    public void addChest(Location location, String lootTable, Transaction transaction) {
        boolean alreadyExists = this.chests.stream().anyMatch((c) -> c.getLocation().equals(location));
        if (alreadyExists) return;
        DChest foundChest = ChestStorage.findChestAt(location);
        if (foundChest == null) foundChest = new DChest(location, lootTable);
        foundChest.removeGroup(transaction);
        this.chests.add(foundChest);
    }

    public List<DChest> getChests() {
        return this.chests;
    }

    public void setLooted() {
        if (status != DChestLootStatus.LOOTED) {
            this.lootedAt = Timestamp.from(Instant.now());
            this.timePassed = 0;
            this.status = DChestLootStatus.LOOTED;
        }
    }

    public void setRestocked(Instant restocked) {
        restockedAt = Timestamp.from(restocked);
        status = DChestLootStatus.RESTOCKED;
    }

    public DChestGroup passTime(int playerCount, int respawnIntervalTicks) {
        this.timePassed = config.normalizeTimePassedToPerc(playerCount, respawnIntervalTicks);
        return this;
    }

    public boolean shouldRestock() {
        return config.getNormalizedRestockTime() >= this.timePassed;
    }
}
