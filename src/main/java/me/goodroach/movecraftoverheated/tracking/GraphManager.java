package me.goodroach.movecraftoverheated.tracking;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class GraphManager {
    public GraphManager() {

    }

    // Perform BFS and return a list of connected components (forest)
    public List<List<DispenserLocation>> getForest(DispenserGraph graph) {
        graph.makeEdges();
        Map<DispenserLocation, List<DispenserLocation>> adjList = graph.getAdjList();

        Set<DispenserLocation> visited = new HashSet<>();
        List<List<DispenserLocation>> forest = new ArrayList<>();

        // Traverse each node in the adjacency list
        for (DispenserLocation dispenser : adjList.keySet()) {
            if (!visited.contains(dispenser)) {
                List<DispenserLocation> connectedComponent = bfs(dispenser, visited, adjList);
                forest.add(connectedComponent);
            }
        }

        return forest;
    }

    // BFS algorithm to explore the graph and return the connected component
    private List<DispenserLocation> bfs(
        DispenserLocation start,
        Set<DispenserLocation> visited,
        Map<DispenserLocation, List<DispenserLocation>> adjList
    ) {
        List<DispenserLocation> component = new ArrayList<>();
        Queue<DispenserLocation> queue = new LinkedList<>();
        queue.offer(start);
        visited.add(start);

        // Perform BFS
        while (!queue.isEmpty()) {
            DispenserLocation current = queue.poll();
            component.add(current);

            // Traverse neighbors of the current node
            for (DispenserLocation neighbor : adjList.get(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }

        return component;
    }
}

