package me.goodroach.movecraftoverheated;

import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BFSUtils {
    public static List<List<Block>> getConnectedDispensers(byte[][] directions, HashMap<Block, Vector> nodes) {
        byte[][] adjMatrix = makeAdjacencyMatrix(directions, nodes);
        int n = adjMatrix.length; // Number of nodes
        boolean[] visited = new boolean[n];
        List<List<Block>> forest = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                List<Block> connectedGraph = bfs(adjMatrix, i, visited, new ArrayList<>(nodes.keySet()));
                forest.add(connectedGraph);
            }
        }

        return forest;
    }

    private static byte[][] makeAdjacencyMatrix(byte[][] directions, HashMap<Block, Vector> nodes) {
        int size = nodes.size();

        Vector current;
        Vector next;
        byte[][] adjMatrix = new byte[size][size];
        List<Block> dispensers = new ArrayList<>(nodes.keySet());
        
        for (int i = 0; i < size; i++) {
            current = nodes.get(dispensers.get(i));

            for (byte[] dir : directions) {
                next = current.clone().add(new Vector(dir[0], dir[1], dir[2]));

                for (int j = 0; j < size; j++) {
                    if (nodes.get(dispensers.get(j)).equals(next)) {
                        adjMatrix[i][j] += 1;
                    }
                }
            }
        }

        printAdjacencyMatrix(adjMatrix);
        return adjMatrix;
    }

    // Note: for internal testing purposes.
    private static void printAdjacencyMatrix(byte[][] adjMatrix) {
        System.out.println("Adjacency Matrix:");
        for (int i = 0; i < adjMatrix.length; i++) {
            for (int j = 0; j < adjMatrix[i].length; j++) {
                System.out.print(adjMatrix[i][j] + " ");
            }
            System.out.println(); // Move to the next row
        }
    }

    private static List<Block> bfs(byte[][] adjMatrix, int start, boolean[] visited, List<Block> dispensers) {
        List<Block> component = new ArrayList<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(start);
        visited[start] = true;

        while (!queue.isEmpty()) {
            int node = queue.poll();
            component.add(dispensers.get(node));

            for (int i = 0; i < adjMatrix.length; i++) {
                if (adjMatrix[node][i] == 1 && !visited[i]) {
                    queue.offer(i);
                    visited[i] = true;
                }
            }
        }

        return component;
    }
}
