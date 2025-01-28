package me.goodroach.movecraftoverheated.listener;

import me.goodroach.movecraftoverheated.tracking.DispenserLocation;
import me.goodroach.movecraftoverheated.tracking.WeaponHeatManager;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.SubCraft;
import net.countercraft.movecraft.events.CraftDetectEvent;
import net.countercraft.movecraft.events.CraftPilotEvent;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class CraftListener implements Listener {
    private final WeaponHeatManager heatManager;

    public CraftListener(WeaponHeatManager heatManager) {
        this.heatManager = heatManager;
    }

    @EventHandler
    public void onCraftPilot(CraftDetectEvent event) {
        // TODO: Add references to the dispenser weapons to the craft via datatags (weak-set)
        for (DispenserLocation dispenser : heatManager.getTrackedDispensers().values()) {
            // Only care about those within the move box, otherwise this might be overkill
            // TODO: Also check if that dispenser will be part of the craft!
            if (event.getCraft().getHitBox().inBounds(dispenser.getLocation().getX(), dispenser.getLocation().getY(), dispenser.getLocation().getZ())) {
                dispenser.bindToCraft(event.getCraft());
            }
        }
    }

    // Copy from sign change code in movecraft code
    @EventHandler
    public void onCraftPilot(@NotNull CraftPilotEvent event) {
        // Walk through all signs and set a UUID in there
        final Craft craft = event.getCraft();

        // Now, find all signs on the craft...
        for (MovecraftLocation mLoc : craft.getHitBox()) {
            Block block = mLoc.toBukkit(craft.getWorld()).getBlock();
            // Only interested in signs, if no sign => continue
            // Edit: That's useful for dispensers too to flag TNT and the like, but for that one could use a separate listener
            if (!(block.getState() instanceof Dispenser))
                continue;
            // Sign located!
            Dispenser tile = (Dispenser) block.getState();

            craft.markTileStateWithUUID(tile);
            tile.update();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraftRelease(CraftReleaseEvent event) {
        // Walk through all signs and set a UUID in there
        // TODO: For subcrafts, restore the parents UUID if it is within the parent
        final Craft craft = event.getCraft();

        // Now, find all signs on the craft...
        for (MovecraftLocation mLoc : craft.getHitBox()) {
            Block block = mLoc.toBukkit(craft.getWorld()).getBlock();
            // Only interested in signs, if no sign => continue
            if (!(block.getState() instanceof Dispenser))
                continue;
            // Sign located!
            Dispenser tile = (Dispenser) block.getState();

            craft.removeUUIDMarkFromTile(tile);

            tile.update();
        }

        // Update the absolute location when a craft is released
        // TODO: Make this prettier, this is not pretty
        for (DispenserLocation dispenser : heatManager.getTrackedDispensers().values()) {
            // Only care about those within the move box, otherwise this might be overkill
            if (event.getCraft().getHitBox().inBounds(dispenser.getLocation().getX(), dispenser.getLocation().getY(), dispenser.getLocation().getZ())) {
                dispenser.unbindFromCraft(event.getCraft());
                if (event.getCraft() instanceof SubCraft subCraft && subCraft.getParent() != null) {
                    Craft parent = subCraft.getParent();
                    if (parent.getHitBox().inBounds(dispenser.getLocation().getX(), dispenser.getLocation().getY(), dispenser.getLocation().getZ())) {
                        dispenser.bindToCraft(parent);
                    }
                }
            }
        }
    }

}
