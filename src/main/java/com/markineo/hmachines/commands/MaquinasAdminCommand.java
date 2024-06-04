package com.markineo.hmachines.commands;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.markineo.hmachines.items.ItemManager;
import com.markineo.hmachines.util.FileManager;

public class MaquinasAdminCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hmachines.admin")) {
            sender.sendMessage(FileManager.getMessage("machines_messages.admin_permission"));
            return true;
        }

        String sintaxe = FileManager.getMessage("machines_messages.admin_sintaxe");

        if (args.length < 1) {
            sender.sendMessage(sintaxe);
            return true;
        }

        switch (args[0]) {
            case "adicionardrop":
                if (sender instanceof Player) {
                    adicionarDrop((Player) sender, args);
                } else {
                    sender.sendMessage("Este comando só pode ser executado por jogadores.");
                }
                break;
            case "removerdrop":
                removerDrop(sender, args);
                break;
            case "clearhds":
                if (sender instanceof Player) {
                    clearHDs((Player) sender);
                } else {
                    sender.sendMessage("Este comando só pode ser executado por jogadores.");
                }
                break;
            case "reload":
                reloadConfig(sender);
                break;
            case "add":
                addItem(sender, args);
                break;
            default:
                sender.sendMessage(FileManager.getMessage("machines_messages.subcommand_not_found"));
                break;
        }

        return true;
    }

    private void adicionarDrop(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§7Sintaxe incorreta. Use: §3/madmin adicionardrop <id da máquina> <porcentagem>§7.\n§7Use o comando §3segurando §7o item na sua mão§7.");
            return;
        }

        String key = args[1];

        FileConfiguration config = FileManager.getMachinesConfig();
        ConfigurationSection section = config.getConfigurationSection("maquinas").getConfigurationSection(key);

        if (section == null) {
            player.sendMessage(FileManager.getMessage("machines_messages.invalid_id"));
            return;
        }

        double porcentagem;
        try {
            String percentageString = args[2].replace("%", "");
            porcentagem = Double.parseDouble(percentageString) / 100;
        } catch (NumberFormatException e) {
            player.sendMessage("§7Porcentagem inválida. Use um número no formato X%.");
            return;
        }

        ItemStack item = player.getItemInHand();
        if (item == null || item.getType().equals(Material.AIR)) {
            player.sendMessage("§7Você precisa segurar um item na mão para adicionar um drop.");

            return;
        }

        String itemSerialized = serializeItem(item);

        int newDropId = 1;

        ConfigurationSection dropsSection = section.getConfigurationSection("drops");
        if (dropsSection != null) {
            newDropId = dropsSection.getKeys(false).stream()
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0) + 1;
        } else {
            dropsSection = section.createSection("drops");
        }

        ConfigurationSection dropsSectionx = dropsSection;
        double totalChance = dropsSectionx.getKeys(false)
            .stream()
            .mapToDouble(dkey -> dropsSectionx.getConfigurationSection(dkey).getDouble("chance", 0))
            .sum();

        if (totalChance + porcentagem > 1) {
            player.sendMessage("§7A soma das porcentagens deve somar exatamente §3100%§7. Com o valor inserido, a soma ultrapassa esse percentual.");

            return;
        }

        ConfigurationSection newDropSection = dropsSection.createSection(String.valueOf(newDropId));
        newDropSection.set("item", itemSerialized);
        newDropSection.set("chance", porcentagem);

        try {
            FileManager.saveMaquinasConfig(config);
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.sendMessage("§7Drop adicionado com sucesso à máquina '" + key + "' com " + porcentagem * 100 + "% de chance.");
    }

    private void removerDrop(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§7Sintaxe incorreta. Use: §3/madmin removedrop <id da máquina> <key do drop>§7.");
            return;
        }

        String machineKey = args[1];
        String dropKey = args[2];

        FileConfiguration config = FileManager.getMachinesConfig();
        ConfigurationSection section = config.getConfigurationSection("maquinas").getConfigurationSection(machineKey);

        if (section == null) {
            sender.sendMessage(FileManager.getMessage("machines_messages.invalid_id"));
            return;
        }

        ConfigurationSection dropsSection = section.getConfigurationSection("drops");
        if (dropsSection == null || !dropsSection.contains(dropKey)) {
            sender.sendMessage("§7Drop com a key '" + dropKey + "' não encontrado na máquina '" + machineKey + "'.");
            return;
        }

        dropsSection.set(dropKey, null);

        try {
            FileManager.saveMaquinasConfig(config);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sender.sendMessage("§7Drop com a key '" + dropKey + "' removido da máquina '" + machineKey + "' com sucesso.");
    }

    private String serializeItem(ItemStack item) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("item", item);

        return config.saveToString();
    }

    private void clearHDs(Player player) {
        removeArmorStandsInRadius(player.getLocation(), 10);
        player.sendMessage(FileManager.getMessage("machines_messages.hds_removed"));
    }

    private void reloadConfig(CommandSender sender) {
        FileManager.reloadConfigurations();
        sender.sendMessage(FileManager.getMessage("machines_messages.reload_message"));
    }

    private void addItem(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage("§7Uso correto&8: §3/madmin add <máquina/refinaria/fix/combustível> <id> <jogador> <quantidade>");
            return;
        }

        String type = args[1];
        String id = args[2];
        String playerName = args[3];
        String quantityString = args[4];

        if (!id.matches("\\d+")) {
            sender.sendMessage(FileManager.getMessage("machines_messages.invalid_id"));
            return;
        }

        if (!quantityString.matches("\\d+")) {
            sender.sendMessage("§cQuantidade inválida.");
            return;
        }

        int quantity = Integer.parseInt(quantityString);

        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            sender.sendMessage("§cJogador não encontrado.");
            return;
        }

        ItemStack item = getItemByType(type, id);
        if (item != null) {
            item.setAmount(quantity);
            targetPlayer.getInventory().addItem(item);
            sender.sendMessage(String.format("§7Foram entregues %d %s(s) para o jogador %s. (ID #§a%s§7)", quantity, type, playerName, id));
        } else {
            sender.sendMessage(FileManager.getMessage("machines_messages.subcommand_not_found"));
        }
    }

    private ItemStack getItemByType(String type, String id) {
        switch (type) {
            case "refinaria":
                return ItemManager.getRefinariaItem(id);
            case "combustível":
                return ItemManager.getCombustivelItem(id);
            case "máquina":
                return ItemManager.getMachineItem(id);
            case "fix":
                return ItemManager.getFixItem(id);
            default:
                return null;
        }
    }

    private void removeArmorStandsInRadius(Location location, double radius) {
        for (Entity entity : location.getWorld().getEntities()) {
            if (entity instanceof ArmorStand && entity.getLocation().distance(location) <= radius) {
                entity.remove();
            }
        }
    }
}
