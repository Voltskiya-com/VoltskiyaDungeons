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
import com.voltskiya.structure.dungeon.entity.layout.DDungeonLayout;
import com.voltskiya.structure.dungeon.entity.layout.DDungeonLayoutMob;
import com.voltskiya.structure.dungeon.entity.schematic.DDungeonMobWeight;
import com.voltskiya.structure.dungeon.entity.schematic.DDungeonSchemMob;
import com.voltskiya.structure.dungeon.entity.schematic.SchemMobSpawnEgg;
import com.voltskiya.structure.dungeon.entity.spawn.DDungeonSpawner;
import com.voltskiya.structure.dungeon.entity.spawn.DungeonSpawnerStorage;
import com.voltskiya.structure.dungeon.wand.DungeonWand;
import com.voltskiya.structure.lootchest.entity.group.DChestGroup;
import io.ebean.Model;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.ClickEvent.Action;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@CommandAlias("dungeon")
@CommandPermission("volt.dungeon")
public class DungeonCommand extends BaseCommand implements SendMessage {

    private static final Pattern NAME_REGEX = Pattern.compile("[a-zA-Z0-9]+");

    public DungeonCommand() {
        VoltskiyaPlugin.get().registerCommand(this);
        CommandCompletions<BukkitCommandCompletionContext> commandCompletions = VoltskiyaPlugin.get().getCommandManager()
            .getCommandCompletions();
        commandCompletions.registerAsyncCompletion("dungeon-spawner",
            c -> {
                DungeonWand wand = DungeonModule.get().dungeonWand().getWand(c.getPlayer());
                if (wand.getDungeon() == null) return Collections.emptyList();
                return wand.getDungeon().getSpawners().stream().map(DDungeonSpawner::getName).toList();
            });
        commandCompletions.registerAsyncCompletion("dungeon-mob",
            c -> {
                DungeonWand wand = DungeonModule.get().dungeonWand().getWand(c.getPlayer());
                if (wand.getDungeon() == null) return Collections.emptyList();
                return wand.getDungeon().getMobTypes().stream().map(DDungeonSchemMob::getName).toList();
            });
        commandCompletions.registerAsyncCompletion("dungeon", c -> DungeonLookup.dungeonNames());
    }

