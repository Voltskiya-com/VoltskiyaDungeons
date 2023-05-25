package com.voltskiya.structure.dungeon.entity.spawn;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class DungeonCenter {

    @Column

    protected UUID world;
    @Column
    protected double x;
    @Column
    protected double y;
    @Column
    protected double z;

}
