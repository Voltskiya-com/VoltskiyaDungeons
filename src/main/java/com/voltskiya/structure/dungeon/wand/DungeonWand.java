package com.voltskiya.structure.dungeon.wand;

import apple.mc.utilities.player.chat.SendMessage;
import apple.mc.utilities.player.wand.Wand;
import com.voltskiya.structure.dungeon.entity.DDungeon;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class DungeonWand extends Wand {

    private DDungeon dungeon = null;
    private Location pos1 = null;
    private Location pos2 = null;

    public DungeonWand(Player player) {
        super(player);
    }

    @Override
    public void onEvent(PlayerInteractEvent event) {
        String message = null;
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        Location loc = clickedBlock.getLocation();
        Action action = event.getAction();
        if (action.isLeftClick()) {
            pos1 = loc;
            message = "Pos 1";
        } else if (action.isRightClick()) {
            pos2 = loc;
            message = "Pos 2";
        }
        if (message != null) {
            event.setCancelled(true);
            SendMessage.get().aqua(event.getPlayer(),
                String.format("%s set to {%d,%d,%d}", message, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        }
    }


    public DDungeon getDungeon() {
        return dungeon;
    }

    public DungeonWand setDungeon(DDungeon dungeon) {
        this.dungeon = dungeon;
        return this;
    }

    public Location getPos1() {
        return pos1;
    }

    public DungeonWand setPos1(Location pos1) {
        this.pos1 = pos1;
        return this;
    }

    public Location getPos2() {
        return pos2;
    }

    public DungeonWand setPos2(Location pos2) {
        this.pos2 = pos2;
        return this;
    }
}
