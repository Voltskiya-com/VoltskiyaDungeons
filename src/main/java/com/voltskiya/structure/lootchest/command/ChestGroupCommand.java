package com.voltskiya.structure.lootchest.command;

import static apple.mc.utilities.world.vector.VectorUtils.distance;

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
import com.voltskiya.structure.database.DungeonDatabase;
import com.voltskiya.structure.dungeon.DungeonModule;
import com.voltskiya.structure.dungeon.entity.spawn.DDungeonSpawner;
import com.voltskiya.structure.dungeon.wand.DungeonWand;
import com.voltskiya.structure.lootchest.entity.chest.ChestStorage;
import com.voltskiya.structure.lootchest.entity.chest.DChest;
import com.voltskiya.structure.lootchest.entity.group.ChestGroupStorage;
import com.voltskiya.structure.lootchest.entity.group.DChestGroup;
import com.voltskiya.structure.lootchest.entity.group.DChestGroupConfig;
import com.voltskiya.structure.lootchest.service.ChestRespawnTask;
import com.voltskiya.structure.lootchest.util.ChestNBT;
import io.ebean.Transaction;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("chest_group")
@CommandPermission("volt.gm.loot.chest")
public class ChestGroupCommand extends BaseCommand implements SendMessage {

    public ChestGroupCommand() {
        VoltskiyaPlugin.get().registerCommand(this);
        CommandCompletions<BukkitCommandCompletionContext> completions = VoltskiyaPlugin.get()
            .getCommandManager().getCommandCompletions();
        completions.registerAsyncCompletion("chestGroups", (c) -> ChestGroupStorage.listGroupNames());
        completions.registerStaticCompletion("chestConfigKey", DChestGroupConfig.autoComplete());
        completions.registerAsyncCompletion("chestConfigValue", context -> {
            String chestGroupName = context.getContextValueByName(String.class, "group_name");
            DChestGroup chestGroup = ChestGroupStorage.findChestGroup(chestGroupName);
            if (chestGroup == null) return Collections.emptyList();
            String key = context.getContextValueByName(String.class, "key");
            if (key == null) return Collections.emptyList();
            Double value = chestGroup.getConfig().getConfigOrDefault(key.split(" ")[0]);
            return Collections.singletonList(String.valueOf(value));
        });
    }

