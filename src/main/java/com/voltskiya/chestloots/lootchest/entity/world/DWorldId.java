package com.voltskiya.chestloots.lootchest.entity.world;

import io.ebean.Model;
import io.ebean.annotation.Identity;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.checkerframework.common.aliasing.qual.Unique;

@Entity
@Table(name = "world")
public class DWorldId extends Model {

    @Id
    @Identity
    public int id;
    @Unique
    @Column
    public UUID worldUUID;

    public DWorldId(UUID worldUUID) {
        this.worldUUID = worldUUID;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldUUID);
    }
}
