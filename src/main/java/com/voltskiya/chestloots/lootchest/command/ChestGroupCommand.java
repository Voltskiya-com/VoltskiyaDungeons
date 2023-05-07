package com.voltskiya.chestloots.lootchest.command;

import static apple.mc.utilities.world.vector.VectorUtils.distance;

import apple.mc.utilities.player.chat.SendMessage;
import com.voltskiya.chestloots.VoltskiyaPlugin;
import com.voltskiya.chestloots.lootchest.entity.chest.ChestStorage;
import com.voltskiya.chestloots.lootchest.entity.chest.DChest;
import com.voltskiya.chestloots.lootchest.entity.group.ChestGroupStorage;
import com.voltskiya.chestloots.lootchest.entity.group.DChestGroup;
import com.voltskiya.chestloots.lootchest.util.ChestNBT;
import com.voltskiya.lib.acf.BaseCommand;
import com.voltskiya.lib.acf.annotation.CommandAlias;
import com.voltskiya.lib.acf.annotation.CommandCompletion;
import com.voltskiya.lib.acf.annotation.CommandPermission;
import com.voltskiya.lib.acf.annotation.Name;
import com.voltskiya.lib.acf.annotation.Subcommand;
import io.ebean.DB;
import io.ebean.Transaction;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;

@CommandAlias("chest_group")
@CommandPermission("volt.gm.loot.chest")
public class ChestGroupCommand extends BaseCommand {

    public ChestGroupCommand() {
        VoltskiyaPlugin.get().registerCommand(this);
        VoltskiyaPlugin.get().getCommandManager()
            .getCommandCompletions()
            .registerAsyncCompletion("chestGroups", (c) -> ChestGroupStorage.listGroupNames());
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

    @Subcommand("config")
    @CommandCompletion("@chestGroups")
    public void config() {
        throw new NotImplementedException("");
    }

    @Subcommand("unmark")
    @CommandCompletion("@range:10-100")
    public void unmark(Player player, @Name("radius") int radius) {
        List<BlockState> chests = findChestsInRadius(radius, player.getLocation());
        try (Transaction transaction = DB.beginTransaction()) {
            for (BlockState chest : chests) {
                DChest chestAt = ChestStorage.computeChestAt(chest.getLocation(), ChestNBT.getLootTable(chest));
                if (chestAt == null) continue;
                chestAt.setGroup(null, transaction);
            }
            transaction.commit();
        }
        //todo
    }

    @CommandCompletion("@chestGroups")
    public void deleteLabel() {
        throw new NotImplementedException("");
    }


    @Subcommand("mark")
    @CommandCompletion("@range:10-100 @chestGroups")
    public void label(Player player, @Name("radius") int radius, @Name("group_name") String chestGroupName) {
        if (chestGroupName == null) {
            SendMessage.get().red(player, "'group_name' is required");
            return;
        }
        DChestGroup chestGroup = ChestGroupStorage.computeChestGroup(chestGroupName);
        Location center = player.getLocation();
        try (Transaction transaction = DB.beginTransaction()) {
            List<BlockState> chests = findChestsInRadius(radius, center);
            for (BlockState chest : chests) {
                chestGroup.addChest(chest.getLocation(), ChestNBT.getLootTable(chest), transaction);
            }
            chestGroup.save(transaction);
            transaction.commit();
        }

        List<DChest> chests = chestGroup.getChests();
        chests.sort(Comparator.comparingDouble(chest -> distance(chest.getLocation(), center)));
        List<String> chestSummaries = new ArrayList<>(chests.size());
        for (DChest chest : chests) {
            Location location = chest.getLocation();
            Block block = location.getBlock();
            if (!(block.getState() instanceof Container)) continue;
            String chestSummary = "%s [%s] {%d %d %d} - distance: %.2f ".formatted(
                block.getType(),
                chest.getLootTable(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                distance(center, location));
            chestSummaries.add(chestSummary);

        }
        String summary =
            "The following chests are now in group '%s'\n".formatted(chestGroupName) + String.join("\n", chestSummaries);
        SendMessage.get().aqua(player, summary);
    }

}
