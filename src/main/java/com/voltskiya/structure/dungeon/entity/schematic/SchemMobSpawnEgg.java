package com.voltskiya.structure.dungeon.entity.schematic;

import apple.mc.utilities.data.serialize.EntitySerializable;
import com.voltskiya.structure.dungeon.entity.DDungeon;
import com.voltskiya.structure.dungeon.entity.layout.DDungeonLayout;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBTList;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class SchemMobSpawnEgg {

    private static final String TAG_PREFIX = "dungeon.schem.";

    public static ItemStack makeSpawnEgg(DDungeonSchemMob schematic) {
        if (schematic.getMobs().isEmpty()) {
            return null;
        }
        EntitySerializable mobType = schematic.getMobs().get(0).getEntity();
        Material material = Material.BAT_SPAWN_EGG;
        try {
            EntityType entityType = mobType.getEntityType();
            if (entityType != null) {
                String name = entityType.name();
                material = Material.valueOf(name + "_SPAWN_EGG");
            }
        } catch (IllegalArgumentException ignored) {
        }
        ItemStack egg = new ItemStack(material);
        NBT.modify(egg, (nbt) -> {
            ReadWriteNBT entityTag = nbt.getOrCreateCompound("EntityTag");
            EntityType entityType = mobType.getEntityType();
            if (entityType != null) {
                entityTag.setString("id", entityType.getKey().toString());
            }
            entityTag.mergeCompound(mobType.getEntityTag());
            entityTag.setBoolean("NoAI", true);
            ReadWriteNBTList<String> tags = entityTag.getStringList("Tags");
            tags.add(getSchemFullTag(schematic));
            tags.add(getSchemDungeonTag(schematic.getDungeon()));
        });

        ItemMeta meta = egg.getItemMeta();
        meta.displayName(Component.text(schematic.getName()));
        egg.setItemMeta(meta);
        return egg;
    }

    @NotNull
    private static String getSchemFullTag(DDungeonSchemMob schematic) {
        return TAG_PREFIX + "." + schematic.getDungeon().getName() + "." + schematic.getName();
    }

    @NotNull
    private static String getSchemDungeonTag(DDungeon dungeon) {
        return TAG_PREFIX + "." + dungeon.getName();
    }

    public static DDungeonSchemMob getSchemMob(DDungeon dungeon, String tag) {
        String prefix = getSchemDungeonTag(dungeon) + ".";
        if (!tag.startsWith(prefix)) return null;
        String schem = tag.substring(prefix.length());
        for (DDungeonSchemMob mob : dungeon.getMobTypes()) {
            if (mob.getName().equals(schem)) {
                return mob;
            }
        }
        return null;
    }

    public static void killSchematic(DDungeonSchemMob mob) {
        DDungeonLayout layout = mob.getDungeon().getLayout();
        if (layout == null) return;
        Location center = layout.getCenter();
        if (center == null) return;

        String tag = getSchemDungeonTag(mob.getDungeon());
        center.getWorld().getEntities().forEach((e) -> {
            if (e.getScoreboardTags().contains(tag)) {
                e.remove();
            }
        });
    }

    public static void summonSchematic(Location location, DDungeonSchemMob mob) {
        if (mob.getMobs().isEmpty()) return;
        String fullTag = getSchemFullTag(mob);
        String tag = getSchemDungeonTag(mob.getDungeon());
        mob.getMobs().get(0).getEntity().spawn(location, (e) -> {
            if (e instanceof LivingEntity living) {
                living.setAI(false);
            }
            e.setPersistent(true);
            e.addScoreboardTag(tag);
            e.addScoreboardTag(fullTag);
        });
    }

}
