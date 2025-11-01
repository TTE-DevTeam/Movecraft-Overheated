package me.goodroach.movecraftoverheated.listener;

import me.goodroach.movecraftoverheated.tracking.DispenserGraph;
import me.goodroach.movecraftoverheated.tracking.DispenserLocation;
import me.goodroach.movecraftoverheated.tracking.WeaponHeatManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.UUID;

import static me.goodroach.movecraftoverheated.MovecraftOverheated.dispenserHeatUUID;
public class WeaponListener implements Listener {
    private WeaponHeatManager heatManager;

    public WeaponListener(WeaponHeatManager heatManager) {
        this.heatManager = heatManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDispense(BlockDispenseEvent event) {
        ItemStack item = event.getItem();

        DispenserGraph graph = heatManager.getWeapons().get(item.getType());
        if (graph == null) {
            return;
        }

        Block block = event.getBlock();
        if (block.getType() != Material.DISPENSER) {
            return;
        }

        Block facingBlock = block.getRelative(((Dispenser) block.getBlockData()).getFacing());
        Vector nodeLoc = facingBlock.getLocation().toVector();

        TileState state = (TileState) block.getState();

        DispenserLocation dispenserLocation = heatManager.getByLocation(block.getLocation());
        if (dispenserLocation == null) {
            PersistentDataContainer container = state.getPersistentDataContainer();
            //TODO: Denest this later
            if (container.has(dispenserHeatUUID)) {
                UUID uuid = UUID.fromString(container.get(dispenserHeatUUID, PersistentDataType.STRING));
                if (heatManager.getTrackedDispensers().containsKey(uuid)) {
                    dispenserLocation = heatManager.getTrackedDispensers().get(uuid);
                    dispenserLocation.setWeapon(graph.getWeapon());
                } else {
                    dispenserLocation = new DispenserLocation(nodeLoc, block.getLocation());
                    container.set(dispenserHeatUUID, PersistentDataType.STRING, dispenserLocation.getUuid().toString());
                    state.update();
                }
            } else {
                dispenserLocation = new DispenserLocation(nodeLoc, block.getLocation());
                container.set(dispenserHeatUUID, PersistentDataType.STRING, dispenserLocation.getUuid().toString());
                state.update();
            }
        }

        dispenserLocation.bindToCraft(null);
        graph.addDispenser(dispenserLocation);
    }
}
