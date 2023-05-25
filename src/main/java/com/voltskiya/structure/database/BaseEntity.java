package com.voltskiya.structure.database;

import io.ebean.Model;
import io.ebean.annotation.DbName;
import javax.persistence.MappedSuperclass;

@DbName(DungeonDatabase.NAME)
@MappedSuperclass
public class BaseEntity extends Model {

    public BaseEntity() {
        super(DungeonDatabase.NAME);
    }
}
