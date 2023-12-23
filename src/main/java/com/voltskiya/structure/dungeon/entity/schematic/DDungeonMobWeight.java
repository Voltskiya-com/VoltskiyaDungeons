package com.voltskiya.structure.dungeon.entity.schematic;

import apple.mc.utilities.data.serialize.EntitySerializable;
import com.voltskiya.structure.database.BaseEntity;
import io.ebean.annotation.DbJson;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "dungeon_schem_mob_weight")
public class DDungeonMobWeight extends BaseEntity {

    @Id
    public UUID id;
    @ManyToOne
    public DDungeonSchemMob schematic;
    @Column
    protected int weight;
    @DbJson
    protected EntitySerializable entity;

    public DDungeonMobWeight(EntitySerializable entity) {
        this.entity = entity;
        this.weight = 0;
    }

    public void incrementWeight() {
        this.weight++;
    }

    public EntitySerializable getEntity() {
        return this.entity;
    }

    public int getWeight() {
        return this.weight;
    }

}
