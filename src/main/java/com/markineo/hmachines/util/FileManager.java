package com.markineo.hmachines.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.markineo.hmachines.Machines;
import com.markineo.hmachines.items.ItemManager;
import com.markineo.hmachines.menus.MenuManager;

public class FileManager {
	private static final JavaPlugin plugin = Machines.getPlugin();
	private static Logger logger = plugin.getLogger();
	
	private static FileConfiguration mainConfig;
	private static FileConfiguration databaseConfig;
	private static FileConfiguration messagesConfig;
	private static FileConfiguration machinesConfig;
	private static FileConfiguration combustiveisConfig;
	private static FileConfiguration refinariasConfig;
	
	private static boolean isLoaded;
	
	public static FileConfiguration getConfig(String fileName) {
		if (!plugin.getDataFolder().exists()) {
			plugin.getDataFolder().mkdirs();
		}
		
		File configFile = new File(plugin.getDataFolder(), fileName);
		
		if (!configFile.exists()) {
			plugin.saveResource(fileName, false);
		}
		
		FileConfiguration config = new YamlConfiguration();
		try {
			config.load(configFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
		
		return config;
	}
	
	public static void loadConfigurations() {
		if (isLoaded) {
			return;
		}
        
		mainConfig = getConfig("config.yml");
		messagesConfig = getConfig("messages.yml");
		databaseConfig = getConfig("database.yml");
        combustiveisConfig = getConfig("combustiveis.yml");
        machinesConfig = getConfig("maquinas.yml");
        refinariasConfig = getConfig("refinarias.yml");
        
        File menusFolder = new File(plugin.getDataFolder(), "menus");
        
        if (!menusFolder.exists()) {
        	menusFolder.mkdir();
        }
        
        if (menusFolder.exists() && menusFolder.isDirectory()) {
        	ArrayList<Integer> loadedMenuIds = new ArrayList<>();
            File[] menuFiles = menusFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            
            if (menuFiles != null) {
                for (File menuFile : menuFiles) {
                    try {
                        FileConfiguration menuConfig = YamlConfiguration.loadConfiguration(menuFile);
                        
                        int menuId = menuConfig.getInt("menu_id");
                        MenuManager.registerMenu(menuConfig);
                        loadedMenuIds.add(menuId);
                        
                        logger.info("Menu " + menuId + " carregado com sucesso.");
                    } catch (Exception e) {
                        logger.info("Erro ao carregar o menu: " + menuFile.getName());
                        e.printStackTrace();
                    }
                }
            }
            
            for (int i = 1; i <= 2; i++) {
                if (!loadedMenuIds.contains(i)) {
                    String menuFileName = getMenuFileNameFromId(i);
                    
                    FileConfiguration newMenuConfig = getConfig("menus/" + menuFileName);
                    MenuManager.registerMenu(newMenuConfig);
                    
                    logger.info("Menu " + i + " recriado com sucesso.");
                }
            }
        }
        
        ItemManager.registerAllItems();
        
        isLoaded = true;
	}
	
	private static String getMenuFileNameFromId(int menuId) {
	    switch (menuId) {
	        case 1:
	            return "maquinas.yml";
	        case 2:
	            return "combustiveis.yml";
	        default:
	            return null;
	    }
	}
	
	public static void reloadConfigurations() {
		combustiveisConfig = null;
	    machinesConfig = null;
	    refinariasConfig = null;
	    messagesConfig = null;
	    databaseConfig = null;
	    mainConfig = null;
	    
	    mainConfig = getConfig("config.yml");
	    databaseConfig = getConfig("database.yml");
	    messagesConfig = getConfig("messages.yml");
		combustiveisConfig = getConfig("combustiveis.yml");
        machinesConfig = getConfig("maquinas.yml");
        refinariasConfig = getConfig("refinarias.yml");
        
        ItemManager.registerAllItems();
	}
	
	public static ItemStack deserializeItem(String itemSerialized) {
	    ItemStack item = null;
	    
	    if (itemSerialized == null || itemSerialized.isEmpty()) {
	    	return item;
	    }
	    
        YamlConfiguration config = new YamlConfiguration();
        
        try {
            config.loadFromString(itemSerialized);
            
            item = config.getItemStack("item");
        } catch (Exception e) {
            e.printStackTrace();
        }
	    
	    return item;
	}
	
	public static void saveMaquinasConfig(FileConfiguration config) throws IOException {
		String fileName = "maquinas.yml";
		File configFile = new File(plugin.getDataFolder(), fileName);
		
		config.save(configFile);
	}
	
	public static FileConfiguration getMessagesConfig() {
		return messagesConfig;
	}
	
	public static FileConfiguration getMainConfig() {
		return mainConfig;
	}
	
	public static FileConfiguration getDatabaseConfig() {
		return databaseConfig;
	}

	public static FileConfiguration getCombustiveisConfig() {
	    return combustiveisConfig;
	}

	public static FileConfiguration getMachinesConfig() {
	    return machinesConfig;
	}

	public static FileConfiguration getRefinariasConfig() {
	    return refinariasConfig;
	}
	
	public static ConfigurationSection getMachineData(int id) {
		String machineKey = "maquinas." + id;
		
		return machinesConfig.getConfigurationSection(machineKey);
	}
	
	public static ConfigurationSection getCombustivelData(int id) {
		String key = "combustiveis." + id;
		
		return combustiveisConfig.getConfigurationSection(key);
	}
	
	public static ConfigurationSection getRefinariaData(int id) {
		String key = "refinarias." + id;
		
		return refinariasConfig.getConfigurationSection(key);
	}
	
	public static String getMessage(String message) {
		if (messagesConfig.getString(message) == null) {
			logger.info("Não foi encontrada a mensagem '" + message + "'.");
			return null;
		}
		
		return messagesConfig.getString(message).replace("&","§").replace("{linha}", "§f§n                                                                            \n§f \n");
	}
}
