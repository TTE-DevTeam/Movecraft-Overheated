package me.goodroach.movecraftoverheated.listener;

import io.papermc.paper.event.block.BlockBreakBlockEvent;
import me.goodroach.movecraftoverheated.tracking.WeaponHeatManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

import static me.goodroach.movecraftoverheated.MovecraftOverheated.dispenserHeatUUID;

public class BlockListener implements Listener {

    private WeaponHeatManager heatManager;

    public BlockListener(WeaponHeatManager heatManager) {
        this.heatManager = heatManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        handle(block);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakBlockEvent event) {
        Block block = event.getBlock();
        handle(block);
    }

    void handle(final Block block) {
        if (block == null)
            return;
        if (block.getType() != Material.DISPENSER || !(block.getState() instanceof TileState)) {
            return;
        }
        TileState state = (TileState) block.getState();
        PersistentDataContainer container = state.getPersistentDataContainer();
        if (container.has(dispenserHeatUUID)) {
            heatManager.removeDispenser(UUID.fromString(container.get(dispenserHeatUUID, PersistentDataType.STRING)));
        }
    }

}
