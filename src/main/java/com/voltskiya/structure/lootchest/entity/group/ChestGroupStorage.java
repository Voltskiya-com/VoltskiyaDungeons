package com.voltskiya.structure.lootchest.entity.group;

import com.voltskiya.structure.lootchest.entity.chest.DChestLootStatus;
import com.voltskiya.structure.lootchest.entity.group.query.QDChestGroup;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ChestGroupStorage {

    public static List<String> listGroupNames() {
        return new QDChestGroup()
            .select(QDChestGroup.alias().name)
            .findSingleAttributeList();
    }

    @NotNull
    public static DChestGroup computeChestGroup(@NotNull String name) {
        DChestGroup group = findChestGroup(name);
        if (group == null) {
            group = new DChestGroup(name);
            group.save();
        }
        return group;
    }

    public static List<DChestGroup> listLootedGroups() {
        return new QDChestGroup()
            .where()
            .status.eq(DChestLootStatus.LOOTED)
            .findList();
    }

    public static DChestGroup findChestGroup(String name) {
        return new QDChestGroup()
            .where()
            .name.eq(name)
            .findOne();
    }

}
