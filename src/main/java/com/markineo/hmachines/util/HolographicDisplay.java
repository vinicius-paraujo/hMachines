package com.markineo.hmachines.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

public class HolographicDisplay {
	private static HashMap<String, List<ArmorStand>> hds = new HashMap<>();

    public static void createHD(String hdName, List<String> lines, Location location) {
    	List<ArmorStand> hd = new ArrayList<>();

        double heightOffset = 0.25;
        double initialVerticalOffset = 1.25;

        for (int i = 0; i < lines.size(); i++) {
            ArmorStand stand = location.getWorld().spawn(location.clone().add(0.5, initialVerticalOffset - i * heightOffset, 0.5), ArmorStand.class);
            
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setSmall(true);
            stand.setCustomNameVisible(true);
            stand.setCustomName(lines.get(i));

            hd.add(stand);
        }

        hds.put(hdName, hd);
    }
    
    public static HashMap<String, List<ArmorStand>> getHds() {
    	return hds;
    }
    
    public static boolean existsHd(String hdId) {
    	if (hds.containsKey(hdId)) return true;
    	
    	return false;
    }
    
    public static void removeAllHDs() {
    	hds.values().forEach(list -> {
    		list.forEach(stand -> {
    			stand.remove();
			});
    	});
    	
    	hds.clear();
    }

    public static void removeHD(String hdName) {
        if (hds.containsKey(hdName)) {
            List<ArmorStand> hd = hds.get(hdName);
            hd.forEach(Entity::remove);
            hds.remove(hdName);
        }
    }
    
    public static boolean existStandNext(Location location) {
        double radius = 0.7;
        return location.getWorld().getNearbyEntities(location, radius, radius, radius).stream()
                .anyMatch(entity -> entity instanceof ArmorStand);
    }

    public static boolean isStandRegistered(ArmorStand stand) {
        return hds.values().stream()
                .flatMap(List::stream)
                .anyMatch(registeredStand -> registeredStand.equals(stand));
    }

    public static void removeStandNext(Location location) {
        double radius = 0.7;
        List<Entity> entitiesToRemove = location.getWorld().getNearbyEntities(location, radius, radius, radius).stream()
                .filter(entity -> entity instanceof ArmorStand)
                .filter(entity -> !isStandRegistered((ArmorStand) entity))
                .collect(Collectors.toList());

        entitiesToRemove.forEach(Entity::remove);
    }
    
    public static void registerArmorStand(String id, ArmorStand stand) {
        hds.computeIfAbsent(id, k -> new ArrayList<>()).add(stand);
    }
    
    
    public static void unregisterArmorStand(String id) {
    	hds.remove(id);
    }
}
