package com.markineo.hmachines.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.markineo.hmachines.Machines;
import com.markineo.hmachines.items.ItemManager;
import com.markineo.hmachines.permissions.PermissionsManager;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.Economy;

public class MenuData {
	private List<MenuItem> menuItens = new ArrayList<>();
	
	private int menuId;
	private FileConfiguration menuConfig;
	
	public MenuData(FileConfiguration menuConfig) {
		this.menuId = menuConfig.getInt("menu_id");
		this.menuConfig = menuConfig;
		registerMenuItens();
	}
	
	public boolean isMenuId(int id) {
		return menuId == id;
	}
	
	public FileConfiguration getConfig() {
		return menuConfig;
	}
	
	public double getPrice(int position) {
		return menuItens.stream().filter(i -> i.getPosition() == position).findFirst().orElse(null).getPrice();
	}
	
	public String getLeftActionPage(int position, UUID playerUUID) {
		return menuItens.stream().filter(i -> i.getPosition() == position && i.getKey().equals("ps-"+playerUUID.toString())).findFirst().orElse(null).getLeftAction();
	}
	
	public String getLeftAction(int position) {
		return menuItens.stream().filter(i -> i.getPosition() == position).findFirst().orElse(null).getLeftAction();
	}
	
	public String getRightAction(int position) {
		return menuItens.stream().filter(i -> i.getPosition() == position).findFirst().orElse(null).getRightAction();
	}
	
	public Inventory getMenu(Player player) {
	    String menuTitle = menuConfig.getString("menu_title").replace("&", "§");
	    int menuRows = menuConfig.getInt("menu_rows");
	    Inventory inventory = Bukkit.createInventory(player, menuRows * 9, menuTitle);

	    ArrayList<MenuItem> newMenuItems = new ArrayList<>();
	    
	    for (MenuItem menuItem : menuItens) {
            ItemStack itemStack = createItemStackFromMenuItem(menuItem, player);
            inventory.setItem(menuItem.getPosition(), itemStack);
	    }

	    menuItens.addAll(newMenuItems);

	    return inventory;
	}
    
	private ItemStack createItemStackFromMenuItem(MenuItem menuItem, Player player) {
	    ConfigurationSection itemSection = menuConfig.getConfigurationSection("itens." + menuItem.getKey());
	    ItemStack itemStack = null;
	    
	    String type = menuItem.getType();
	    switch (type) {
	        case "icon":
	        	String id = itemSection.getString("id");
	        	
	        	ItemStack specialItem = getSpecialItem(id);
	        	if (specialItem != null) {
	        	    itemStack = specialItem;
	        	    ItemMeta meta = itemStack.getItemMeta();
	        	    
	        	    for (ItemFlag flag : meta.getItemFlags()) {
	        	        meta.removeItemFlags(flag);
	        	    }

	        	    for (Enchantment enchantment : meta.getEnchants().keySet()) {
	        	        meta.removeEnchant(enchantment);
	        	    }

	        	    itemStack.setItemMeta(meta);
	        	} else {
	        	    itemStack = ItemManager.getItemById(id);
	        	}
	        	
	     	    Material material = itemStack.getType();
	     	    
	     	    if (material.equals(Material.SKULL_ITEM) && itemSection.getString("skull_owner") != null) {
	     	        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
	     	        String skullOwner = itemSection.getString("skull_owner").replace("{player_name}", player.getName());
	     	        
	     	        if (skullOwner.contains("{heads")) {
	     	        	HeadDatabaseAPI api = new HeadDatabaseAPI();
	     	        	itemStack = api.getItemHead(skullOwner.replace("{heads-", "").replace("}", ""));
	     	        } else {
 	        			skullMeta.setOwner(skullOwner);
     	        		itemStack.setItemMeta(skullMeta);
	     	        }
	     	    }

	     	    ItemMeta meta = itemStack.getItemMeta();
	     	    meta.setDisplayName(replacePlaceHolders(itemSection.getString("name"), player));
	     	    
	     	    List<String> lore = new ArrayList<>();
	     	    for (String line : itemSection.getStringList("description")) {
	     	        String processedLine = replacePlaceHolders(line, player);
	     	        lore.add(processedLine);
	     	    }
	     	    
	     	    meta.setLore(lore);
	     	    itemStack.setItemMeta(meta);
	     	    
	            break;
	    }

	    return itemStack;
	}
	
	private ItemStack getSpecialItem(String id) {
		String[] parts = id.split(":");
        if (parts.length < 2) {
            return null;
        }

        String produto = parts[0].trim();
        String key = parts[1].trim();
        
        return getProductItem(produto, key);
	}
	
	private ItemStack getProductItem(String product, String key) {
		ItemStack item;
		
		switch (product.toLowerCase()) {
		case "combustivel":
			item = ItemManager.getCombustivelItem(key);
			break;
		case "maquina":
			item = ItemManager.getMachineItem(key);
			break;
		case "refinaria":
			item = ItemManager.getRefinariaItem(key);
			break;
		case "fix":
			item = ItemManager.getFixItem(key);
			break;
		default:
			item = null;
			break;
		}
		
		return item;
	};
	
	private void registerMenuItens() {
	    ConfigurationSection itemsSection = menuConfig.getConfigurationSection("itens");
	    if (itemsSection == null) return;
	    
	    for (String itemKey : itemsSection.getKeys(false)) {
	        ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemKey);
	        
	        String type = itemSection.getString("type");
	        
	        int posX = itemSection.getInt("pos_x");
	        int posY = itemSection.getInt("pos_y");
	        int position = posX + (posY * 9);
	        
	        String leftAction = itemSection.getString("left_action", "");
	        String rightAction = itemSection.getString("right_action", "");
	        
	        double price = itemSection.getDouble("price");
	        
	        MenuItem menuItem;
	        if (price > 0) {
	        	menuItem = new MenuItem(position, type, itemKey, leftAction, rightAction, price);
	        } else {
	        	menuItem = new MenuItem(position, type, itemKey, leftAction, rightAction);
	        }
	        
	        menuItens.add(menuItem);
	    }
	}
	
	private String replacePlaceHolders(String message, OfflinePlayer player) {
		Economy economy = Machines.getEconomy();
		double playerBalance = economy.getBalance(player);
		
		message = message.replace("{prefix}", getPlayerPrefix(player.getUniqueId())).replace("{player_name}", player.getName()).replace("&", "§").replace("{saldo}", formatarMoney(playerBalance));
		message = replaceLine(player, message);
		
		return message;
	}
	
	private String getPlayerPrefix(UUID playerUUID) {
		return PermissionsManager.getUser(playerUUID.toString()).getCachedData().getMetaData().getPrefix();
	}

	private String replaceLine(OfflinePlayer player, String line) {
		Matcher matcher = Pattern.compile("%([^%]+)%").matcher(line);

        while (matcher.find()) {
        	String placeholder = matcher.group(1);
            String placeholderValue = PlaceholderAPI.setPlaceholders(player, "%" + placeholder + "%");
            
            if (placeholderValue != null) {
                line = line.replace("%" + placeholder + "%", placeholderValue);
            }
        }

        return line;
    }
	
	private String formatarMoney(double valor) {
	    String[] sufixos = new String[]{"", "k", "M", "B", "T", "Q"};
	    int indice = 0;
	    
	    while (valor >= 1000 && indice < sufixos.length - 1) {
	        valor /= 1000;
	        indice++;
	    }
	    
	    return String.format("%.2f%s", valor, sufixos[indice]);
	}
	
}