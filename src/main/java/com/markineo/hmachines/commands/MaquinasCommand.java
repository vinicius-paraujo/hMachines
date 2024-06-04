package com.markineo.hmachines.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.markineo.hmachines.menus.MenuManager;

public class MaquinasCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			Bukkit.getConsoleSender().sendMessage("§cEsse comando deve ser executado por um jogador.");
			return true;
		}
		
		Player player = (Player) sender;
		
		Inventory menu = MenuManager.getMenuById(1, player);
		player.openInventory(menu);

		return true;
	}
}
