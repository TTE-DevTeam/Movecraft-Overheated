package me.goodroach.movecraftoverheated.tracking;

import me.goodroach.movecraftoverheated.weapons.Weapon;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Location;
import org.bukkit.Material;
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
import static me.goodroach.movecraftoverheated.MovecraftOverheated.heatKey;

public class WeaponHeatManager extends BukkitRunnable implements Listener {
    private final GraphManager graphManager;
    private Map<Material, DispenserGraph> weapons = new HashMap<>();
    private final Map<UUID, DispenserWeapon> trackedDispensers = new ConcurrentHashMap();

    public WeaponHeatManager(GraphManager graphManager) {
        this.graphManager = graphManager;
    }

    @Override
    public void run() {
        long time = System.currentTimeMillis();
        for (DispenserGraph graph : weapons.values()) {
            coolDispensers(graph.getWeapon());

            List<List<DispenserWeapon>> dispenserForest = graphManager.getForest(graph);

            setHeatFromForest(dispenserForest, graph.getWeapon());
            graph.clear();
        }
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
     * @param dispenserWeapon The dispenser weapon whose heat is being set. This should not be {@code null}.
     * @param amount The heat amount to set for the dispenser. A value less than or equal to zero will reset
     *               the heat and remove the dispenser from tracking.
     * @throws IllegalArgumentException If the dispenser weapon's block is not of type {@link Material#DISPENSER}.
     */
    public void addDispenserHeat(DispenserWeapon dispenserWeapon, int amount) {
        if (dispenserWeapon == null) {
            throw new IllegalArgumentException("DispenserWeapon cannot be null");
        }

        // Check if the dispenserWeapon is already in the map, and if so, get the existing one
        DispenserWeapon existingWeapon = trackedDispensers.get(dispenserWeapon);
        if (existingWeapon != null) {
            dispenserWeapon = existingWeapon; // Reuse the existing dispenserWeapon object
        }

        Block dispenser = dispenserWeapon.getLocation().getBlock();
        TileState state = (TileState) dispenser.getState();
        PersistentDataContainer dataContainer = state.getPersistentDataContainer();

        // This resets the dispenser's tile state if the plugin did not track it due to a crash or a bug.
        if (!trackedDispensers.containsValue(dispenserWeapon)) {
            dataContainer.remove(heatKey);
            dataContainer.remove(dispenserHeatUUID);
            dataContainer.set(dispenserHeatUUID, PersistentDataType.STRING, dispenserWeapon.getUuid().toString());
        }

        int currentAmount = dataContainer.getOrDefault(heatKey, PersistentDataType.INTEGER, 0);
        amount += currentAmount;

        // Cleans the data container and the list of tracked dispensers.
        if (amount <= 0) {
            trackedDispensers.remove(dispenserWeapon.getUuid());
            dataContainer.remove(heatKey);
            dataContainer.remove(dispenserHeatUUID);
        } else {
            dataContainer.set(heatKey, PersistentDataType.INTEGER, amount);
            trackedDispensers.put(dispenserWeapon.getUuid(), dispenserWeapon);
        }

        // Dont forget to save!
        state.update();
    }


    private void checkDisaster(Weapon weapon) {
    }

    private void coolDispensers(Weapon weapon) {
        if (trackedDispensers.isEmpty()) {
            return;
        }

        for (DispenserWeapon dispenser : trackedDispensers.values()) {
            // Negative value as it is removing heat
            addDispenserHeat(dispenser, -1 * weapon.heatDissipation());
        }
    }

    public void removeDispenser(Location location) {

    }

    public void removeDispenser(UUID dispenserUUID) {
        if (dispenserUUID == null) {
            return;
        }
        DispenserWeapon dispenserWeapon = this.trackedDispensers.getOrDefault(dispenserUUID, null);
        if (dispenserWeapon == null) {
            return;
        } else {
            this.weapons.values().forEach(dispenserGraph -> {
                dispenserGraph.removeDispenser(dispenserWeapon);
            });
            this.trackedDispensers.remove(dispenserUUID);
        }
    }

    private void setHeatFromForest(List<List<DispenserWeapon>> forest, Weapon weapon) {
        for (List<DispenserWeapon> dispenserTree : forest) {
            for (DispenserWeapon dispenser : dispenserTree) {
                addDispenserHeat(dispenser, dispenserTree.size() * weapon.heatRate());
            }
        }
    }

    public Map<Material, DispenserGraph> getWeapons() {
        return weapons;
    }

    public void addWeapon(Weapon weapon) {
        weapons.put(weapon.material(), new DispenserGraph(weapon));
    }

    public Map<UUID, DispenserWeapon> getTrackedDispensers() {
        return trackedDispensers;
    }
}
