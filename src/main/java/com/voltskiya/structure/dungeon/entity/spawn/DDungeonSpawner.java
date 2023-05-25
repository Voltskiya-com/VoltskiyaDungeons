package com.voltskiya.structure.dungeon.entity.spawn;

import com.voltskiya.structure.database.BaseEntity;
import com.voltskiya.structure.dungeon.entity.DDungeon;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "dungeon_spawner")
public class DDungeonSpawner extends BaseEntity {

    @Id
    private UUID id;
    @ManyToOne
    private DDungeon dungeon;
    @Column(unique = true, nullable = false)
    private String name;
    @Embedded(prefix = "center_")
    private DungeonCenter center;
}
