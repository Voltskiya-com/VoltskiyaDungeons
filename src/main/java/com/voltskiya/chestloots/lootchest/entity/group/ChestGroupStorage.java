package com.voltskiya.chestloots.lootchest.entity.group;

import com.voltskiya.chestloots.lootchest.entity.chest.DChestLootStatus;
import com.voltskiya.chestloots.lootchest.entity.group.query.QDChestGroup;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ChestGroupStorage {

    public static List<String> listGroupNames() {
        return new QDChestGroup()
            .select(QDChestGroup.alias().name)
            .findSingleAttribute();
    }

    @NotNull
    public static DChestGroup computeChestGroup(@NotNull String name) {
        DChestGroup group = new QDChestGroup()
            .where()
            .name.eq(name)
            .findOne();
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
}
