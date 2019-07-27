package com.dmastech.markets.commands;

import com.dmastech.markets.Markets;
import com.dmastech.markets.Utils;
import com.dmastech.markets.helpers.SignHelper;
import com.dmastech.markets.managers.ConfigManager;
import com.dmastech.markets.managers.DataManager;
import com.dmastech.markets.objects.MarketItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.IOException;

public class MarketsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.GREEN + "Markets " + ChatColor.DARK_GRAY + "version " + ChatColor.GREEN +
                    Markets.getPdf().getVersion() + ChatColor.DARK_GRAY + " by " + ChatColor.GREEN + "sniddunc");
        }

        else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!Markets.getPermissions().has(player, "markets.reload") && !player.isOp()) {
                player.sendMessage(ChatColor.RED + "You lack the required permissions to execute this command");
                return true;
            }

            ConfigManager.reload();

            try {
                DataManager.load();
            } catch (IOException e) {}

            player.sendMessage(ChatColor.GREEN + "Markets has been reloaded");
        }

        else if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
            if (!Markets.getPermissions().has(player, "markets.reset") && !player.isOp()) {
                player.sendMessage(ChatColor.RED + "You lack the required permissions to execute this command");
                return true;
            }

            for (MarketItem item : DataManager.getMarketItems()) {
                item.setSellPrice(item.getBaseSellPrice());
                item.setBuyPrice(item.getBaseBuyPrice());

                item.updateSigns();
            }

            player.sendMessage(ChatColor.GREEN + "Item prices have been reset");
        }

        else if (args.length == 3 && (args[0].equalsIgnoreCase("setbuy"))) {
            if (!Markets.getPermissions().has(player, "markets.setbuy") && !player.isOp()) {
                player.sendMessage(ChatColor.RED + "You lack the required permissions to execute this command");
                return true;
            }

            return handlePriceUpdate(args, player);
        }

        else if (args.length == 3 && (args[0].equalsIgnoreCase("setsell"))) {
            if (!Markets.getPermissions().has(player, "markets.setsell") && !player.isOp()) {
                player.sendMessage(ChatColor.RED + "You lack the required permissions to execute this command");
                return true;
            }

            return handlePriceUpdate(args, player);
        }

        else if (args.length == 3 && (args[0].equalsIgnoreCase("setchange"))) {
            if (!Markets.getPermissions().has(player, "markets.setchange") && !player.isOp()) {
                player.sendMessage(ChatColor.RED + "You lack the required permissions to execute this command");
                return true;
            }

            return handlePriceUpdate(args, player);
        }

        else {
            player.sendMessage(ChatColor.GREEN + "Markets " + ChatColor.DARK_GRAY + "version " + ChatColor.GREEN +
                    Markets.getPdf().getVersion() + ChatColor.DARK_GRAY + " by " + ChatColor.GREEN + "sniddunc");

            player.sendMessage(ChatColor.GREEN + "/markets reload");
            player.sendMessage(ChatColor.GREEN + "/markets reset");
            player.sendMessage(ChatColor.GREEN + "/markets setbuy <item> <price>");
            player.sendMessage(ChatColor.GREEN + "/markets setsell <item> <price>");
            player.sendMessage(ChatColor.GREEN + "/markets setchange <item> <price>");
        }

        return false;
    }

    public boolean handlePriceUpdate(String[] args, Player player) {
        String cmd = args[0].toLowerCase();
        String typeString = SignHelper.translateItemType(args[1]);

        if (!Utils.isMaterial(typeString)) {
            player.sendMessage(ChatColor.RED + args[1] + " is not a valid item");
            return true;
        }

        Material material = Material.getMaterial(args[1].toUpperCase());

        double price = Double.MIN_NORMAL;

        try {
            price = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Invalid price");
            return true;
        }

        if (cmd.equals("setbuy")) {
            ConfigManager.setBuyPrice(material, price);

            player.sendMessage(ChatColor.GREEN + "Buy price for " + material.toString() + " set to " + price);
        }

        else if (cmd.equals("setsell")) {
            ConfigManager.setSellPrice(material, price);

            player.sendMessage(ChatColor.GREEN + "Sell price for " + material.toString() + " set to " + price);
        }

        else if (cmd.equals("setchange")) {
            ConfigManager.setChangeAmount(material, price);

            player.sendMessage(ChatColor.GREEN + "Change amount for " + material.toString() + " set to " + price);
        }

        return false;
    }
}
