package me.goodroach.movecraftoverheated;

import me.goodroach.movecraftoverheated.commands.CheckHeatCommand;
import me.goodroach.movecraftoverheated.config.Settings;
import me.goodroach.movecraftoverheated.disaster.ExplosionDisaster;
import me.goodroach.movecraftoverheated.listener.BlockListener;
import me.goodroach.movecraftoverheated.listener.CraftListener;
import me.goodroach.movecraftoverheated.listener.WeaponListener;
import me.goodroach.movecraftoverheated.tracking.GraphManager;
import me.goodroach.movecraftoverheated.tracking.WeaponHeatManager;
import me.goodroach.movecraftoverheated.config.OverheatProperties;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

public final class MovecraftOverheated extends JavaPlugin {
    private static MovecraftOverheated instance;
    private WeaponHeatManager heatManager;
    public static NamespacedKey heatKey;
    public static NamespacedKey craftHeatKey;
    public static NamespacedKey dispenserHeatUUID;

    @Override
    public void onEnable() {
        instance = this;
        heatKey = new NamespacedKey(instance, "heat");
        craftHeatKey = new NamespacedKey(instance, "craft_heat");
        dispenserHeatUUID = new NamespacedKey(instance, "dispenser_heat_uuid");

        // TODO: Test, then uncomment
        // Once this works, config parsing needs to be changed (see further down)
        ConfigurationSerialization.registerClass(OverheatProperties.class, "OverheatWeapon");
        ConfigurationSerialization.registerClass(ExplosionDisaster.class, "ExplosionDisaster");

        saveDefaultConfig();

        heatManager = new WeaponHeatManager();
        heatManager.runTaskTimer(MovecraftOverheated.getInstance(), 0L, 1L); // Run every 20 ticks (1 second)

        //Listeners
        getServer().getPluginManager().registerEvents(new WeaponListener(heatManager), this);
        getServer().getPluginManager().registerEvents(new CraftListener(heatManager), this);
        getServer().getPluginManager().registerEvents(new BlockListener(heatManager), this);

        // Plugin startup logic
        initConfig();

        this.getCommand("checkheat").setExecutor(new CheckHeatCommand(heatManager));

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void initConfig() {
        Settings.HeatCheckInterval = getConfig().getInt("HeatCheckInterval", 1000);
        Settings.DisasterCheckInterval = getConfig().getInt("DisasterCheckInterval", 10000);
        if (getConfig().contains("HeatSinkBlocks")) {
            Map<String, Object> tempMap = getConfig().getConfigurationSection("HeatSinkBlocks").getValues(false);
            for(String str : tempMap.keySet()) {
                Settings.HeatSinkBlocks.put(NamespacedKey.fromString(str),(Double)tempMap.get(str));
            }
        }
        if (getConfig().contains("RadiatorBlocks")) {
            Map<String, Object> tempMap = getConfig().getConfigurationSection("RadiatorBlocks").getValues(false);
            for(String str : tempMap.keySet()) {
                Settings.RadiatorBlocks.put(NamespacedKey.fromString(str),(Double)tempMap.get(str));
            }
        }

        if (getConfig().contains("Weapons")) {
            List<OverheatProperties> overheatProperties = (List<OverheatProperties>) getConfig().getList("Weapons");
            overheatProperties.forEach(heatManager::addWeapon);
        }
    }

    public static MovecraftOverheated getInstance() {
        return instance;
    }
}
