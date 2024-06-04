package com.markineo.hmachines.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.markineo.hmachines.Machines;
import com.markineo.hmachines.util.FileManager;

public class ItemManager {
	private static Logger logger = Machines.getPluginLogger();
	
	// [KEY, ITEM]
	private static HashMap<String, ItemStack> machinesItems = new HashMap<>();
	private static HashMap<String, ItemStack> brokenMachinesItems = new HashMap<>();
	private static HashMap<String, ItemStack> refinariasItems = new HashMap<>();
	private static HashMap<String, ItemStack> combustiveisItems = new HashMap<>();
	private static HashMap<String, ItemStack> fixItems = new HashMap<>();
	
	public static void registerAllItems() {
		ConfigurationSection machines = FileManager.getMachinesConfig().getConfigurationSection("maquinas");
		ConfigurationSection brokenMachines = FileManager.getMachinesConfig().getConfigurationSection("maquinas_quebradas");
		ConfigurationSection refinarias = FileManager.getRefinariasConfig().getConfigurationSection("refinarias");
		ConfigurationSection combustiveis = FileManager.getCombustiveisConfig().getConfigurationSection("combustiveis");
		ConfigurationSection fixitems = FileManager.getCombustiveisConfig().getConfigurationSection("fix_items");
		
		registerSectionItems(machines, machinesItems);
		registerSectionItems(refinarias, refinariasItems);
		registerSectionItems(combustiveis, combustiveisItems);
		registerSectionItems(fixitems, fixItems);
		registerSectionItems(brokenMachines, brokenMachinesItems);
	}
	
	private static void registerSectionItems(ConfigurationSection section, HashMap<String, ItemStack> target) {
		if (!target.isEmpty()) {
			target.clear();
		}
		
		for (String key : section.getKeys(false)) {
			ItemStack item = getItem(section.getConfigurationSection(key));
			
			if (item == null) {
				continue;
			}
			
			target.put(key, item);
		};
	}
	
	private static ItemStack getItem(ConfigurationSection section) {
		ItemStack baseItem = getItemById(section.getString("id"));
		
		if (baseItem != null) {
			ItemMeta meta = baseItem.getItemMeta();
			
			String name = replaceLine(section.getString("name"));
			meta.setDisplayName(name);
			
			List<String> lore = new ArrayList<>();
			for (String line : section.getStringList("description")) {
				lore.add(replaceLine(line));
			}
			meta.setLore(lore);
			
			meta.addEnchant(Enchantment.LUCK, 7, true);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			
			baseItem.setItemMeta(meta);
		}
		
		return baseItem;
	}
	
	public static String getBrokenMachineKey(ItemStack item) {
		 return brokenMachinesItems.entrySet().stream()
	                .filter(entry -> entry.getValue().isSimilar(item))
	                .map(HashMap.Entry::getKey)
	                .findFirst()
	                .orElse(null);
	}
	
	public static String getMachineKey(ItemStack item) {
		 return machinesItems.entrySet().stream()
	                .filter(entry -> entry.getValue().isSimilar(item))
	                .map(HashMap.Entry::getKey)
	                .findFirst()
	                .orElse(null);
	}
	
	public static String getCombustivelKey(ItemStack item) {
		 return combustiveisItems.entrySet().stream()
	                .filter(entry -> entry.getValue().isSimilar(item))
	                .map(HashMap.Entry::getKey)
	                .findFirst()
	                .orElse(null);
	}
	
	public static String getRefinariaKey(ItemStack item) {
		 return refinariasItems.entrySet().stream()
	                .filter(entry -> entry.getValue().isSimilar(item))
	                .map(HashMap.Entry::getKey)
	                .findFirst()
	                .orElse(null);
	}
	
	public static boolean isCombustivelItem(ItemStack item) {
		return combustiveisItems.values().stream().anyMatch(mItem -> mItem.isSimilar(item));
	}
	
	public static boolean isFixItem(ItemStack item) {
		return fixItems.values().stream().anyMatch(mItem -> mItem.isSimilar(item));
	}
	
	public static boolean isRefinariaItem(ItemStack item) {
		return refinariasItems.values().stream().anyMatch(mItem -> mItem.isSimilar(item));
	}
	
	public static boolean isMachineItem(ItemStack item) {
		return machinesItems.values().stream().anyMatch(mItem -> mItem.isSimilar(item));
	}
	
	public static boolean isBrokenMachineItem(ItemStack item) {
		return brokenMachinesItems.values().stream().anyMatch(mItem -> mItem.isSimilar(item));
	}
	
	public static int getMachineCapacidade(String key) {
		ConfigurationSection section = FileManager.getMachinesConfig().getConfigurationSection("maquinas").getConfigurationSection(key);
		
		if (section == null) {
			return -1;
		};
		
		return section.getInt("capacidade_litros");
	}
	
	/*
	 * SHOP
	 */
	public static double getMachinePrice(String key) {
		ConfigurationSection section = FileManager.getMachinesConfig().getConfigurationSection("maquinas").getConfigurationSection(key);
		
		if (section == null) {
			return -1;
		};
		
		return section.getDouble("preco");
	}
	
	public static double getRefinariaPrice(String key) {
		ConfigurationSection section = FileManager.getRefinariasConfig().getConfigurationSection("refinarias").getConfigurationSection(key);
		
		if (section == null) {
			return -1;
		};
		
		return section.getDouble("preco");
	}
	
	public static double getCombustivelPrice(String key) {
		ConfigurationSection section = FileManager.getCombustiveisConfig().getConfigurationSection("combustiveis").getConfigurationSection(key);
		
		if (section == null) {
			return -1;
		};
		
		return section.getDouble("preco");
	}
	
