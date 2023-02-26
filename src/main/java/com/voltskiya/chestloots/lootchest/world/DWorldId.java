package com.voltskiya.chestloots.lootchest.world;

import io.ebean.annotation.Identity;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.checkerframework.common.aliasing.qual.Unique;

@Entity
@Table(name = "world")
public class DWorldId {

    @Id
    @Identity
    public int id;
    @Unique
    @Column
    public UUID worldUUID;

    public DWorldId(UUID worldUUID) {
        this.worldUUID = worldUUID;
    }
}
