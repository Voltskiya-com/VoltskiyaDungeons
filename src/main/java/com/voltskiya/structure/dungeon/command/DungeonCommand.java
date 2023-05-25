package com.voltskiya.structure.dungeon.command;

import apple.mc.utilities.data.serialize.EntitySerialOptions;
import apple.mc.utilities.data.serialize.EntitySerializable;
import apple.mc.utilities.player.chat.SendMessage;
import com.voltskiya.lib.acf.BaseCommand;
import com.voltskiya.lib.acf.BukkitCommandCompletionContext;
import com.voltskiya.lib.acf.CommandCompletions;
import com.voltskiya.lib.acf.annotation.CommandAlias;
import com.voltskiya.lib.acf.annotation.CommandCompletion;
import com.voltskiya.lib.acf.annotation.CommandPermission;
import com.voltskiya.lib.acf.annotation.Name;
import com.voltskiya.lib.acf.annotation.Optional;
import com.voltskiya.lib.acf.annotation.Subcommand;
import com.voltskiya.structure.VoltskiyaPlugin;
import com.voltskiya.structure.dungeon.DungeonModule;
import com.voltskiya.structure.dungeon.entity.DDungeon;
import com.voltskiya.structure.dungeon.entity.DungeonLookup;
import com.voltskiya.structure.dungeon.entity.schematic.DDungeonMobWeight;
import com.voltskiya.structure.dungeon.entity.schematic.DDungeonSchemMob;
import com.voltskiya.structure.dungeon.wand.DungeonWand;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.ClickEvent.Action;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;

@CommandAlias("dungeon")
@CommandPermission("volt.dungeon")
public class DungeonCommand extends BaseCommand implements SendMessage {

    public DungeonCommand() {
        VoltskiyaPlugin.get().registerCommand(this);
        CommandCompletions<BukkitCommandCompletionContext> commandCompletions = VoltskiyaPlugin.get().getCommandManager()
            .getCommandCompletions();
        commandCompletions.registerCompletion("dungeon-spawner",
            c -> DungeonLookup.findSpawner(c.getContextValueByName(String.class, "dungeon")));
        commandCompletions.registerAsyncCompletion("dungeon", c -> DungeonLookup.dungeonNames());
    }

    @Subcommand("select")
    @CommandCompletion("@dungeon @nothing")
    public void select(Player player, @Name("dungeon") String dungeonArg, @Optional() @Name("") String confirm) {
        DDungeon dungeon = DungeonLookup.findDungeon(dungeonArg);
        if (dungeon == null) {
            if (confirm == null || !confirm.equalsIgnoreCase("confirm")) {
                ClickEvent command = ClickEvent.clickEvent(Action.SUGGEST_COMMAND, "/dungeon select %s confirm".formatted(dungeonArg));
                TextComponent message = Component.text("Dungeon '%s' not found. ".formatted(dungeonArg))
                    .append(Component.text("[Click here]").clickEvent(command))
                    .append(Component.text(" to create."));
                player.sendMessage(message);
                return;
            }
            dungeon = new DDungeon(dungeonArg);
            dungeon.save();
            aqua(player, "Created dungeon '%s'", dungeon.getName());
        }
        DungeonModule.get().dungeonWand().getWand(player).setDungeon(dungeon);
        aqua(player, "Selected dungeon '%s'", dungeon.getName());
    }

    @Subcommand("wand")
    public void wand(Player player) {
        ItemStack wand = DungeonModule.get().dungeonWand().createItem(Material.IRON_INGOT, "Dungeon Wand");
        player.getInventory().addItem(wand);
    }


    @Subcommand("mob")
    public class DungeonMobCommand extends BaseCommand {

        @Subcommand("paste")
        public void paste() {
        }

        @Subcommand("set")
        @CommandCompletion("[name]")
        public void set(Player player, @Name("mob_name") String mobArg) {
            DungeonWand wand = DungeonModule.get().dungeonWand().getWand(player);
            Location pos1 = wand.getPos1();
            Location pos2 = wand.getPos2();
            if (pos1 == null || pos2 == null) {
                red(player, "Select an area with a Dungeon Wand before trying to create a MobType");
                return;
            }
            DDungeon dungeon = wand.getDungeon();
            if (dungeon == null) {
                red(player, "Select a dungeon before trying to create a MobType /dungeon select [dungeon_name]");
                return;
            }
            DDungeonSchemMob mobType = DungeonLookup.findMob(dungeon, mobArg);
            if (mobType == null) {
                mobType = new DDungeonSchemMob(dungeon, mobArg);
            }
            mobType = scanMobConfig(pos1, pos2, mobType);
            if (mobType == null) {
                red(player, "Select an area with mobs before trying to create a MobType");
                return;
            }
            sendMobMessage(player, mobType);
        }

        private void sendMobMessage(CommandSender player, DDungeonSchemMob mobType) {
            double totalWeight = mobType.getMobs().stream().mapToInt(DDungeonMobWeight::getWeight).sum();
            double noSpawnWeight = mobType.getNoSpawnWeight();
            noSpawnWeight /= totalWeight + noSpawnWeight;

            Component message = Component.text("Mob: '%s'".formatted(mobType.getName()))
                .appendNewline()
                .append(Component.text("Chance to spawn: %.2f".formatted(1 - noSpawnWeight)));

            for (DDungeonMobWeight mob : mobType.getMobs()) {
                double weight = mob.getWeight() / totalWeight;
                Component displayName = mob.getEntity().getDisplayName();
                message = message.appendNewline()
                    .append(displayName)
                    .append(Component.text(" = %.2f".formatted(weight)));
            }
            player.sendMessage(message);
        }

        private DDungeonSchemMob scanMobConfig(Location pos1, Location pos2, DDungeonSchemMob mobType) {
            Collection<Entity> worldMobs = pos1.getWorld().getNearbyEntities(BoundingBox.of(pos1, pos2));

            Map<EntitySerializable, DDungeonMobWeight> mobTypes = new HashMap<>();
            int noSpawnWeight = 0;
            for (Entity worldMob : worldMobs) {
                if (worldMob instanceof Player) continue;
                if (worldMob instanceof ArmorStand) {
                    noSpawnWeight++;
                    continue;
                }
                EntitySerialOptions serialOptions = new EntitySerialOptions(true, true);
                EntitySerializable serial = new EntitySerializable(worldMob, serialOptions);
                serial.getEntityTag().removeKey("NoAi"); // NoAi is just for config mobs
                serial.saveEntityTag();

                DDungeonMobWeight mob = mobTypes.computeIfAbsent(serial, DDungeonMobWeight::new);
                mob.incrementWeight();
            }
            if (mobTypes.isEmpty()) return null;
            mobType.setMobs(mobTypes.values().stream().toList());
            mobType.setNoSpawnWeight(noSpawnWeight);
            mobType.save();
            return mobType;
        }
    }

}
