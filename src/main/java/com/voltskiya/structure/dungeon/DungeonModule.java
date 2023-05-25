package com.voltskiya.structure.dungeon;

import apple.mc.utilities.PluginModuleMcUtil;
import apple.mc.utilities.player.wand.WandType;
import com.voltskiya.lib.AbstractModule;
import com.voltskiya.structure.dungeon.command.DungeonCommand;
import com.voltskiya.structure.dungeon.wand.DungeonWand;

public class DungeonModule extends AbstractModule implements PluginModuleMcUtil {

    private static DungeonModule instance;
    private WandType<DungeonWand> dungeonWand;

    public DungeonModule() {
        instance = this;
    }

    public static DungeonModule get() {
        return instance;
    }

    public WandType<DungeonWand> dungeonWand() {
        return dungeonWand;
    }

    @Override
    public void enable() {
        new DungeonCommand();
        dungeonWand = createWand(DungeonWand::new, "dungeon_wand");
    }

    @Override
    public String getName() {
        return "Dungeon";
    }
}
