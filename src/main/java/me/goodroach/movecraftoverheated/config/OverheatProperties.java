package me.goodroach.movecraftoverheated.config;

import me.goodroach.movecraftoverheated.disaster.BaseDisaster;
import me.goodroach.movecraftoverheated.util.SerializationUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public record OverheatProperties(
    NamespacedKey itemID,
    byte[][] directions,
    int heatRate,
    int heatDissipation,
    List<? extends BaseDisaster> disasters

) implements ConfigurationSerializable {

    public OverheatProperties(
        NamespacedKey itemID,
        byte[][] directions,
        int heatRate,
        int heatDissipation,
        List<? extends BaseDisaster> disasters
    ) {
        this.itemID = itemID;
        this.directions = directions;
        this.heatRate = heatRate;
        this.heatDissipation = heatDissipation;
        this.disasters = disasters;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> serialized = Map.of(
                "Material", this.itemID(),
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

    public static OverheatProperties deserialize(Map<String, Object> args) {
        NamespacedKey itemID = NamespacedKey.fromString((String) args.get("Material"));
        byte[][] directions = SerializationUtil.deserialize2dByteArray(args.get("Directions"));
        int heatRate = NumberConversions.toInt(args.getOrDefault("HeatRate", 0));
        int heatDissipation = NumberConversions.toInt(args.getOrDefault("HeatDissipation", 0));

        List<? extends BaseDisaster> disasters = (List<? extends BaseDisaster>) args.get("Disasters");
        disasters.sort(Comparator.reverseOrder());

        return new OverheatProperties(itemID, directions, heatRate, heatDissipation, disasters);
    }
}
