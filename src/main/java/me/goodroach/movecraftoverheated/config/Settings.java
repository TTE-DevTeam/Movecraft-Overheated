package me.goodroach.movecraftoverheated.config;

import org.bukkit.NamespacedKey;

import java.util.HashMap;
import java.util.Map;

public class Settings {
    public static int HeatCheckInterval = 1000;
    public static int DisasterCheckInterval = 10000;
    public static double CooldownPerTick = 1.0D;
    public static Map<NamespacedKey, Double> RadiatorBlocks = new HashMap<>();
    public static Map<NamespacedKey, Double> HeatSinkBlocks = new HashMap<>();
}