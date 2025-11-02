package me.goodroach.movecraftoverheated.tracking;

import me.goodroach.movecraftoverheated.config.OverheatProperties;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static me.goodroach.movecraftoverheated.MovecraftOverheated.dispenserHeatUUID;

public class WeaponHeatManager extends BukkitRunnable implements Listener {
    private Map<NamespacedKey, DispenserGraph> weapons = new HashMap<>();
    // TODO: Are synchronized maps necessary? Are we ever accessing this async?
    private final Map<UUID, DispenserHeatData> trackedDispensers = new ConcurrentHashMap();
    private final Map<Location, DispenserHeatData> location2Dispenser = Collections.synchronizedMap(new WeakHashMap<>());

    public WeaponHeatManager() {
    }

    @Override
    public void run() {
        // Update location to weapon cache first!
        location2Dispenser.clear();
        trackedDispensers.values().forEach(dispenserWeapon -> location2Dispenser.put(dispenserWeapon.getLocation(), dispenserWeapon));
        long time = System.currentTimeMillis();
        for (DispenserGraph graph : weapons.values()) {
            List<List<DispenserHeatData>> dispenserForest = GraphManager.getForest(graph);

            setHeatFromForest(dispenserForest, graph.getWeapon());
            graph.clear();
        }
    }

    @Nullable
    public DispenserHeatData getByLocation(Location location) {
        DispenserHeatData result = location2Dispenser.getOrDefault(location, null);
        if (result == null) {
            for (DispenserHeatData dispenserHeatData : trackedDispensers.values()) {
                if (dispenserHeatData.getLocation().equals(location)) {
                    result = dispenserHeatData;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Adds heat to a given dispenser weapon.
     * <p>
     * This method updates the persistent data container of the dispenser's block to track the heat value.
     * It ensures that the dispenser's heat does not go below zero and removes the dispenser from tracking
     * if the heat is reset to zero. It also handles scenarios where the dispenser was not properly tracked
     * due to plugin crashes or bugs.
     * </p>
     *
     * @param dispenserHeatData The dispenser weapon whose heat is being set. This should not be {@code null}.
     * @param amount The heat amount to set for the dispenser. A value less than or equal to zero will reset
     *               the heat and remove the dispenser from tracking.
     * @throws IllegalArgumentException If the dispenser weapon's block is not of type {@link Material#DISPENSER}.
     */
    public void addDispenserHeat(DispenserHeatData dispenserHeatData, int amount) {
        if (dispenserHeatData == null) {
            throw new IllegalArgumentException("DispenserWeapon cannot be null");
        }

        // Check if the dispenserWeapon is already in the map, and if so, get the existing one
        // TODO: Is this necessary?
        DispenserHeatData existingWeapon = trackedDispensers.get(dispenserHeatData.getUuid());
        boolean unknown = existingWeapon == null;
        if (existingWeapon != null) {
            dispenserHeatData = existingWeapon; // Reuse the existing dispenserWeapon object
        }

        Block dispenser = dispenserHeatData.getLocation().getBlock();
        TileState state = (TileState) dispenser.getState();
        PersistentDataContainer dataContainer = state.getPersistentDataContainer();

        // This resets the dispenser's tile state if the plugin did not track it due to a crash or a bug.
        if (!trackedDispensers.containsValue(dispenserHeatData)) {
            //dataContainer.remove(heatKey);
            dataContainer.remove(dispenserHeatUUID);
            dataContainer.set(dispenserHeatUUID, PersistentDataType.STRING, dispenserHeatData.getUuid().toString());
            state.update();
        }

        int currentAmount = dispenserHeatData.getHeat();
        currentAmount += amount;
        dispenserHeatData.setHeat(currentAmount);

        // Cleans the data container and the list of tracked dispensers.
        if (amount <= 0) {
            // TODO: Sure? The dispenserGraph will still hold the reference to it...
            trackedDispensers.remove(dispenserHeatData.getUuid());
            //dataContainer.remove(heatKey);
            dataContainer.remove(dispenserHeatUUID);
            state.update();
        } else if(unknown) {
            trackedDispensers.put(dispenserHeatData.getUuid(), dispenserHeatData);
        }
    }


    private void checkDisaster(OverheatProperties overheatProperties) {
    }

    public void removeDispenser(Location location) {

    }

    public void removeDispenser(UUID dispenserUUID) {
        if (dispenserUUID == null) {
            return;
        }
        DispenserHeatData dispenserHeatData = this.trackedDispensers.getOrDefault(dispenserUUID, null);
        if (dispenserHeatData == null) {
            return;
        } else {
            this.weapons.values().forEach(dispenserGraph -> {
                dispenserGraph.removeDispenser(dispenserHeatData);
            });
            this.trackedDispensers.remove(dispenserUUID);
        }
    }

    private void setHeatFromForest(List<List<DispenserHeatData>> forest, OverheatProperties overheatProperties) {
        for (List<DispenserHeatData> dispenserTree : forest) {
            for (DispenserHeatData dispenser : dispenserTree) {
                addDispenserHeat(dispenser, dispenserTree.size() * overheatProperties.heatRate());
            }
        }
    }

    public Map<NamespacedKey, DispenserGraph> getWeapons() {
        return weapons;
    }

    public void addWeapon(OverheatProperties overheatProperties) {
        weapons.put(overheatProperties.itemID(), new DispenserGraph(overheatProperties));
    }

    public Map<UUID, DispenserHeatData> getTrackedDispensers() {
        return trackedDispensers;
    }
}
