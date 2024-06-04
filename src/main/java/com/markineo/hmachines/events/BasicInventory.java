package com.markineo.hmachines.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.markineo.hmachines.Machines;
import com.markineo.hmachines.menus.MenuManager;
import com.markineo.hmachines.util.FileManager;
import com.markineo.hmachines.util.ShopCollector;

import net.milkbowl.vault.economy.Economy;

public class BasicInventory implements Listener {
	@EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String inventoryTitle = player.getOpenInventory().getTitle();
        if (inventoryTitle == null) return;

        if (isValidItem(event) && MenuManager.isMenu(inventoryTitle)) {
            handleMenuClick(event, player, inventoryTitle);
        }
    }

    private boolean isValidItem(InventoryClickEvent event) {
        return event.getCurrentItem() != null && !event.getCurrentItem().getType().equals(Material.AIR);
    }

    private void handleMenuClick(InventoryClickEvent event, Player player, String inventoryTitle) {
        int position = event.getSlot();
        String action = getAction(event, inventoryTitle, position, player);

        event.setCancelled(true);
        
        double price = getPrice(event, inventoryTitle, position);
        if (price > 0 && !handlePayment(player, price)) return;

        if (action != null) {
            Inventory inventory = event.getClickedInventory();
            processAction(player, action, event.getCurrentItem(), position, inventory, inventoryTitle);
        }
    }

    private String getAction(InventoryClickEvent event, String inventoryTitle, int position, Player player) {
        InventoryAction action = event.getAction();
        if (action.equals(InventoryAction.PICKUP_ALL)) {
            return MenuManager.getLeftAction(inventoryTitle, position);
        } else if (action.equals(InventoryAction.PICKUP_HALF)) {
            return MenuManager.getRightAction(inventoryTitle, position);
        } else {
            return MenuManager.getLeftActionPage(inventoryTitle, position, player.getUniqueId());
        }
    }

    private double getPrice(InventoryClickEvent event, String inventoryTitle, int position) {
        return event.getAction().equals(InventoryAction.PICKUP_ALL) ? MenuManager.getPrice(inventoryTitle, position) : -1;
    }

    private boolean handlePayment(Player player, double price) {
        Economy economy = Machines.getEconomy();
        double balance = economy.getBalance(player);
        
        if (balance < price) {
        	player.sendMessage(FileManager.getMessage("mundos_messages.money_insuficiente"));
			
            return false;
        }
        
        economy.withdrawPlayer(player, price);  
        
        player.sendMessage(FileManager.getMessage("mundos_messages.money_transation").replace("{valor}", String.valueOf(price)));
		
        return true;
    }
	
	private void processAction(Player player, String action, ItemStack clickedItem, int slot, Inventory inventory, String title) {
		switch (action) {
		case "cancel":
			player.closeInventory();
			player.playSound(player.getLocation(), Sound.CLICK, 1.0F, 1.0F);
			
			player.sendMessage(FileManager.getMessage("mundos_messages.action_cancelled"));
			
			break;
		default:
			if (action.startsWith("compra")) {
				player.closeInventory();
				
				String produtoKey = action.substring("compra".length()).trim();
				
                if (produtoKey.isEmpty()) {
                	return;
                }
                
                String[] parts = produtoKey.split(":");
                if (parts.length < 2) {
                    return;
                }

                String produto = parts[0].trim();
                String key = parts[1].trim();
                
                ShopCollector userData = new ShopCollector(key, produto, 0, 0);
                PlayerChat.registerClick(player, userData);
                
                player.sendMessage(FileManager.getMessage("machines_messages.loja_preconfirm_msg"));
                break;
			}
			
			if (action.startsWith("playermessage")) {
                String message = action.substring("playermessage".length()).trim();
                if (!message.isEmpty()) {
                	player.closeInventory();
                    player.sendMessage(message.replace("&", "§"));
                    player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0F, 1.0F);
                }
                
                break;
            }
			
			if (action.startsWith("consolecmd")) {
                String command = action.substring("consolecmd".length()).trim();
                if (!command.isEmpty()) {
                	player.closeInventory();
                	Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                }
                break;
            }
			
			if (action.startsWith("playercmd")) {
                String command = action.substring("playercmd".length()).trim();
                if (!command.isEmpty()) {
                	player.closeInventory();
                	player.performCommand(command);
                	Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                }
                break;
            }
			
			if (action.startsWith("openmenu")) {
                int menuId = Integer.parseInt(action.substring("openmenu".length()).trim());
                if (menuId >= 0) {
                	Inventory menu = MenuManager.getMenuById(menuId, player);
                    player.openInventory(menu);
                }
                break;
            }
			
			break;
		}
	}
}
