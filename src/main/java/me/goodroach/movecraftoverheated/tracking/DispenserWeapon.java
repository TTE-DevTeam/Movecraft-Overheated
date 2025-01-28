package me.goodroach.movecraftoverheated.tracking;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.TrackedLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.SubCraft;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static me.goodroach.movecraftoverheated.MovecraftOverheated.craftHeatKey;

public class DispenserWeapon {
    private final Vector vector;
    private final Location absolute;
    private final UUID uuid;
    private WeakReference<TrackedLocation> tracked = new WeakReference<>(null);
    private WeakReference<Craft> craft = new WeakReference<>(null);
    private int heatValue;

    public DispenserWeapon(Vector vector, Location absolute) {
        this.vector = vector;
        this.absolute = absolute;
        uuid = UUID.randomUUID();
    }

    public int getHeat() {
        return this.heatValue;
    }

    public void setHeat(int value) {
        this.heatValue = value;
    }

    public Vector getVector() {
        return vector;
    }

    // when unbinding from a craft, update the absolute location!
    public void unbindFromCraft(@NotNull Craft craft) {
        if (this.getTrackedLocation() == null) {
            // technically, this is an error case...
            return;
        }
        if (this.getCraft() == craft || (craft instanceof SubCraft subCraft && subCraft.getParent() == this.getCraft())) {
            Location tracked = getTrackedLocation().getAbsoluteLocation().toBukkit(getCraft().getWorld());
            if (!tracked.equals(this.absolute)) {
                this.absolute.setWorld(tracked.getWorld());
                this.absolute.set(tracked.getX(), tracked.getY(), tracked.getZ());
            }
        }
    }

    public boolean bindToCraft(@Nullable Craft craft) {
        if (craft == null) {
            craft = MathUtils.getCraftByPersistentBlockData(absolute);
        }
        if (craft == null) {
            return false;
        }

        Craft alreadyKnownCraft = getCraft();

        // we are already bound to this craft!
        if (alreadyKnownCraft != null && alreadyKnownCraft.getUUID().equals(craft.getUUID()) && this.getTrackedLocation() != null) {
            return true;
        }

        // Movecraft itself shoudl do the transition stuff! We should not care about it at all!
        if (craft instanceof SubCraft subCraft && alreadyKnownCraft == subCraft.getParent() && subCraft.getParent() != null) {
            // We only need to update our craft reference, movecraft itself cares about updating the tracked location!
            this.craft = new WeakReference<>(craft);
            return true;
        }

        this.craft = new WeakReference<>(craft);

        Set<TrackedLocation> trackedLocationSet = craft.getTrackedLocations().computeIfAbsent(craftHeatKey, key -> new HashSet<>());
        MovecraftLocation absoluteMovecraft = MathUtils.bukkit2MovecraftLoc(absolute);
        TrackedLocation trackedLocation = null;
        // TODO: Test performance! Possibly move this to a async task...
        for (TrackedLocation known : trackedLocationSet) {
            if (known.getAbsoluteLocation().equals(absoluteMovecraft)) {
                trackedLocation = known;
                break;
            }
        }
        if (trackedLocation == null) {
            trackedLocation = new TrackedLocation(craft, absoluteMovecraft);
            if (trackedLocationSet.add(trackedLocation)) {
                this.tracked = new WeakReference<>(trackedLocation);
                return true;
            } else {
                return false;
            }
        } else {
            this.tracked = new WeakReference<>(trackedLocation);
        }

        return true;
    }

    public Location getLocation() {
        if (getTrackedLocation() != null && getCraft() != null) {
            Location tracked = getTrackedLocation().getAbsoluteLocation().toBukkit(getCraft().getWorld());
            return tracked;
        } else {
            return this.absolute;
        }
    }

    @Nullable
    protected TrackedLocation getTrackedLocation() {
        return this.tracked.get();
    }

    @Nullable
    public Craft getCraft() {
        return this.craft.get();
    }

    public UUID getUuid() {
        return uuid;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DispenserWeapon that = (DispenserWeapon) obj;
        return this.getLocation().equals(that.getLocation()) && this.getVector().equals(that.getVector());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLocation(), getVector());
    }
}
