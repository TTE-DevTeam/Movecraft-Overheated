package me.goodroach.movecraftoverheated.listener;

import me.goodroach.movecraftoverheated.tracking.WeaponHeatManager;
import me.goodroach.movecraftoverheated.weapons.Weapon;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Set;

public class WeaponListener implements Listener {
    private WeaponHeatManager heatManager;
    private Set<Weapon> weaponSet;

    public WeaponListener(WeaponHeatManager heatManager) {
        this.heatManager = heatManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDispense(BlockDispenseEvent event) {
        ItemStack item = event.getItem();

        Weapon firedWeapon = null;
        for (Weapon weapon : heatManager.getWeapons()) {
            if (item.getType() == weapon.getMaterial()) {
                firedWeapon = weapon;
            }
        }

        if (firedWeapon == null) {
            return;
        }

        Block block = event.getBlock();
        if (block.getType() != Material.DISPENSER) {
            return;
        }

        Block facingBlock = block.getRelative(((Dispenser) block.getBlockData()).getFacing());
        Vector nodeLoc = facingBlock.getLocation().toVector();

        firedWeapon.addNode(block, nodeLoc);
    }
}