    private static List<BlockState> findChestsInRadius(int radius, Location center) {
        Location location = center.clone().add(-radius, -radius, -radius);
        int originalY = location.getBlockY();
        int originalZ = location.getBlockZ();
        World world = location.getWorld();
        List<BlockState> nearbyChests = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockState block = world.getBlockState(location);
                    if (block instanceof Container)
                        nearbyChests.add(block);

                    location.add(0, 0, 1);
                }
                location.add(0, 1, 0);
                location.setZ(originalZ);
            }
            location.add(1, 0, 0);
            location.setY(originalY);
        }
        return nearbyChests;
    }

    private static String summarizeChests(Location center, List<DChest> chests) {
        chests.sort(Comparator.comparingDouble(chest -> distance(chest.getLocation(), center)));
        List<String> chestSummaries = new ArrayList<>(chests.size());
        for (DChest chest : chests) {
            Location location = chest.getLocation();
            Block block = location.getBlock();
            if (!(block.getState() instanceof Container)) continue;
            String chestSummary = "%s [%s] {%s,%d %d %d} - distance: %.2f ".formatted(
                block.getType(),
                chest.getLootTable(),
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                distance(center, location));
            chestSummaries.add(chestSummary);

        }
        return String.join("\n", chestSummaries);
    }

    private DChestGroup getChestGroup(CommandSender sender, String chestGroupName) {
        if (chestGroupName == null) {
            red(sender, "'group_name' is required");
        }
        DChestGroup chestGroup = ChestGroupStorage.findChestGroup(chestGroupName);
        if (chestGroup == null) {
            red(sender, "'%s' does not exist", chestGroupName);
        }
        return chestGroup;
    }

    @Subcommand("unmark")
    @CommandCompletion("@range:10-100")
    public void unmark(Player player, @Name("radius") int radius) {
        List<BlockState> nearbyChestBlocks = findChestsInRadius(radius, player.getLocation());
        List<DChest> removedChests = new ArrayList<>();
        try (Transaction transaction = DungeonDatabase.db().beginTransaction()) {
            for (BlockState chest : nearbyChestBlocks) {
                DChest chestAt = ChestStorage.computeChestAt(chest.getLocation(), ChestNBT.getLootTable(chest));
                if (chestAt == null) continue;
                chestAt.setGroup(null, transaction);
                removedChests.add(chestAt);
            }
            transaction.commit();
        }
        String header = "Successfully unmarked the following chests:\n";
        String summary = summarizeChests(player.getLocation(), removedChests);
        aqua(player, header + summary);
    }

    @Subcommand("delete")
    @CommandCompletion("@chestGroups [force]|true|false")
    public void deleteLabel(Player player, @Name("group_name") String chestGroupName, @Optional @Name("force") Boolean force) {
        DChestGroup chestGroup = getChestGroup(player, chestGroupName);
        if (chestGroup == null) return;
        if (!force && !chestGroup.getChests().isEmpty()) {
            String header = "Unmark all %d chests in '%s' before deleting:\n".formatted(chestGroup.getChests().size(),
                chestGroup.getName());
            String message = header + summarizeChests(player.getLocation(), chestGroup.getChests());
            red(player, message, chestGroupName);
            return;
        }
        chestGroup.removeAllChests().save();
        chestGroup.delete();
        aqua(player, "Successfully removed chest_group '%s'".formatted(chestGroup.getName()));
    }

    @Subcommand("mark")
    @CommandCompletion("@chestGroups @range:10-100")
    public void label(Player player, @Name("group_name") String chestGroupName, @Name("radius") int radius) {
        if (chestGroupName == null) {
            red(player, "'group_name' is required");
            return;
        }
        DChestGroup chestGroup = ChestGroupStorage.computeChestGroup(chestGroupName);
        Location center = player.getLocation();
        List<BlockState> chests = findChestsInRadius(radius, center);
        try (Transaction transaction = DungeonDatabase.get().getDB().beginTransaction()) {
            for (BlockState chest : chests) {
                chestGroup.addChest(chest.getLocation(), ChestNBT.getLootTable(chest), transaction);
            }
            chestGroup.save();
            transaction.commit();
        }
        String summary = summarizeChests(center, chestGroup.getChests());
        aqua(player, "The following chests are now in group '%s':\n".formatted(chestGroup.getName()) + summary);
    }

    @Subcommand("respawn")
    @CommandCompletion("@range:10-100")
    public void respawn(Player player, @Name("radius") int radius) {
        List<DChest> chests = ChestStorage.findNearbyChests(player.getLocation(), radius);
        Instant now = Instant.now();
        for (DChest chest : chests) {
            ChestRespawnTask.restock(chest, now, true);
        }
        aqua(player, "Successfully restocked %d nearby chests".formatted(chests.size()));
    }

    @Subcommand("passtime")
    @CommandCompletion("[minutes]|@range:1-300")
    public void passTime(CommandSender player, @Optional @Name("minutes") Integer timeToPassArg) {
        int timeToPass = Objects.requireNonNullElse(timeToPassArg, 100000) * 20;
        VoltskiyaPlugin.get().runTaskAsync(() -> new ChestRespawnTask().passTime(timeToPass));
    }

    @Subcommand("respawn_group")
    @CommandCompletion("@chestGroups")
    public void respawn(Player player, @Name("group_name") String chestGroupName) {
        DChestGroup chestGroup = ChestGroupStorage.computeChestGroup(chestGroupName);
        ChestRespawnTask.restockGroup(Instant.now(), chestGroup);
        aqua(player, "Successfully restocked group %s".formatted(chestGroup.getName()));
    }

    @Subcommand("dungeon add")
    @CommandCompletion("@chestGroups @nothing")
    public void addDungeon(Player player, @Name("group_name") String chestGroupName) {
        DChestGroup chestGroup = getChestGroup(player, chestGroupName);
        if (chestGroup == null) return;
        DungeonWand wand = DungeonModule.get().dungeonWand().getWand(player);
        DDungeonSpawner spawner = wand.getSpawner();
        if (spawner == null) {
            red(player, "Select a dungeon spawner before registering it to a chestGroup");
            return;
        }
        chestGroup.setSpawner(spawner);
        chestGroup.save();
        aqua(player, "Successfully set %s.%s to respawn with chest_group %s"
            .formatted(spawner.getDungeon().getName(), spawner.getName(), chestGroup.getName()));
    }

    @Subcommand("config")
    public class Config extends BaseCommand {

        @Subcommand("get")
        @CommandCompletion("@chestGroups @chestConfigKey @chestConfigValue")
        public void get(CommandSender sender,
            @Name("group_name") String chestGroupName,
            @Name("key") String key) {
            DChestGroup chestGroup = getChestGroup(sender, chestGroupName);
            if (chestGroup == null) return;
            if (key == null) throw new CommandException("'key' is required");
            Double value = chestGroup.getConfig().getConfig(key);
            String valueStr = value == null ? "unset" : String.valueOf(value);
            aqua(sender, "[%s] %s: %s", chestGroup.getName(), key, valueStr);
        }

        @Subcommand("unset")
        @CommandCompletion("@chestGroups @chestConfigKey @chestConfigValue")
        public void unset(CommandSender sender,
            @Name("group_name") String chestGroupName,
            @Name("key") String key) {
            DChestGroup chestGroup = getChestGroup(sender, chestGroupName);
            if (chestGroup == null) return;
            if (key == null) throw new CommandException("'key' is required");
            boolean success = chestGroup.getConfig().setConfig(key, null);
            if (!success) red(sender, "'%s' is not a valid key", key);
            aqua(sender, "[%s] %s: %s", chestGroup.getName(), key, "unset");
        }

        @Subcommand("set")
        @CommandCompletion("@chestGroups @chestConfigKey @chestConfigValue")
        public void set(CommandSender sender,
            @Name("group_name") String chestGroupName,
            @Name("key") String key,
            @Name("value") double value) {
            DChestGroup chestGroup = getChestGroup(sender, chestGroupName);
            if (chestGroup == null) return;
            if (key == null) throw new CommandException("'key' is required");
            boolean success = chestGroup.getConfig().setConfig(key, value);
            if (!success) red(sender, "'%s' is not a valid key", key);
            aqua(sender, "[%s] %s: %f", chestGroup.getName(), key, value);
        }
    }

}