	public static double getFixPrice(String key) {
		ConfigurationSection section = FileManager.getCombustiveisConfig().getConfigurationSection("fix_items").getConfigurationSection(key);
		
		if (section == null) {
			return -1;
		};
		
		return section.getDouble("preco");
	}
	
	public static int getRefinariaLimite(String key) {
		ConfigurationSection section = FileManager.getRefinariasConfig().getConfigurationSection("refinarias").getConfigurationSection(key);
		
		if (section == null) {
			return -1;
		};
		
		return section.getInt("limite");
	}
	
	public static ItemStack getRefinariaItem(String key) {
		ConfigurationSection section = FileManager.getRefinariasConfig().getConfigurationSection("refinarias").getConfigurationSection(key);
		
		if (section == null) {
			return null;
		};
		
		return getItem(section);
	}
	
	public static ItemStack getMachineItem(String key) {
		ConfigurationSection section = FileManager.getMachinesConfig().getConfigurationSection("maquinas").getConfigurationSection(key);
		
		if (section == null) {
			return null;
		};
		
		return getItem(section);
	}
	
	public static ItemStack getFixItem(String key) {
		ConfigurationSection section = FileManager.getCombustiveisConfig().getConfigurationSection("fix_items").getConfigurationSection(key);
		
		if (section == null) {
			return null;
		};
		
		return getItem(section);
	}
	
	public static ItemStack getBrokenMachineItem(String key) {
		ConfigurationSection section = FileManager.getMachinesConfig().getConfigurationSection("maquinas_quebradas").getConfigurationSection(key);
		
		if (section == null) {
			return null;
		};
		
		return getItem(section);
	}
	
	public static ItemStack getCombustivelItem(String key) {
		ConfigurationSection section = FileManager.getCombustiveisConfig().getConfigurationSection("combustiveis").getConfigurationSection(key);
		
		if (section == null) {
			return null;
		};
		
		return getItem(section);
	}
	
	public static ItemStack getCombustivelRefinadoItem(String key) {
		int nextkey = Integer.parseInt(key) + 1;
		
		ConfigurationSection section = FileManager.getCombustiveisConfig().getConfigurationSection("combustiveis").getConfigurationSection("" + nextkey);
		
		if (section == null) {
			return null;
		};
		
		return getItem(section);
	}
	
	public static boolean isCombustivelBruto(String key) {
		ConfigurationSection section = FileManager.getCombustiveisConfig().getConfigurationSection("combustiveis").getConfigurationSection(key);
		
		if (section == null) {
			return false;
		}
		
		return section.getBoolean("bruto");
	}
	
	public static double getCombustivelChanceQuebrar(String key) {
		ConfigurationSection section = FileManager.getCombustiveisConfig().getConfigurationSection("combustiveis").getConfigurationSection(key);
		
		if (section == null) {
			return -1;
		}
		
		return section.getDouble("chance_quebrar");
	}
	
	public static int getCombustivelLitros(String key) {
		ConfigurationSection section = FileManager.getCombustiveisConfig().getConfigurationSection("combustiveis").getConfigurationSection(key);
		
		if (section == null) {
			return -1;
		}
		
		return section.getInt("litros");
	}
	
	/*
	 * 
	 * MACHINE DROPS
	 * 
	 */
	public static ItemStack getMachineDrop(String machineKey) {
	    ItemStack dropItem = null;

	    ConfigurationSection section = FileManager.getMachinesConfig()
	            .getConfigurationSection("maquinas")
	            .getConfigurationSection(machineKey)
	            .getConfigurationSection("drops");

	    if (section == null) {
	        return dropItem;
	    }

	    double totalChance = section.getKeys(false)
	            .stream()
	            .mapToDouble(key -> section.getConfigurationSection(key).getDouble("chance", 0))
	            .sum();

	    if (totalChance != 1) {
	        logger.info("A soma das porcentagens dos drops para a máquina '#" + machineKey + "' não representa 100%. Analise o arquivo 'maquinas.yml'.");
	        return dropItem;
	    }

	    Random random = new Random();
	    double randomValue = random.nextDouble();

	    double cumulativeProbability = 0;
	    for (String key : section.getKeys(false)) {
	        ConfigurationSection itemConfig = section.getConfigurationSection(key);
	        
	        double itemChance = itemConfig.getDouble("chance", 0);
	        double normalizedChance = itemChance / totalChance;
	        
	        cumulativeProbability += normalizedChance;

	        if (randomValue <= cumulativeProbability) {
	        	if (itemConfig.getString("item") != null) {
	        		dropItem = FileManager.deserializeItem(itemConfig.getString("item"));
	        	} else {
	        		dropItem = getItem(itemConfig);
	        	}
	        	
	            break;
	        }
	    }

	    return dropItem;
	}
	
	
	public static ItemStack getItemById(String id) {
        ItemStack item = null;
        
        String[] parts = id.split(":");
        if (parts.length == 0 || parts.length > 2) {
            return null;
        }

        try {
            int x = Integer.parseInt(parts[0]);
            
            @SuppressWarnings("deprecation")
			Material material = Material.getMaterial(x);

            if (material == null) {
                return null;
            }

            short damage = 0;
            if (parts.length == 2) {
                damage = Short.parseShort(parts[1]);
            }

            item = new ItemStack(material, 1, damage);
        } catch (NumberFormatException e) {
            return null;
        }

        return item;
    }
	
	private static String replaceLine(String message) {
		message = message.replace("&", "§");
		
		return message;
	}
}
