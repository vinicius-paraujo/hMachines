package com.markineo.hmachines.events;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import com.markineo.hmachines.Machines;
import com.markineo.hmachines.items.ItemManager;
import com.markineo.hmachines.util.FileManager;
import com.markineo.hmachines.util.ShopCollector;

import net.milkbowl.vault.economy.Economy;

public class PlayerChat implements Listener {
	private static HashMap<UUID, ShopCollector> playersCollector;
	
	private static Logger logger = Machines.getPluginLogger();
	private static Economy economy = Machines.getEconomy();;
	
	public PlayerChat() {
		PlayerChat.playersCollector = new HashMap<>();
	}
	
	public static void registerClick(Player player, ShopCollector collectorData) {
		playersCollector.put(player.getUniqueId(), collectorData);
	}
	
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (event.getPlayer() == null || event.getMessage() == null) {
			return;
		}
		
		Player player = event.getPlayer();
		String answer = event.getMessage();
		
        if (!playersCollector.containsKey(player.getUniqueId())) {
        	return;
        }
        
        if (answer.equalsIgnoreCase("cancelar")) {
        	player.sendMessage(FileManager.getMessage("machines_messages.loja_cancel"));
        	playersCollector.remove(player.getUniqueId());
        	
        	event.setCancelled(true);
        	return;
    	}
        
        ShopCollector userData = playersCollector.get(player.getUniqueId());
        if (userData == null) {
        	return;
		}
        
        switch (userData.getStage()) {
        case 0:
        	handleStageZero(player, userData, answer);
        	
        	break;
        case 1:
        	handleStageOne(player, userData, answer);
        	
        	break;
    	default:
    		logger.info("Existe um erro na loja do plugin, por favor, contate o desenvolvedor do plugin.");
    		break;
        }
        
        event.setCancelled(true);
	}
	
	private void handleStageZero(Player player, ShopCollector userData, String answer) {
		if (!answer.matches("\\d+")) {
			player.sendMessage(FileManager.getMessage("machines_messages.not_a_number"));
			playersCollector.remove(player.getUniqueId());
			
			return;
		}
		
		String key = userData.getKey();
		String produto = userData.getProduct();
		
		int size = Integer.parseInt(answer);
		double basePrice = getItemPrice(produto, key);
		
		double finalPrice = (basePrice * size);
		double playerBalance = economy.getBalance(player);
		
		if (playerBalance < finalPrice) {
			playersCollector.remove(player.getUniqueId());
			player.sendMessage(FileManager.getMessage("machines_messages.money_insuficient"));
			
			return;
		}
		
		userData.updateSize(size);
		userData.updateStage(1);
		
		String msg = FileManager.getMessage("machines_messages.loja_confirm_msg")
				.replace("{quantidade}", "" + size)
				.replace("{produto}", formatarProduto(produto, size))
				.replace("{valor}", formatarPreco(finalPrice));
		player.sendMessage(msg);
		
	}
	
	private void handleStageOne(Player player, ShopCollector userData, String answer) {
		if (!answer.equalsIgnoreCase("confirmar")) {
        	player.sendMessage(FileManager.getMessage("machines_messages.loja_cancel"));
        	playersCollector.remove(player.getUniqueId());
        	
        	return;
		}
		
		String key = userData.getKey();
		String produto = userData.getProduct();
		
		int size = userData.getSize();
		double basePrice = getItemPrice(produto, key);
		
		double finalPrice = (basePrice * size);
		double playerBalance = economy.getBalance(player);
		
		if (playerBalance < finalPrice) {
			playersCollector.remove(player.getUniqueId());
			player.sendMessage(FileManager.getMessage("machines_messages.money_insuficient"));
			
			return;
		}
		
		ItemStack item = getProductItem(produto, key, size);
		if (item == null) {
			player.sendMessage("§cOcorreu um erro ao gerar o seu item, contate um administrador.");
		}
		
		economy.withdrawPlayer(player, finalPrice);
		player.getInventory().addItem(item);
		playersCollector.remove(player.getUniqueId());
		
		String msg = FileManager.getMessage("machines_messages.loja_success_msg")
				.replace("{quantidade}", "" + size)
				.replace("{produto}", formatarProduto(produto, size))
				.replace("{valor}", formatarPreco(finalPrice));
		player.sendMessage(msg);
	}
	
	private String formatarProduto(String produto, int size) {
	    String singular, plural;

	    switch (produto.toLowerCase()) {
	        case "maquina":
	            singular = "máquina";
	            plural = "máquinas";
	            break;
	        case "combustivel":
	            singular = "combustível";
	            plural = "combustíveis";
	            break;
	        case "refinaria":
	        	singular = produto;
	        	plural = "refinarias";
	        case "fix":
	            singular = "conserta máquina";
	            plural = singular;
	            break;
	        default:
	        	singular = produto;
	        	plural = produto;
	        	break;
	    }

	    return size == 1 ? singular : plural;
	}
	
	private double getItemPrice(String product, String key) {
		double price;
		
		switch (product.toLowerCase()) {
		case "combustivel":
			price = ItemManager.getCombustivelPrice(key);
			break;
		case "maquina":
			price = ItemManager.getMachinePrice(key);
			break;
		case "refinaria":
			price = ItemManager.getRefinariaPrice(key);
			break;
		case "fix":
			price = ItemManager.getFixPrice(key);
			break;
		default:
			logger.info("Existe um erro na loja, ocorreu a tentativa de obter o preço de um produto não registrado, verifique os menus de compra e busque por '" + product + "', esse item está escrito errado.");
			price = -1;
			break;
		}
		
		return price;
	};
	
	private ItemStack getProductItem(String product, String key, int size) {
		ItemStack item;
		
		switch (product.toLowerCase()) {
		case "combustivel":
			item = ItemManager.getCombustivelItem(key);
			item.setAmount(size);
			break;
		case "maquina":
			item = ItemManager.getMachineItem(key);
			item.setAmount(size);
			break;
		case "refinaria":
			item = ItemManager.getRefinariaItem(key);
			item.setAmount(size);
			break;
		case "fix":
			item = ItemManager.getFixItem(key);
			item.setAmount(size);
			break;
		default:
			logger.info("Existe um erro na loja, ocorreu a tentativa de obter um item de um produto não registrado, verifique os menus de compra e busque por '" + product + "', esse item está escrito errado.");
			item = null;
			break;
		}
		
		return item;
	};
	
	private String formatarPreco(double valor) {
	    return String.format("%.2f", valor);
	}
}