    @Subcommand("select")
    @CommandCompletion("@dungeon @nothing")
    public void select(Player player, @Name("dungeon") String dungeonArg, @Optional() @Name("") String confirm) {
        if (!NAME_REGEX.asMatchPredicate().test(dungeonArg)) {
            red(player, "Match regex " + NAME_REGEX.pattern());
            return;
        }
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

    @Nullable
    private DDungeonLayout getPlayerLayout(Player player) {
        DungeonWand wand = DungeonModule.get().dungeonWand().getWand(player);
        DDungeon dungeon = wand.getDungeon();
        if (dungeon == null) {
            red(player, "Select a dungeon before trying to do this");
            return null;
        }
        DDungeonLayout layout = dungeon.getLayout();
        if (layout == null) {
            red(player, "The center has not been set for the selected dungeon");
            return null;
        }
        return layout;
    }

    private <T> boolean checkConfirm(Player player, String confirm, T namable, Function<T, String> getName, String command) {
        if (namable == null) {
            red(player, "Nothing selected!");
            return false;
        }
        if (confirm == null || !confirm.equalsIgnoreCase("confirm")) {
            red(player, "Are you sure you want to delete %s?\nTo confirm run: %s confirm".formatted(getName.apply(namable), command));
            return false;
        }
        return true;
    }

    @Subcommand("delete")
    @CommandCompletion("@nothing")
    public void delete(Player player, @Optional @Name("confirm") String confirm) {
        DungeonWand wand = DungeonModule.get().dungeonWand().getWand(player);
        DDungeon dungeon = wand.getDungeon();
        boolean canContinue = checkConfirm(player, confirm, dungeon, DDungeon::getName, "/dungeon delete");
        if (!canContinue) return;
        for (DDungeonSpawner spawner : dungeon.getSpawners()) {
            DChestGroup chestGroup = spawner.getChestGroup();
            if (chestGroup != null) {
                chestGroup.setSpawner(null);
                chestGroup.save();
            }
            spawner.setChestGroup(null);
            spawner.save();
            spawner.delete();
        }
        for (DDungeonSchemMob schem : dungeon.getMobTypes()) {
            List<DDungeonMobWeight> mobs = schem.getMobs();
            for (DDungeonMobWeight weight : mobs) {
                weight.delete();
            }
            schem.delete();
        }
        DDungeonLayout layout = dungeon.getLayout();
        if (layout != null) {
            for (DDungeonLayoutMob mob : layout.getMobs()) {
                mob.delete();
            }
        }
        dungeon.delete();
        if (layout != null)
            layout.delete();

        aqua(player, "Deleted dungeon " + dungeon.getName());
    }

    @Subcommand("spawner")
    public class DungeonSpawnerCommand extends BaseCommand {

        @Subcommand("kill")
        public void kill(Player player) {
            DDungeonSpawner spawner = getSpawner(player);
            if (spawner == null) return;
            DungeonSpawnerStorage.kill(spawner);
        }

        @Subcommand("tp")
        public void tp(Player player) {
            DDungeonSpawner spawner = getSpawner(player);
            if (spawner == null) return;
            Location center = spawner.getCenter();
            if (center == null) {
                red(player, "Center is not set");
                return;
            }
            player.teleport(center);
        }

        @Nullable
        private DDungeonSpawner getSpawner(Player player) {
            DungeonWand wand = DungeonModule.get().dungeonWand().getWand(player);
            DDungeonSpawner spawner = wand.getSpawner();
            if (spawner == null) {
                red(player, "Select a dungeon spawner before trying to do this");
                return null;
            }
            return spawner;
        }

        @Subcommand("paste")
        public void paste(Player player) {
            DDungeonSpawner spawner = getSpawner(player);
            if (spawner == null) return;
            DungeonSpawnerStorage.summon(spawner);
        }

        @Subcommand("center set")
        public void setCenter(Player player) {
            DDungeonSpawner spawner = getSpawner(player);
            if (spawner == null) return;
            spawner.setCenter(player.getLocation());
            spawner.save();
            aqua(player, "Successfully set the center");
        }

        @Subcommand("select")
        @CommandCompletion("@dungeon-spawner @nothing")
        public void select(Player player, @Name("spawner") String spawnerArg, @Optional String confirm) {
            if (!NAME_REGEX.asMatchPredicate().test(spawnerArg)) {
                red(player, "Match regex " + NAME_REGEX.pattern());
                return;
            }
            DungeonWand wand = DungeonModule.get().dungeonWand().getWand(player);
            DDungeon dungeon = wand.getDungeon();
            if (dungeon == null) {
                red(player, "Select a dungeon before trying to do this");
                return;
            }
            DDungeonSpawner spawner = dungeon.getSpawner(spawnerArg);
            if (spawner == null) {
                if (confirm == null) {
                    ClickEvent command = ClickEvent.clickEvent(Action.SUGGEST_COMMAND,
                        "/dungeon spawner select %s confirm".formatted(spawnerArg));
                    TextComponent message = Component.text(
                            "Spawner '%s' not found for dungeon '%s'. ".formatted(spawnerArg, dungeon.getName()))
                        .append(Component.text("[Click here]").clickEvent(command))
                        .append(Component.text(" to create."));
                    player.sendMessage(message);
                    return;
                }
                spawner = new DDungeonSpawner(dungeon, spawnerArg);
                spawner.save();
            }
            wand.setSpawner(spawner);
            aqua(player, "Selected %s.%s".formatted(dungeon.getName(), spawner.getName()));
        }

        @Subcommand("delete")
        @CommandCompletion("@nothing")
        public void delete(Player player, @Optional @Name("confirm") String confirm) {
            DungeonWand wand = DungeonModule.get().dungeonWand().getWand(player);
            DDungeonSpawner spawner = wand.getSpawner();
            boolean canContinue = checkConfirm(player, confirm, spawner, DDungeonSpawner::getFullName, "/dungeon spawner delete");
            if (!canContinue) return;
            spawner.delete();
            aqua(player, "Deleted dungeon " + spawner.getName());
        }
    }

    @Subcommand("layout")
    public class DungeonLayoutCommand extends BaseCommand {

        @Subcommand("kill")
        public void kill(Player player) {
            DDungeonLayout layout = getPlayerLayout(player);
            if (layout == null) return;
            List<DDungeonLayoutMob> mobs = layout.getMobs();
            Location center = layout.getCenter();
            if (center == null) {
                red(player, "Set the layout center before trying to do this");
                return;
            }
            for (DDungeonLayoutMob mob : mobs) {
                SchemMobSpawnEgg.killSchematic(mob.getSchemMob());
            }
        }

        @Subcommand("register mob")
        @CommandCompletion("@range:10-100 @nothing")
        public void registerMobs(Player player, @Name("radius") int radius) {
            DDungeonLayout layout = getPlayerLayout(player);
            if (layout == null) return;
            List<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);
            nearbyEntities.sort(Comparator.comparing(Entity::getLocation, Comparator.comparingDouble(player.getLocation()::distance)));
            for (Entity entity : nearbyEntities) {
                DungeonWand.registerMob(player, entity, layout);
            }
        }

        @Subcommand("tp")
        public void teleport(Player player) {
            DDungeonLayout layout = getPlayerLayout(player);
            if (layout == null) return;
            player.teleport(layout.getCenter());
        }

        @Subcommand("paste")
        public void paste(Player player) {
            DDungeonLayout layout = getPlayerLayout(player);
            if (layout == null) return;
            List<DDungeonLayoutMob> mobs = layout.getMobs();
            Location center = layout.getCenter();
            if (center == null) {
                red(player, "Set the layout center before trying to do this");
                return;
            }
            mobs.stream().map(DDungeonLayoutMob::getSchemMob).forEach(SchemMobSpawnEgg::killSchematic);
            for (DDungeonLayoutMob mob : mobs) {
                Location location = mob.getLocation(center);
                if (location == null) continue;
                SchemMobSpawnEgg.summonSchematic(location, mob.getSchemMob());
            }
        }

        @Subcommand("center set")
        public void setCenter(Player player) {
            DungeonWand wand = DungeonModule.get().dungeonWand().getWand(player);
            DDungeon dungeon = wand.getDungeon();
            if (dungeon == null) {
                red(player, "Select a dungeon before trying to set the center of the layout");
                return;
            }
            DDungeonLayout layout = dungeon.getLayout();
            if (layout == null) {
                layout = new DDungeonLayout(dungeon, player.getLocation());
                layout.save();
                dungeon.setLayout(layout).save();
            } else {
                layout.setCenter(player.getLocation());
                layout.save();
            }
            aqua(player, "Successfully set the center");
        }
    }

