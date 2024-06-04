package com.markineo.hmachines.animations;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.markineo.hmachines.Machines;
import com.markineo.hmachines.machines.MachineData;
import com.markineo.hmachines.machines.RefinariaData;
import com.markineo.hmachines.util.HolographicDisplay;

import me.arcaniax.hdb.api.HeadDatabaseAPI;

public class AnimationManager {
    private static final JavaPlugin plugin = Machines.getPlugin();

    private static final List<Block> animatedBlocks = new ArrayList<>();
    
    private static final long INTERVAL = 2L;

    public static void runMachineAnimation(Block block, MachineData machine) {
        if (animatedBlocks.contains(block)) {
            return;
        }

        animatedBlocks.add(block);
        
        HeadDatabaseAPI api = new HeadDatabaseAPI();
        ItemStack headIcon = api.getItemHead("47441");

        Location loc = block.getLocation().add(0.5, -0.1, 0.5);

        ArmorStand armorStand = (ArmorStand) block.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        String key = "a:" + machine.getOwnerUUID() + ":" + machine.getMachineId();
        
        HolographicDisplay.registerArmorStand(key, armorStand);

        armorStand.setVisible(false);
        armorStand.setSmall(true);
        armorStand.setHelmet(headIcon);

        new BukkitRunnable() {
            private double angle = 0;

            @Override
            public void run() {
                if (machine == null || !machine.isActive() || armorStand.isDead()) {
                    startRecoilAnimation(block, armorStand, key);
                    cancel();
                    
                    return;
                }

                Location headLoc = armorStand.getLocation().add(0, 0.5, 0);
                headLoc.setYaw((float) angle);

                armorStand.teleport(headLoc);

                if (angle % 30 == 0) {
                    block.getWorld().playEffect(headLoc, Effect.FIREWORKS_SPARK, 0);
                }

                angle += 5;
            }
        }.runTaskTimer(plugin, 0L, INTERVAL);
    }
    
    public static void runRefinariaAnimation(Block block, RefinariaData refinaria) {
        if (animatedBlocks.contains(block)) {
            return;
        }

        animatedBlocks.add(block);
        
        HeadDatabaseAPI api = new HeadDatabaseAPI();
        ItemStack headIcon = api.getItemHead("66250");

        Location loc = block.getLocation().add(0.5, -0.1, 0.5);

        ArmorStand armorStand = (ArmorStand) block.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        String key = "ar:" + refinaria.getOwnerUUID() + ":" + refinaria.getRefinariaId();
        
        HolographicDisplay.registerArmorStand(key, armorStand);

        
        armorStand.setVisible(false);
        armorStand.setSmall(true);
        armorStand.setHelmet(headIcon);

        new BukkitRunnable() {
            private double angle = 0;

            @Override
            public void run() {
                if (refinaria == null || !refinaria.isActive() || armorStand.isDead()) {
                    startRecoilAnimation(block, armorStand, key);
                    cancel();
                    
                    return;
                }

                Location headLoc = armorStand.getLocation().add(0, 0.5, 0);
                headLoc.setYaw((float) angle);

                armorStand.teleport(headLoc);

                if (angle % 30 == 0) {
                    block.getWorld().playEffect(headLoc, Effect.FIREWORKS_SPARK, 0);
                }

                angle += 5;
            }
        }.runTaskTimer(plugin, 0L, INTERVAL);
    }

    private static void startRecoilAnimation(Block block, ArmorStand armorStand, String key) {
        new BukkitRunnable() {
            private double yOffset = 0;

            @Override
            public void run() {
                if (yOffset <= -1.0) {
                    armorStand.remove();
                    animatedBlocks.remove(block);
                    HolographicDisplay.unregisterArmorStand(key);
                    cancel();
                    return;
                }

                yOffset -= 0.05;
                Location headLoc = armorStand.getLocation().add(0, yOffset, 0);
                armorStand.teleport(headLoc);
            }
        }.runTaskTimer(plugin, 0L, INTERVAL);
    }
}