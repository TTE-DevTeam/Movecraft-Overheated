package me.goodroach.movecraftoverheated.tracking;

import me.goodroach.movecraftoverheated.weapons.Weapon;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DispenserGraph {
    // Use concurrent for optimizations later
    // Represents the actual graph => Represents a dispenser with it's neighbours using the directions of the weapon
    private Map<DispenserLocation, List<DispenserLocation>> adjList = new ConcurrentHashMap<>();
    private final Weapon weapon;

    public DispenserGraph(Weapon weapon) {
        this.weapon = weapon;
    }

    public Map<DispenserLocation, List<DispenserLocation>> getAdjList() {
        return adjList;
    }

    public void addDispenser(DispenserLocation dispenserLocation) {
        // Use putIfAbsent, otherwise you will reset the entire thing everytime!
        adjList.putIfAbsent(dispenserLocation, new ArrayList<>());
    }

    public void makeEdges() {
        Vector current;
        Vector next;

        for (DispenserLocation dispenser1 : adjList.keySet()) {
            current = dispenser1.getVector();
            for (byte[] dir : weapon.directions()) {
                next = current.clone().add(new Vector(dir[0], dir[1], dir[2]));
                // TODO: Implement equals method in DispenserLocation so it can match with a vector with the same coords
                for (DispenserLocation dispenser2 : adjList.keySet()) {
                    if (dispenser2.getVector().equals(next)) {
                        adjList.computeIfAbsent(dispenser1, k -> new ArrayList<>()).add(dispenser2);
                    }
                }
            }
        }
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public void clear() {
        adjList.clear();
    }

    public void removeDispenser(DispenserLocation dispenserLocation) {
        this.adjList.values().remove(dispenserLocation);
        this.adjList.remove(dispenserLocation);
    }
}
