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
    public List<List<DispenserHeatData>> getForest(DispenserGraph graph) {
        graph.makeEdges();
        Map<DispenserHeatData, List<DispenserHeatData>> adjList = graph.getAdjList();

        Set<DispenserHeatData> visited = new HashSet<>();
        List<List<DispenserHeatData>> forest = new ArrayList<>();

        // Traverse each node in the adjacency list
        for (DispenserHeatData dispenser : adjList.keySet()) {
            if (!visited.contains(dispenser)) {
                List<DispenserHeatData> connectedComponent = bfs(dispenser, visited, adjList);
                forest.add(connectedComponent);
            }
        }

        return forest;
    }

    // BFS algorithm to explore the graph and return the connected component
    private List<DispenserHeatData> bfs(
        DispenserHeatData start,
        Set<DispenserHeatData> visited,
        Map<DispenserHeatData, List<DispenserHeatData>> adjList
    ) {
        List<DispenserHeatData> component = new ArrayList<>();
        Queue<DispenserHeatData> queue = new LinkedList<>();
        queue.offer(start);
        visited.add(start);

        // Perform BFS
        while (!queue.isEmpty()) {
            DispenserHeatData current = queue.poll();
            component.add(current);

            // Traverse neighbors of the current node
            for (DispenserHeatData neighbor : adjList.get(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }

        return component;
    }
}

