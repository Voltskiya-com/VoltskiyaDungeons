package com.voltskiya.structure.dungeon.entity.spawn;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Embeddable
public class EmbeddedLocation {

    @Column
    protected UUID world;
    @Column
    protected Double x;
    @Column
    protected Double y;
    @Column
    protected Double z;
    @Column
    protected Double xFacing;
    @Column
    protected Double yFacing;
    @Column
    protected Double zFacing;
    private transient Location location;

    public EmbeddedLocation(Location center) {
        this.world = center.getWorld().getUID();
        this.x = center.getX();
        this.y = center.getY();
        this.z = center.getZ();
        Vector facing = center.getDirection();
        this.xFacing = facing.getX();
        this.yFacing = facing.getY();
        this.zFacing = facing.getZ();
    }

    @Nullable
    public Location toLocation() {
        if (this.location != null) return this.location.clone();
        World world = getWorld();
        if (world == null) return null;
        this.location = new Location(world, this.x, this.y, this.z)
            .setDirection(new Vector(this.xFacing, this.yFacing, this.zFacing));
        return this.location.clone();
    }

    @Nullable
    private World getWorld() {
        if (world == null) return null;
        return Bukkit.getWorld(this.world);
    }

    public void addLocation(Location addLocation) {
        this.x += addLocation.getX();
        this.y += addLocation.getY();
        this.z += addLocation.getZ();
        this.world = addLocation.getWorld().getUID();
        this.location = null;
    }
}
