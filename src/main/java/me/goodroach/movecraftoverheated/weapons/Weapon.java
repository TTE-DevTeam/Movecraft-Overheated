package me.goodroach.movecraftoverheated.weapons;

import me.goodroach.movecraftoverheated.disaster.BaseDisaster;
import me.goodroach.movecraftoverheated.disaster.ExplosionDisaster;
import me.goodroach.movecraftoverheated.util.SerializationUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record Weapon(
    Material material,
    byte[][] directions,
    int heatRate,
    int heatDissipation,
    List<? extends BaseDisaster> disasters

) implements ConfigurationSerializable {

    public Weapon(
        Material material,
        byte[][] directions,
        int heatRate,
        int heatDissipation,
        List<? extends BaseDisaster> disasters
    ) {
        this.material = material;
        this.directions = directions;
        this.heatRate = heatRate;
        this.heatDissipation = heatDissipation;
        this.disasters = disasters;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> serialized = Map.of(
                "Material", this.material(),
                "Directions", SerializationUtil.serialize2dByteArray(this.directions()),
                "HeatRate", this.heatRate(),
                "HeatDissipation", this.heatDissipation()
        );

        disasters.sort(Comparator.reverseOrder());
        List<Map<String, Object>> serializedDisasters = new ArrayList<>();
        for (BaseDisaster disaster : disasters) {
            serializedDisasters.add(disaster.serialize());
        }
        serialized.put("Disasters", serializedDisasters);

        return serialized;
    }

    public static Weapon deserialize(Map<String, Object> args) {
        Material material = Material.getMaterial((String) args.get("Material"));
        byte[][] directions = SerializationUtil.deserialize2dByteArray(args.get("Directions"));
        int heatRate = NumberConversions.toInt(args.getOrDefault("HeatRate", 0));
        int heatDissipation = NumberConversions.toInt(args.getOrDefault("HeatDissipation", 0));

        List<? extends BaseDisaster> disasters = (List<? extends BaseDisaster>) args.get("Disasters");
        disasters.sort(Comparator.reverseOrder());

        return new Weapon(material, directions, heatRate, heatDissipation, disasters);
    }
}
