package com.voltskiya.structure.dungeon.entity.schematic;

import apple.mc.utilities.data.serialize.EntitySerializable;
import com.voltskiya.structure.dungeon.entity.DDungeon;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
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
            entityTag.getStringList("Tags").add(getSchemTag(schematic));
        });

        ItemMeta meta = egg.getItemMeta();
        meta.displayName(Component.text(schematic.getName()));
        egg.setItemMeta(meta);
        return egg;
    }

    @NotNull
    private static String getSchemTag(DDungeonSchemMob schematic) {
        return TAG_PREFIX + schematic.getName();
    }

    public static DDungeonSchemMob getSchemMob(DDungeon dungeon, String tag) {
        if (!tag.startsWith(TAG_PREFIX)) return null;
        String schem = tag.substring(TAG_PREFIX.length());
        for (DDungeonSchemMob mob : dungeon.getMobTypes()) {
            if (mob.getName().equals(schem)) {
                return mob;
            }
        }
        return null;
    }

    public static void summonSchematic(Location location, DDungeonSchemMob mob) {
        if (mob.getMobs().isEmpty()) return;
        String tag = getSchemTag(mob);
        location.getNearbyEntities(0.5, 0.5, 0.5).forEach((e) -> {
            if (e.getScoreboardTags().contains(tag)) {
                e.remove();
            }
        });
        mob.getMobs().get(0).getEntity().spawn(location, (e) -> {
            if (e instanceof LivingEntity living) {
                living.setAI(false);
            }
            e.addScoreboardTag(tag);
        });
    }

}
