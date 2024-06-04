package com.markineo.hmachines;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.markineo.hmachines.animations.AnimationManager;
import com.markineo.hmachines.commands.CombustiveisCommand;
import com.markineo.hmachines.commands.MaquinasAdminCommand;
import com.markineo.hmachines.commands.MaquinasCommand;
import com.markineo.hmachines.events.BasicInventory;
import com.markineo.hmachines.events.DisableCraft;
import com.markineo.hmachines.events.OnPlaceMachine;
import com.markineo.hmachines.events.PlayerChat;
import com.markineo.hmachines.events.PlayerInteract;
import com.markineo.hmachines.items.ItemManager;
import com.markineo.hmachines.machines.MachinesManager;
import com.markineo.hmachines.permissions.PermissionsManager;
import com.markineo.hmachines.util.DatabaseManager;
import com.markineo.hmachines.util.FileManager;
import com.markineo.hmachines.util.HolographicDisplay;

import net.milkbowl.vault.economy.Economy;

public class Machines extends JavaPlugin {
	private static JavaPlugin instance;
	private static Logger logger;
	
	private static Economy econ;
	
	private Connection conn;
	
	@Override
	public void onEnable() {
		instance = this;
		logger = getLogger();
		
		if (!setupEconomy()) {
            logger.severe("A dependência 'Vault' não foi encontrada, portanto o plugin será desligado.");
            
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
		
		new FileManager();
		FileManager.loadConfigurations();

		new PermissionsManager();
		new ItemManager();
		new MachinesManager();
		new HolographicDisplay();
		new AnimationManager();
		
		// commands & events
		getCommand("madmin").setExecutor(new MaquinasAdminCommand());
		getCommand("maquinas").setExecutor(new MaquinasCommand());
		getCommand("máquinas").setExecutor(new MaquinasCommand());
		getCommand("combustíveis").setExecutor(new CombustiveisCommand());
		getCommand("combustiveis").setExecutor(new CombustiveisCommand());
		
		getServer().getPluginManager().registerEvents(new BasicInventory(), this);
		getServer().getPluginManager().registerEvents(new OnPlaceMachine(), this);
		getServer().getPluginManager().registerEvents(new PlayerChat(), this);
		getServer().getPluginManager().registerEvents(new PlayerInteract(), this);
		getServer().getPluginManager().registerEvents(new DisableCraft(), this);
		
		new DatabaseManager();
		
		try {
			conn = DatabaseManager.getConnection();
			
			if (conn != null) {
				logger.info("Conectado com sucesso ao MySQL.");
			} else {
				logger.severe("Não foi possível conectar ao banco de dados MySQL, o plugin será desligado.");
				Bukkit.getPluginManager().disablePlugin(this);
			}
		} catch (SQLException e) {
			logger.info(e.getMessage());
		} finally {
			try {
				if (conn != null && !conn.isClosed()) conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		Bukkit.getConsoleSender().sendMessage("§7[§3hMachines§7] §fDesenvolvido por: Markineo.");
		
		Bukkit.getScheduler().runTask(this, () -> {
			try {
				MachinesManager.setupMachines();
				MachinesManager.setupRefinarias();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}
	
	public static Logger getPluginLogger() {
		return logger;
	}
	
	public static JavaPlugin getPlugin() {
		return instance;
	}
	
	public static Economy getEconomy() {
		return econ;
	}
	
	private boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        
        econ = rsp.getProvider();
        return econ != null;
    }
	
	@Override
	public void onDisable() {
		MachinesManager.saveAllMachines();
		MachinesManager.saveAllRefinarias();
		HolographicDisplay.removeAllHDs();
		
		Bukkit.getConsoleSender().sendMessage("§7[§3hMachines§7] O plugin foi desligado com sucesso.");
	}
	
	
}