    @Subcommand("mob")
    public class DungeonMobCommand extends BaseCommand {

        private static void sendMobMessage(CommandSender player, DDungeonSchemMob mobType) {
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

        private static DDungeonSchemMob scanMobConfig(Location pos1, Location pos2, DDungeonSchemMob mobType) {
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
                serial.getEntityTag().removeKey("NoAI"); // NoAi is just for config mobs
                serial.saveEntityTag();

                DDungeonMobWeight mob = mobTypes.computeIfAbsent(serial, DDungeonMobWeight::new);
                mob.incrementWeight();
            }
            if (mobTypes.isEmpty()) return null;
            mobType.getMobs().forEach(Model::delete);
            mobType.setMobs(mobTypes.values().stream().toList());
            mobType.setNoSpawnWeight(noSpawnWeight);
            mobType.save();
            return mobType;
        }

        @Subcommand("paste")
        @CommandCompletion("@dungeon-mob")
        public void paste(Player player, @Name("mob_name") String mobArg) {
            DungeonWand wand = DungeonModule.get().dungeonWand().getWand(player);
            DDungeon dungeon = wand.getDungeon();
            if (dungeon == null) {
                red(player, "Select a dungeon before trying to do this");
                return;
            }
            DDungeonSchemMob schemMob = DungeonLookup.findMob(dungeon, mobArg);
            if (schemMob == null) {
                red(player, "There is no mob named %s in %s".formatted(mobArg, dungeon.getName()));
                return;
            }
            Vector direction = player.getFacing().getDirection();
            Location column = player.getLocation().setDirection(direction);
            Location location = column.clone();
            Vector rotatedDirection = direction.clone().rotateAroundY(Math.toRadians(90));
            for (DDungeonMobWeight mob : schemMob.getMobs()) {
                for (int i = 0; i < mob.getWeight(); i++) {
                    mob.getEntity().spawn(location, (e) -> {
                        if (e instanceof LivingEntity living) living.setAI(false);
                    });
                    location.add(direction);
                }
                column.add(rotatedDirection);
                location = column.clone();
            }
            for (int i = 0; i < schemMob.getNoSpawnWeight(); i++) {
                location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
            }
            ItemStack egg = SchemMobSpawnEgg.makeSpawnEgg(schemMob);
            if (egg != null)
                player.getInventory().addItem(egg);
        }

        @Subcommand("set")
        @CommandCompletion("@dungeon-mob|[name]")
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
            if (!NAME_REGEX.asMatchPredicate().test(mobArg)) {
                red(player, "Match regex " + NAME_REGEX.pattern());
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
            ItemStack mobSpawnEgg = SchemMobSpawnEgg.makeSpawnEgg(mobType);
            if (mobSpawnEgg == null) {
                red(player, "An error occurred saving the spawn egg");
                return;
            }
            wand.getDungeon().refresh();
            player.getInventory().addItem(mobSpawnEgg);
        }
    }

}
