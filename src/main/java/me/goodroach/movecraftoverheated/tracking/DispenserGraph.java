package me.goodroach.movecraftoverheated.tracking;

import me.goodroach.movecraftoverheated.weapons.Weapon;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DispenserGraph {
    // Use concurrent for optimizations later
    private Map<DispenserWeapon, List<DispenserWeapon>> adjList = new ConcurrentHashMap<>();
    private final Weapon weapon;

    public DispenserGraph(Weapon weapon) {
        this.weapon = weapon;
    }

    public Map<DispenserWeapon, List<DispenserWeapon>> getAdjList() {
        return adjList;
    }

    public void addDispenser(DispenserWeapon dispenserWeapon) {
        // Use putIfAbsent, otherwise you will reset the entire thing everytime!
        adjList.putIfAbsent(dispenserWeapon, new ArrayList<>());
    }

    public void makeEdges() {
        Vector current;
        Vector next;

        for (DispenserWeapon dispenser1 : adjList.keySet()) {
            current = dispenser1.getVector();
            for (byte[] dir : weapon.directions()) {
                next = current.clone().add(new Vector(dir[0], dir[1], dir[2]));
                for (DispenserWeapon dispenser2 : adjList.keySet()) {
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

    public void removeDispenser(DispenserWeapon dispenserWeapon) {
        this.adjList.values().remove(dispenserWeapon);
        this.adjList.remove(dispenserWeapon);
    }
}
