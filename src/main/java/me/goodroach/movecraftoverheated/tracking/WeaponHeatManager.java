package me.goodroach.movecraftoverheated.tracking;

import me.goodroach.movecraftoverheated.weapons.Weapon;
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

public class WeaponHeatManager extends BukkitRunnable implements Listener {
    private final GraphManager graphManager;
    private Map<Material, DispenserGraph> weapons = new HashMap<>();
    // TODO: Are synchronized maps necessary? Are we ever accessing this async?
    private final Map<UUID, DispenserWeapon> trackedDispensers = new ConcurrentHashMap();
    private final Map<Location, DispenserWeapon> location2Dispenser = Collections.synchronizedMap(new WeakHashMap<>());

    public WeaponHeatManager(GraphManager graphManager) {
        this.graphManager = graphManager;
    }

    @Override
    public void run() {
        // Update location to weapon cache first!
        location2Dispenser.clear();
        trackedDispensers.values().forEach(dispenserWeapon -> location2Dispenser.put(dispenserWeapon.getLocation(), dispenserWeapon));
        long time = System.currentTimeMillis();
        for (DispenserGraph graph : weapons.values()) {
            coolDispensers(graph.getWeapon(), graph);

            List<List<DispenserWeapon>> dispenserForest = graphManager.getForest(graph);

            setHeatFromForest(dispenserForest, graph.getWeapon());
            graph.clear();
        }
    }

    @Nullable
    public DispenserWeapon getByLocation(Location location) {
        DispenserWeapon result = location2Dispenser.getOrDefault(location, null);
        if (result == null) {
            for (DispenserWeapon dispenserWeapon : trackedDispensers.values()) {
                if (dispenserWeapon.getLocation().equals(location)) {
                    result = dispenserWeapon;
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
        // TODO: Is this necessary?
        DispenserWeapon existingWeapon = trackedDispensers.get(dispenserWeapon.getUuid());
        boolean unknown = existingWeapon == null;
        if (existingWeapon != null) {
            dispenserWeapon = existingWeapon; // Reuse the existing dispenserWeapon object
        }

        Block dispenser = dispenserWeapon.getLocation().getBlock();
        TileState state = (TileState) dispenser.getState();
        PersistentDataContainer dataContainer = state.getPersistentDataContainer();

        // This resets the dispenser's tile state if the plugin did not track it due to a crash or a bug.
        if (!trackedDispensers.containsValue(dispenserWeapon)) {
            //dataContainer.remove(heatKey);
            dataContainer.remove(dispenserHeatUUID);
            dataContainer.set(dispenserHeatUUID, PersistentDataType.STRING, dispenserWeapon.getUuid().toString());
            state.update();
        }

        int currentAmount = dispenserWeapon.getHeat();
        currentAmount += amount;
        dispenserWeapon.setHeat(currentAmount);

        // Cleans the data container and the list of tracked dispensers.
        if (amount <= 0) {
            // TODO: Sure? The dispenserGraph will still hold the reference to it...
            trackedDispensers.remove(dispenserWeapon.getUuid());
            //dataContainer.remove(heatKey);
            dataContainer.remove(dispenserHeatUUID);
            state.update();
        } else if(unknown) {
            trackedDispensers.put(dispenserWeapon.getUuid(), dispenserWeapon);
        }
    }


    private void checkDisaster(Weapon weapon) {
    }

    private void coolDispensers(Weapon weapon, DispenserGraph graph) {
        // TODO: Only cool the dispensers associated with this weapon, currently it just cools all tracked dispensers...
        if (trackedDispensers.isEmpty()) {
            return;
        }

        // TODO: This does not seem to be correct at all! This will just cool ALL dispensers and not just the ones for this weapon...
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
