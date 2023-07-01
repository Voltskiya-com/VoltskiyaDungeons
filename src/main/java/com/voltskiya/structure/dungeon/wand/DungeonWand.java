package com.voltskiya.structure.dungeon.wand;

import apple.mc.utilities.player.chat.SendMessage;
import apple.mc.utilities.player.wand.Wand;
import com.voltskiya.structure.dungeon.entity.DDungeon;
import com.voltskiya.structure.dungeon.entity.layout.DDungeonLayout;
import com.voltskiya.structure.dungeon.entity.layout.DDungeonLayoutMob;
import com.voltskiya.structure.dungeon.entity.layout.DungeonLayoutStorage;
import com.voltskiya.structure.dungeon.entity.schematic.DDungeonSchemMob;
import com.voltskiya.structure.dungeon.entity.schematic.SchemMobSpawnEgg;
import com.voltskiya.structure.dungeon.entity.spawn.DDungeonSpawner;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class DungeonWand extends Wand implements SendMessage {

    private DDungeon dungeon = null;
    private DDungeonSpawner spawner = null;
    private Location pos1 = null;
    private Location pos2 = null;

    public DungeonWand(Player player) {
        super(player);
    }

    private static String locationMessage(Location entityLocation) {
        return "%d %d %d".formatted(
            entityLocation.getBlockX(),
            entityLocation.getBlockY(),
            entityLocation.getBlockZ());
    }

    public static void registerMob(Player player, Entity entity, DDungeonLayout layout) {
        if (DungeonLayoutStorage.findMobAt(layout, entity.getLocation()) != null) {
            SendMessage.get().aqua(player, "Already registered");
            return;
        }
        for (String tag : entity.getScoreboardTags()) {
            DDungeonSchemMob mob = SchemMobSpawnEgg.getSchemMob(layout.getDungeon(), tag);
            if (mob == null) continue;
            Location relative = entity.getLocation().subtract(layout.getCenter());
            new DDungeonLayoutMob(layout, relative, mob).save();
            SendMessage.get().aqua(player,
                "Registered mob at %s with %s".formatted(locationMessage(entity.getLocation()), layout.getDungeon().getName()));
            return;
        }
        SendMessage.get().red(player, "Mob does not have a schematic mob");
    }

    @Override
    public void onEvent(PlayerInteractEvent event) {
        String message = null;
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        Location loc = clickedBlock.getLocation();
        Action action = event.getAction();
        if (action.isLeftClick()) {
            setPos1(loc);
            message = "Pos 1";
        } else if (action.isRightClick()) {
            setPos2(loc);
            message = "Pos 2";
        }
        if (message != null) {
            event.setCancelled(true);
            aqua(event.getPlayer(), String.format("%s set to {%d,%d,%d}", message, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        }
    }

    public DDungeon getDungeon() {
        if (this.dungeon != null) this.dungeon.refresh();
        return this.dungeon;
    }

    public DungeonWand setDungeon(DDungeon dungeon) {
        this.dungeon = dungeon;
        this.spawner = dungeon.getSpawner(DDungeonSpawner.DEFAULT_NAME);
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

    public void onEntityClick(Entity entity, boolean isLeftClick) {
        if (dungeon == null) {
            red(player, "Select a dungeon before registering mobs");
            return;
        }
        DDungeonLayout layout = dungeon.getLayout();
        if (layout == null || layout.getCenter() == null) {
            red(player, "Selected dungeon does not have a layout center yet");
            return;
        }
        if (isLeftClick) {
            deleteMobAt(entity, layout);
            return;
        }
        registerMob(player, entity, layout);
    }

    private void deleteMobAt(Entity entity, DDungeonLayout layout) {
        Location entityLocation = entity.getLocation();
        int deleted = DungeonLayoutStorage.deleteMobAt(layout, entityLocation);
        String xyz = locationMessage(entityLocation);
        if (deleted == 0) {
            red(player, "Mob at " + xyz + " is not associated with a dungeon");
            return;
        }
        entity.remove();
        aqua(player, "Deleted dungeon mob at " + xyz);
    }

    public DDungeonSpawner getSpawner() {
        if (this.spawner != null) this.spawner.refresh();
        return this.spawner;
    }

    public void setSpawner(DDungeonSpawner spawner) {
        this.spawner = spawner;
    }
}
