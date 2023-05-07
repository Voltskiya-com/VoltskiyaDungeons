package com.voltskiya.chestloots.lootchest.util;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;

public class ChestNBT {

    public static String getLootTable(BlockState blockState) {
        if (blockState instanceof Container)
            return NBT.get(blockState, nbt -> nbt.getString("LootTable"));
        return null;
    }

    public static void setLootTable(BlockState blockState, String lootTable) {
        if (blockState instanceof Container)
            NBT.modify(blockState, nbt -> {nbt.setString("LootTable", lootTable);});
    }
}
