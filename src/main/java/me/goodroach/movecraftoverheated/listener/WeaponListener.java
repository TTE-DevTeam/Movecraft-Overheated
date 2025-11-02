package me.goodroach.movecraftoverheated.listener;

import me.goodroach.movecraftoverheated.tracking.DispenserGraph;
import me.goodroach.movecraftoverheated.tracking.DispenserHeatData;
import me.goodroach.movecraftoverheated.tracking.WeaponHeatManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.UUID;

import static me.goodroach.movecraftoverheated.MovecraftOverheated.dispenserHeatUUID;
public class WeaponListener implements Listener {
    private WeaponHeatManager heatManager;

    public WeaponListener(WeaponHeatManager heatManager) {
        this.heatManager = heatManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDispense(BlockDispenseEvent event) {
        ItemStack item = event.getItem();
        // TODO: Move to own utility! Avoid using Material.class at all costs and use NamespacedKey instead!
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(item);
        ResourceLocation nmsKey = BuiltInRegistries.ITEM.getKey(nmsItemStack.getItem());
        NamespacedKey itemID = new NamespacedKey(nmsKey.getNamespace(), nmsKey.getPath());

        DispenserGraph graph = heatManager.getWeapons().get(itemID);
        if (graph == null) {
            return;
        }

        Block block = event.getBlock();
        if (block.getType() != Material.DISPENSER) {
            return;
        }

        Block facingBlock = block.getRelative(((Dispenser) block.getBlockData()).getFacing());
        Vector nodeLoc = facingBlock.getLocation().toVector();

        TileState state = (TileState) block.getState();

        DispenserHeatData dispenserHeatData = heatManager.getByLocation(block.getLocation());
        if (dispenserHeatData == null) {
            PersistentDataContainer container = state.getPersistentDataContainer();
            //TODO: Denest this later
            if (container.has(dispenserHeatUUID)) {
                UUID uuid = UUID.fromString(container.get(dispenserHeatUUID, PersistentDataType.STRING));
                if (heatManager.getTrackedDispensers().containsKey(uuid)) {
                    dispenserHeatData = heatManager.getTrackedDispensers().get(uuid);
                } else {
                    dispenserHeatData = new DispenserHeatData(nodeLoc, block.getLocation());
                    container.set(dispenserHeatUUID, PersistentDataType.STRING, dispenserHeatData.getUuid().toString());
                    state.update();
                }
            } else {
                dispenserHeatData = new DispenserHeatData(nodeLoc, block.getLocation());
                container.set(dispenserHeatUUID, PersistentDataType.STRING, dispenserHeatData.getUuid().toString());
                state.update();
            }
        }

        dispenserHeatData.bindToCraft(null);
        graph.addDispenser(dispenserHeatData);
    }
}
