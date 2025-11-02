package me.goodroach.movecraftoverheated.config;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class Settings {
    public static int HeatCheckInterval = 1000;
    public static int DisasterCheckInterval = 10000;
    public static Map<Material, Double> RadiatorBlocks = new HashMap<>();
    public static Map<Material, Double> HeatSinkBlocks = new HashMap<>();
}