package com.markineo.hmachines.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import com.markineo.hmachines.items.ItemManager;

public class DisableCraft implements Listener {
	@EventHandler
	public void onCraft(CraftItemEvent event) {
		if (event.getCurrentItem() == null) {
			return;
		}
		
		ItemStack item = event.getCurrentItem();
		
		boolean isSpecial = ItemManager.isMachineItem(item) || ItemManager.isBrokenMachineItem(item) || ItemManager.isCombustivelItem(item) || ItemManager.isFixItem(item) || ItemManager.isRefinariaItem(item);
		
        if (isSpecial) {
            event.setCancelled(true);
        }
    }
	
}
