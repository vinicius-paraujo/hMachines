package com.markineo.hmachines.menus;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class MenuManager {
	private static HashMap<String, MenuData> menus = new HashMap<>();

	public static void registerMenu(FileConfiguration menuFile) {
		try {
			String menuTitle = menuFile.getString("menu_title").replace("&", "§");
			
			menus.put(menuTitle, new MenuData(menuFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static double getPrice(String menuTitle, int position) {
		return menus.get(menuTitle).getPrice(position);
	}
	
	public static String getLeftActionPage(String menuTitle, int position, UUID playerUUID) {
		return menus.get(menuTitle).getLeftActionPage(position, playerUUID);
	}
	
	public static String getLeftAction(String menuTitle, int position) {
		return menus.get(menuTitle).getLeftAction(position);
	}
	
	public static String getRightAction(String menuTitle, int position) {
		return menus.get(menuTitle).getRightAction(position);
	}
	
	public static boolean isMenu(String menuTitle) {
		return menus.containsKey(menuTitle);
	}
	
	public static Inventory getMenuByTitle(String title, Player player) {
		return menus.get(title).getMenu(player);
	};
	
	public static Inventory getMenuById(int id, Player player) {
        MenuData menu = menus.values().stream()
                             .filter(m -> m.isMenuId(id))
                             .findFirst()
                             .orElse(null);

        if (menu != null) {
            return menu.getMenu(player);
        }

        return null;
    }
	
}