package com.markineo.hmachines.events;

import java.sql.SQLException;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.markineo.hmachines.items.ItemManager;
import com.markineo.hmachines.machines.MachineData;
import com.markineo.hmachines.machines.MachinesManager;
import com.markineo.hmachines.machines.RefinariaData;
import com.markineo.hmachines.util.FileManager;
import com.markineo.hmachines.util.HolographicDisplay;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class OnPlaceMachine implements Listener {
	private WorldGuardPlugin worldGuard;
	
	public OnPlaceMachine() {
		this.worldGuard = WorldGuardPlugin.inst();
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.getPlayer() == null || event.getBlock() == null) {
			return;
		}
		
		Player player = event.getPlayer();
		Block block = event.getBlock();
		
		ItemStack handItem = event.getItemInHand();
		
		if (!event.canBuild() || !worldGuard.canBuild(player, event.getBlock())) {
			event.setCancelled(true);
			
			return;
		}
		
		try {
			if (ItemManager.isRefinariaItem(handItem)) {
				placeRefinariaItem(player, block, handItem);
			}
			
			if (ItemManager.isMachineItem(handItem)) {
				placeMachineItem(player, block, handItem, event);
			}
			
			if (ItemManager.isBrokenMachineItem(handItem)) {
				placeBrokenMachineItem(player, block, handItem, event);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	private static void placeRefinariaItem(Player player, Block placedBlock, ItemStack handItem) throws SQLException {
		int id = MachinesManager.getNextRefinariaId();
		String refinariaKey = ItemManager.getRefinariaKey(handItem);
		
		Location location = placedBlock.getLocation();
		
		RefinariaData refinaria = new RefinariaData(id, refinariaKey, player.getUniqueId(), false, null, 0, ItemManager.getRefinariaLimite(refinariaKey), location, location.getWorld().getUID());
		MachinesManager.registerRefinaria(location, refinaria);
		
		player.sendMessage(FileManager.getMessage("machines_messages.place_refinaria").replace("{quantidade}", "" + 1));
	}
	
	private static void placeMachineItem(Player player, Block placedBlock, ItemStack handItem, BlockPlaceEvent event) throws SQLException {
		String machineKey = ItemManager.getMachineKey(handItem);
		
		if (player.isSneaking() && MachinesManager.existMachineInRaio(placedBlock.getLocation(), 5)) {
			if (!player.hasPermission("hmachines.stack")) {
				player.sendMessage(FileManager.getMessage("machines_messages.permission_err3")); 
				
				return;
			}
			
			MachineData machine = MachinesManager.getMachineAt(MachinesManager.getMachineInRaio(placedBlock.getLocation(), 5));
			
			if (machine == null) {
				event.setCancelled(true);
				return;
			}
			
			if (!machine.getOwnerUUID().equals(player.getUniqueId())) {
				player.sendMessage(FileManager.getMessage("machines_messages.stack_err1"));
				event.setCancelled(true);
				
				return;
			}
			
			if (!machine.getMachineKey().equals(machineKey)) {		
				player.sendMessage(FileManager.getMessage("stack_err2"));
				event.setCancelled(true);
				
				return;
			}
			
			player.getInventory().removeItem(handItem);
			
			int capacidadeBase = ItemManager.getMachineCapacidade(machineKey);
			int machineCapacidade = machine.getLitrosMax();
			int newCapacidade = machineCapacidade + (capacidadeBase * handItem.getAmount());
			
			machine.updateCapacidade(newCapacidade);
			machine.updateStacks((machine.getStacks() + handItem.getAmount()));
			
			event.setCancelled(true);
			
			player.sendMessage(FileManager.getMessage("machines_messages.machine_stack").replace("{quantidade}", "" + handItem.getAmount()));
		} else {
			int id = MachinesManager.getNextMachineId();
			
			int capacidadeBase = ItemManager.getMachineCapacidade(machineKey);
			
			MachineData machine = new MachineData(id, machineKey, player.getUniqueId(), false, false, 0, capacidadeBase, 1);
			MachinesManager.registerMachine(placedBlock.getLocation(), machine);
			
			player.sendMessage(FileManager.getMessage("machines_messages.place_machine"));
		}
	}
	
	private static void placeBrokenMachineItem(Player player, Block placedBlock, ItemStack handItem, BlockPlaceEvent event) throws SQLException {
		String machineKey = ItemManager.getBrokenMachineKey(handItem);
		
		if (player.isSneaking() && MachinesManager.existMachineInRaio(placedBlock.getLocation(), 5)) {
			if (!player.hasPermission("hmachines.stack")) {
				player.sendMessage(FileManager.getMessage("machines_messages.permission_err3")); 
				
				return;
			}
			
			MachineData machine = MachinesManager.getMachineAt(MachinesManager.getMachineInRaio(placedBlock.getLocation(), 5));
			
			if (machine == null) {
				event.setCancelled(true);
				return;
			}
			
			if (!machine.getOwnerUUID().equals(player.getUniqueId())) {
				player.sendMessage(FileManager.getMessage("machines_messages.stack_err1"));
				event.setCancelled(true);
				
				return;
			}
			
			if (!machine.getMachineKey().equals(machineKey)) {		
				player.sendMessage(FileManager.getMessage("machines_messages.stack_err2"));
				event.setCancelled(true);
				
				return;
			}
			
			if (!machine.isBroken()) {
				player.sendMessage(FileManager.getMessage("machines_messages.machine_stack_broken_err"));
				event.setCancelled(true);
				
				return;
			}
			
			player.getInventory().removeItem(handItem);
			
			int capacidadeBase = ItemManager.getMachineCapacidade(machineKey);
			int machineCapacidade = machine.getLitrosMax();
			int newCapacidade = machineCapacidade + (capacidadeBase * handItem.getAmount());
			
			machine.updateCapacidade(newCapacidade);
			machine.updateStacks((machine.getStacks() + handItem.getAmount()));
			
			event.setCancelled(true);
			
			player.sendMessage(FileManager.getMessage("machines_messages.place_broken_machine").replace("{quantidade}", "" + handItem.getAmount()));
		} else {
			int id = MachinesManager.getNextMachineId();
			
			int capacidadeBase = ItemManager.getMachineCapacidade(machineKey);
			
			MachineData machine = new MachineData(id, machineKey, player.getUniqueId(), false, true, 0, capacidadeBase, 1);
			MachinesManager.registerMachine(placedBlock.getLocation(), machine);
			
			player.sendMessage(FileManager.getMessage("machines_messages.place_broken_machine").replace("{quantidade}", 1 + ""));
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getBlock() == null || event.getPlayer() == null) {
			return;
		}
		
		Block block = event.getBlock();
		Player player = event.getPlayer();
		
		Location location = block.getLocation();
		
		MachineData machine = MachinesManager.getMachineAt(location);
		
		if (machine != null) {	
			boolean isMachineBroken = machine.isBroken();
			
			if (!isAdmin(player) && !machine.getOwnerUUID().equals(player.getUniqueId())) {
				player.sendMessage(FileManager.getMessage("machines_messages.permission_err2"));
				event.setCancelled(true);
				
				return;
			}
			
			String hdId = "hd:" + player.getUniqueId().toString() + ":" + machine.getMachineId();
			String key = "a:" + machine.getOwnerUUID() + ":" + machine.getMachineId();
			
			if (HolographicDisplay.existsHd(key)) {
				HolographicDisplay.removeHD(key);
			}
			
			if (HolographicDisplay.existsHd(hdId)) {
				HolographicDisplay.removeHD(hdId);
			}
			
			String machineKey = machine.getMachineKey();
			int stacks = machine.getStacks();
			
			ItemStack machineItem = isMachineBroken ? ItemManager.getBrokenMachineItem(machineKey) : ItemManager.getMachineItem(machineKey);
			machineItem.setAmount(stacks);
			
			MachinesManager.deleteMachine(location, machine);
			event.setCancelled(true);
			block.setType(Material.AIR);
			
			player.getInventory().addItem(machineItem);
			
			player.sendMessage(FileManager.getMessage("machines_messages.machine_removed").replace("{quantidade}", "" + stacks));
			
			return;
		};
		
		RefinariaData refinaria = MachinesManager.getRefinariaAt(location);
		
		if (refinaria != null) {
			if (!isAdmin(player) && !refinaria.getOwnerUUID().equals(player.getUniqueId())) {
				player.sendMessage(FileManager.getMessage("machines_messages.refinaria_only_owner"));
				event.setCancelled(true);
				
				return;
			}
			
			String hdId = "hd:" + player.getUniqueId().toString() + ":" + refinaria.getRefinariaId();
			String key = "ar:" + refinaria.getOwnerUUID() + ":" + refinaria.getRefinariaId();
			
			if (HolographicDisplay.existsHd(key)) {
				HolographicDisplay.removeHD(key);
			}
			
			if (HolographicDisplay.existsHd(hdId)) {
				HolographicDisplay.removeHD(hdId);
			}
			
			String refinariaKey = refinaria.getRefinariaKey();
			ItemStack refinariaItem = ItemManager.getRefinariaItem(refinariaKey);
			
			MachinesManager.deleteRefinaria(location, refinaria);
			event.setCancelled(true);
			block.setType(Material.AIR);
			
			player.getInventory().addItem(refinariaItem);
			
			player.sendMessage(FileManager.getMessage("machines_messages.refinaria_removed").replace("{quantidade}", "" + 1));
			
			return;
		}
	}
	
	private static boolean isAdmin(Player player) {
		return player.hasPermission("hmachines.admin");
	}
}
