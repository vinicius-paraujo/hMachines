package com.markineo.hmachines.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.markineo.hmachines.Machines;
import com.markineo.hmachines.animations.AnimationManager;
import com.markineo.hmachines.items.ItemManager;
import com.markineo.hmachines.machines.MachineData;
import com.markineo.hmachines.machines.MachinesManager;
import com.markineo.hmachines.machines.RefinariaData;
import com.markineo.hmachines.permissions.PermissionsManager;
import com.markineo.hmachines.util.FileManager;
import com.markineo.hmachines.util.HolographicDisplay;

public class PlayerInteract implements Listener {
	private static JavaPlugin plugin;

	private static int ANIMATION_DISTANCE = FileManager.getMainConfig().getInt("animation_distance");
	
	private Map<Block, Integer> dropTasks = new HashMap<>();
	
	public PlayerInteract() {
		PlayerInteract.plugin = Machines.getPlugin();
	}
	
	@EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
		if (event.getRightClicked() == null || !(event.getRightClicked() instanceof ArmorStand)) {
			return;
		}
		
		ArmorStand stand = (ArmorStand) event.getRightClicked();
		if (HolographicDisplay.isStandRegistered(stand)) {
			event.setCancelled(true);
		}
    }
	
	@EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
        	return;
        }
        
        if (event.getPlayer() == null || event.getClickedBlock() == null) {
        	return;
        }
        
        Player player = event.getPlayer(); 
        
        ItemStack handItem = player.getItemInHand();
        Block clickedBlock = event.getClickedBlock();
        
        if (clickedBlock == null || clickedBlock.getType().equals(Material.AIR)) {
        	return;
        }
        
        if (handItem != null && !handItem.getType().equals(Material.AIR) && (ItemManager.isCombustivelItem(handItem) || ItemManager.isFixItem(handItem))) {
        	event.setCancelled(true);
        }
        
        if (MachinesManager.getRefinariaAt(clickedBlock.getLocation()) == null && MachinesManager.getMachineAt(clickedBlock.getLocation()) == null) {
        	return;
        }
        
        if (!player.hasPermission("hmachines.use")) {
        	player.sendMessage(FileManager.getMessage("machines_messages.permission_err1")); 
			return;
		}
        
        event.setCancelled(true);
        
        if (ItemManager.isFixItem(handItem)) {
        	handleFixMachine(event, player, clickedBlock, handItem);
        } else if (ItemManager.isCombustivelItem(handItem)) {
        	
        	MachineData machine = MachinesManager.getMachineAt(clickedBlock.getLocation());
        	if (machine != null) {
        		handleMachineInteraction(machine, player, clickedBlock, handItem);
        		return;
        	}
        	
        	RefinariaData refinaria = MachinesManager.getRefinariaAt(clickedBlock.getLocation());
        	if (refinaria != null) {
        		handleRefinariaInteraction(refinaria, player, clickedBlock, handItem);
        		return;
        	}
        	
        } else {
            handleHolographicDisplay(player, clickedBlock);
        }
    }
	
	private void handleFixMachine(PlayerInteractEvent event, Player player, Block clickedBlock, ItemStack handItem) {
		int size = handItem.getAmount();
		
		MachineData machine = MachinesManager.getMachineAt(clickedBlock.getLocation());
		
		if (machine == null) {
			return;
		}
		
		if (machine.isBroken()) {
			boolean everyoneCanReload = FileManager.getMainConfig().getBoolean("qualquer_um_pode_recarregar");
			if (!isAdmin(player) && !everyoneCanReload && !machine.getOwnerUUID().equals(player.getUniqueId())) {
				player.sendMessage(FileManager.getMessage("machines_messages.permission_err5"));
				
				return;
			}
			
			if (size > 1) {
				handItem.setAmount((size-1));
			} else {
				player.getInventory().removeItem(handItem);
			}
			
			machine.setBrokenState(false);
			
			player.sendMessage(FileManager.getMessage("machines_messages.machine_fixed"));
		}
	}
	
	private void handleRefinariaInteraction(RefinariaData refinaria, Player player, Block clickedBlock, ItemStack handItem) {
		String key = ItemManager.getCombustivelKey(handItem);
		
		if (!ItemManager.isCombustivelBruto(key)) {
			player.sendMessage(FileManager.getMessage("machines_messages.refinaria_err3"));
			
			return;
		}
		
		boolean everyoneCanReload = FileManager.getMainConfig().getBoolean("qualquer_um_pode_recarregar");
		if (!everyoneCanReload && !refinaria.getOwnerUUID().equals(player.getUniqueId())) {
			player.sendMessage(FileManager.getMessage("machines_messages.permission_err6"));
			
			return;
		}
		
		int refCombustivel = refinaria.getCombustivelAmount(); 
		int refLimit = refinaria.getCombustivelMax();
		
		String combustivelKey = refinaria.getCombustivelAtualKey();

		if (refinaria.isActive() && combustivelKey != null && !key.equals(combustivelKey)) {
			player.sendMessage(FileManager.getMessage("machines_messages.refinaria_err1")); 
			
			return; 
		}
		
		int total;
		if (player.isSneaking()) {
			total = refCombustivel + handItem.getAmount();
			
			if (total > refLimit) {
				player.sendMessage(FileManager.getMessage("machines_messages.refinaria_err2"));
				return;
			}
			
			player.getInventory().removeItem(handItem);      
		} else {
			total = (refCombustivel + 1);
			if (total > refLimit) {
				player.sendMessage(FileManager.getMessage("machines_messages.refinaria_err2"));
				return; 
			}
			
			if (handItem.getAmount() > 1) {
				int newSize = (handItem.getAmount() - 1);
				handItem.setAmount(newSize);
			} else {
				player.getInventory().removeItem(handItem);
			}
		}
		
		if (!refinaria.isActive()) {
			refinaria.changeActive(true);
		}
		
		if (!key.equals(combustivelKey)) {
			refinaria.changeCombustivelType(key);
		}
		
		refinaria.changeCombustivelAmount(total);
		
		if (MachinesManager.existsPlayerInRaio(clickedBlock.getLocation(), ANIMATION_DISTANCE)) {
			AnimationManager.runRefinariaAnimation(clickedBlock, refinaria);
		}
		
		if (dropTasks.containsKey(clickedBlock)) {
	        int taskId = dropTasks.get(clickedBlock);
	        plugin.getServer().getScheduler().cancelTask(taskId);
	        
	        
	        int newTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> refinariaDropItem(clickedBlock, key, total), FileManager.getMainConfig().getLong("refinaria_drop"));
	        dropTasks.put(clickedBlock, newTaskId);
	    } else {
	        int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> refinariaDropItem(clickedBlock, key, total), FileManager.getMainConfig().getLong("refinaria_drop"));
	        
	        dropTasks.put(clickedBlock, taskId);
	    }
	}

    private void handleMachineInteraction(MachineData machine, Player player, Block clickedBlock, ItemStack handItem) {
		boolean everyoneCanReload = FileManager.getMainConfig().getBoolean("qualquer_um_pode_recarregar");
		if (!isAdmin(player) && !everyoneCanReload && !machine.getOwnerUUID().equals(player.getUniqueId())) {
			player.sendMessage(FileManager.getMessage("machines_messages.permission_err5"));
			
			return;
		}
		
    	if (machine.isBroken()) {
    		player.sendMessage(FileManager.getMessage("machines_messages.broken_err3"));
    		
    		return;
		}
    	
    	String combustivelKey = ItemManager.getCombustivelKey(handItem);
    	if (combustivelKey == null) {
			player.sendMessage("§cOcorreu um erro com o combustível, tente novamente.");
			return;
		}
    		
		int litros = machine.getLitros(); 
		int limit = machine.getLitrosMax();
		int stacks = machine.getStacks();
		
		String machineKey = machine.getMachineKey();
		
		if (ItemManager.isCombustivelBruto(combustivelKey)) {
		    double chance = ItemManager.getCombustivelChanceQuebrar(combustivelKey);
		    
		    if (new Random().nextInt(100) <= Math.abs(100 * chance)) {
		        player.sendMessage(FileManager.getMessage("machines_messages.broken_err2"));
		        
		        if (machine.isActive()) {
		        	machine.changeActive(false);
		        }
		        
		        machine.setBrokenState(true);
		        
				dropTasks.remove(clickedBlock);
		       
		        return;
		    } else {
		        player.sendMessage(FileManager.getMessage("machines_messages.broken_err1"));
		    }
		}

		int total;
		
		int combustivelLitros = ItemManager.getCombustivelLitros(combustivelKey);
		
		if (player.isSneaking()) {
			total = litros + (combustivelLitros * handItem.getAmount());
			
			if (total > limit) {
				player.sendMessage(FileManager.getMessage("machines_messages.combustivel_err1"));
				
				return;
			}
			
			player.getInventory().removeItem(handItem);      
		} else {
			total = (litros + combustivelLitros);
			
			if (total > limit) {
				player.sendMessage(FileManager.getMessage("machines_messages.combustivel_err1"));
				return;
			}
			
			int newSize = (handItem.getAmount() - 1);
			
			if (handItem.getAmount() > 1) {
				handItem.setAmount(newSize);
			} else {
				player.getInventory().removeItem(handItem);
			}
		}
		
		machine.updateLitrosAmount(total);
		
		if (MachinesManager.existsPlayerInRaio(clickedBlock.getLocation(), ANIMATION_DISTANCE)) {
			AnimationManager.runMachineAnimation(clickedBlock, machine);
		}
		
	    if (dropTasks.containsKey(clickedBlock)) {
	        int taskId = dropTasks.get(clickedBlock);
	        plugin.getServer().getScheduler().cancelTask(taskId);
	        
	        int newTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> machineDropItem(clickedBlock, total, stacks, machineKey), FileManager.getMainConfig().getLong("drop_ticks"));
	        dropTasks.put(clickedBlock, newTaskId);
	    } else {
	        int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> machineDropItem(clickedBlock, total, stacks, machineKey), FileManager.getMainConfig().getLong("drop_ticks"));
	        dropTasks.put(clickedBlock, taskId);
	    }
	    
	    if (!machine.isActive()) {
	    	machine.changeActive(true);
	    }
    }

    private void handleHolographicDisplay(Player player, Block clickedBlock) {
    	Location location = clickedBlock.getLocation();
    	MachineData machine = MachinesManager.getMachineAt(location);
    	
    	if (machine != null) {
			if (HolographicDisplay.existStandNext(location)) {
				HolographicDisplay.removeStandNext(location);
			}
			
			UUID ownerUUID = machine.getOwnerUUID();
			
			String hdId = "hd:" + ownerUUID.toString() + ":" + machine.getMachineId();
			
			if (HolographicDisplay.existsHd(hdId)) {
				HolographicDisplay.removeHD(hdId);
			}
			
			OfflinePlayer ownerPlayer = Bukkit.getOfflinePlayer(ownerUUID);
			String playerPrefix = PermissionsManager.getUser(ownerPlayer.getUniqueId().toString()).getCachedData().getMetaData().getPrefix().replace("&", "§");
			
    		if (machine.isBroken()) {
    			int stacks = machine.getStacks();
    			
    			List<String> hdData = new ArrayList<>();
    			
    			for (String line : FileManager.getMessagesConfig().getStringList("hds.broken")) {
    				hdData.add(line
    						.replace("&", "§")
    						.replace("{player_name}", ownerPlayer.getName())
    						.replace("{stacks}", "" + stacks)
    						.replace("{prefix}", playerPrefix));
    			}
    				
				HolographicDisplay.createHD(hdId, hdData, location);
				
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> HolographicDisplay.removeHD(hdId), 250L);
    		} else {
    			int litrosAtuais = machine.getLitros();
    			int litrosMax = machine.getLitrosMax();
    			int stacks = machine.getStacks();
    			boolean active = machine.isActive();
    			String machineKey = machine.getMachineKey();
    			
    			if (litrosAtuais >= stacks && !active) {
    				if (MachinesManager.existsPlayerInRaio(clickedBlock.getLocation(), ANIMATION_DISTANCE)) {
    					AnimationManager.runMachineAnimation(clickedBlock, machine);
    				}
    				
    				if (dropTasks.containsKey(clickedBlock)) {
    			        int taskId = dropTasks.get(clickedBlock);
    			        plugin.getServer().getScheduler().cancelTask(taskId);
    			        
    			        int newTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> machineDropItem(clickedBlock, litrosAtuais, stacks, machineKey), FileManager.getMainConfig().getLong("drop_ticks"));
    			        dropTasks.put(clickedBlock, newTaskId);
    			    } else {
    			        int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> machineDropItem(clickedBlock, litrosAtuais, stacks, machineKey), FileManager.getMainConfig().getLong("drop_ticks"));
    			        dropTasks.put(clickedBlock, taskId);
    			    }
    			    
			    	machine.changeActive(true);
    			}
    			
    			int totalCharacters = 20;
    			
				double percentageA = (double) litrosAtuais/litrosMax;
	
				int charactersA = (int) Math.round(percentageA * totalCharacters);
				int charactersB = totalCharacters - charactersA;;
	
				String color;
				if (percentageA > .50) {
				    color = "§a";
				} else if (percentageA <= .50 && percentageA > .25) {
				    color = "§e";
				} else {
				    color = "§c";
				}
	
				StringBuilder lineBuilder = new StringBuilder(color);
				for (int i = 0; i < charactersA; i++) {
				    lineBuilder.append("┃");
				}
				lineBuilder.append("§7");
				for (int i = 0; i < charactersB; i++) {
				    lineBuilder.append("┃");
				}
				lineBuilder.append(color);
	
				String bar = lineBuilder.toString();

				List<String> hdData = new ArrayList<>();
    			
    			for (String line : FileManager.getMessagesConfig().getStringList("hds.machine")) {
    				hdData.add(line
    						.replace("&", "§")
    						.replace("{player_name}", ownerPlayer.getName())
    						.replace("{stacks}", "" + stacks)
    						.replace("{prefix}", playerPrefix)
    						.replace("{litros_bar}", bar));
    			}
				
				HolographicDisplay.createHD(hdId, hdData, location);

				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> HolographicDisplay.removeHD(hdId), 250L);
    		}
    		
    		return;
    	}
    	
    	RefinariaData refinaria = MachinesManager.getRefinariaAt(location);
    	handleRefinariaHD(refinaria, location, clickedBlock);
    	
    }
    
    private void handleRefinariaHD(RefinariaData refinaria, Location location, Block refinariaBlock) {
    	if (HolographicDisplay.existStandNext(location)) {
			HolographicDisplay.removeStandNext(location);
		}
		
		UUID ownerUUID = refinaria.getOwnerUUID();
		
		String hdId = "hd:" + ownerUUID.toString() + ":" + refinaria.getRefinariaId();
		
		if (HolographicDisplay.existsHd(hdId)) {
			return;
		}
		
		int combustivelAmount = refinaria.getCombustivelAmount();
		int combustivelMax = refinaria.getCombustivelMax();
		boolean active = refinaria.isActive();

		if (combustivelAmount > 0 && !active) {
			String combustivelKey = refinaria.getCombustivelAtualKey();
			
			if (MachinesManager.existsPlayerInRaio(location, ANIMATION_DISTANCE)) {
				AnimationManager.runRefinariaAnimation(refinariaBlock, refinaria);
			}
			
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> refinariaDropItem(refinariaBlock, combustivelKey, combustivelAmount), FileManager.getMainConfig().getLong("refinaria_drop"));
			
			refinaria.changeActive(true);
		}
		
		int totalCharacters = 20;
		double percentageA = (double) combustivelAmount/combustivelMax;

		int charactersA = (int) Math.round(percentageA * totalCharacters);
		int charactersB = totalCharacters - charactersA;;

		String color;
		if (percentageA > .50) {
		    color = "§a";
		} else if (percentageA <= .50 && percentageA > .25) {
		    color = "§e";
		} else {
		    color = "§c";
		}

		StringBuilder lineBuilder = new StringBuilder(color);
		for (int i = 0; i < charactersA; i++) {
		    lineBuilder.append("┃");
		}
		lineBuilder.append("§7");
		for (int i = 0; i < charactersB; i++) {
		    lineBuilder.append("┃");
		}
		lineBuilder.append(color);

		String bar = lineBuilder.toString();
		
		OfflinePlayer ownerPlayer = Bukkit.getOfflinePlayer(ownerUUID);
		String playerPrefix = PermissionsManager.getUser(ownerPlayer.getUniqueId().toString()).getCachedData().getMetaData().getPrefix().replace("&", "§");
		
		List<String> hdData = new ArrayList<>();
		
		String statusMsg = active ? FileManager.getMessage("hds.refinaria_status_on") : FileManager.getMessage("hds.refinaria_status_off");
		
		for (String line : FileManager.getMessagesConfig().getStringList("hds.refinaria")) {
			hdData.add(line
					.replace("&", "§")
					.replace("{player_name}", ownerPlayer.getName())
					.replace("{prefix}", playerPrefix)
					.replace("{status}", statusMsg)
					.replace("{combustiveis_bar}", bar)
					);
		}
		
		HolographicDisplay.createHD(hdId, hdData, location);
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> HolographicDisplay.removeHD(hdId), 250L);
    }
	
	private void machineDropItem(Block machineBlock, int litrosAtuais, int stacks, String machineKey) {
		MachineData machine = MachinesManager.getMachineAt(machineBlock.getLocation());
		
		if (litrosAtuais < 1 || machine == null || litrosAtuais < stacks) {
			return;
		}
		
		litrosAtuais -= stacks;

		ItemStack machineDrop = ItemManager.getMachineDrop(machineKey);
		machineDrop.setAmount(stacks);
		
		Location dropLocation = machineBlock.getLocation().clone().add(0.5, 1, 0.5);
		
		if (MachinesManager.existsPlayerInRaio(dropLocation, ANIMATION_DISTANCE)) {
			AnimationManager.runMachineAnimation(machineBlock, machine);
		}
		
		machineBlock.getWorld().dropItemNaturally(dropLocation, machineDrop);
		machine.fastUpdateLitrosAmount(litrosAtuais);
		
		if (litrosAtuais < 1) {
			machine.changeActive(false);
			dropTasks.remove(machineBlock);
		}

		final int litros = litrosAtuais;
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {	
			@Override
			public void run() {
				machineDropItem(machineBlock, litros, stacks, machineKey);
			}
		}, FileManager.getMainConfig().getLong("drop_ticks"));
		
	}
	
	private void refinariaDropItem(Block refinariaBlock, String combustivelKey, int combustivelSize) {
		RefinariaData refinaria = MachinesManager.getRefinariaAt(refinariaBlock.getLocation());
		
		if ((combustivelSize < 1) || refinaria == null) {
			return;
		}
		
		combustivelSize -= 1;
		refinaria.fastChangeCombustivelAmount(combustivelSize);
		
		Location location = refinariaBlock.getLocation().clone().add(0.5, 1, 0.5);
		
		if (MachinesManager.existsPlayerInRaio(location, ANIMATION_DISTANCE)) {
			AnimationManager.runRefinariaAnimation(refinariaBlock, refinaria);
		}
		
		ItemStack dropItem = ItemManager.getCombustivelRefinadoItem(combustivelKey);
		
		refinariaBlock.getWorld().dropItemNaturally(location, dropItem);
		
		if (combustivelSize < 1) {
			refinaria.changeCombustivelAmount(0);
			refinaria.changeActive(false);
			dropTasks.remove(refinariaBlock);
		}
		
		final int size = combustivelSize;
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {	
			@Override
			public void run() {
				refinariaDropItem(refinariaBlock, combustivelKey, size);
				
			}
		}, FileManager.getMainConfig().getLong("refinaria_drop"));
	}
	
	private static boolean isAdmin(Player player) {
		return player.hasPermission("hmachines.admin");
	}
}
