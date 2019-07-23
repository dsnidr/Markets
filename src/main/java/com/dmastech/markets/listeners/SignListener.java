package com.dmastech.markets.listeners;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import com.dmastech.markets.Markets;
import com.dmastech.markets.Utils;
import com.dmastech.markets.helpers.SignHelper;
import com.dmastech.markets.managers.ConfigManager;
import com.dmastech.markets.managers.DataManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SignListener implements Listener {

    @EventHandler
    public void onSignCreate(SignChangeEvent event) {
        Player player = event.getPlayer();

        String[] lines = event.getLines();
        String formattedItemName = SignHelper.translateItemType(lines[2]);

        // Creating a buy sign
        if (lines[0].equalsIgnoreCase(Utils.getColourizedString(ConfigManager.Signs.BuyLineBefore))) {
            if (!Markets.getPermissions().has(player, "markets.buy.create") && !player.isOp()) return;

            boolean valid = SignHelper.validateSign(event);

            if (!valid) return;

            int amount = SignHelper.getAmount(lines);

            event.setLine(0, Utils.getColourizedString(ConfigManager.Signs.BuyLineAfter));
            event.setLine(3, SignHelper.formatPrice(ConfigManager.buyPrices.get(formattedItemName) * amount));

            DataManager.registerBuySign(event.getBlock().getLocation());
        }

        // Creating a sell sign
        else if (lines[0].equalsIgnoreCase(Utils.getColourizedString(ConfigManager.Signs.SellLineBefore))) {
            if (!Markets.getPermissions().has(player, "markets.sell.create") && !player.isOp()) return;

            boolean valid = SignHelper.validateSign(event);

            if (!valid) return;

            int amount = SignHelper.getAmount(lines);

            event.setLine(0, Utils.getColourizedString(ConfigManager.Signs.SellLineAfter));
            event.setLine(3, SignHelper.formatPrice(ConfigManager.sellPrices.get(formattedItemName) * amount));

            DataManager.registerSellSign(event.getBlock().getLocation());
        }
    }

    @EventHandler
    public void onSignRemove(BlockBreakEvent event) {
        if (!event.getBlock().getType().toString().contains("SIGN")) return;

        Player player = event.getPlayer();

        Sign sign = (Sign) event.getBlock().getState();
        String[] lines = sign.getLines();

        // Deleting a buy sign
        if (lines[0].equalsIgnoreCase(Utils.getColourizedString(ConfigManager.Signs.BuyLineAfter))) {
            if (!Markets.getPermissions().has(player, "markets.buy.delete") && !player.isOp()) return;

            DataManager.unregisterBuySign(sign.getLocation());
        }

        // Deleting a sell sign
        else if (lines[0].equalsIgnoreCase(Utils.getColourizedString(ConfigManager.Signs.SellLineAfter))) {
            if (!Markets.getPermissions().has(player, "markets.sell.delete") && !player.isOp()) return;

            DataManager.unregisterSellSign(sign.getLocation());
        }
    }

    @EventHandler
    public void onSignUse(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (!event.getClickedBlock().getType().toString().contains("SIGN")) return;

        Player player = event.getPlayer();

        Sign sign = (Sign) event.getClickedBlock().getState();
        String lines[] = sign.getLines();

        // Using a buy sign
        if (lines[0].equalsIgnoreCase(Utils.getColourizedString(ConfigManager.Signs.BuyLineAfter))) {
            if (!Markets.getPermissions().has(player, "markets.buy.use") && !player.isOp()) return;

            Economy economy = Markets.getEconomy();

            int amount = SignHelper.getAmount(lines);
            double price = SignHelper.getPrice(lines);
            Material mat = SignHelper.getMaterial(lines);

            if (!economy.has(player, price)) {
                player.sendMessage(ChatColor.RED + "You can't afford this!");

                return;
            }

            EconomyResponse response = economy.withdrawPlayer(player, price);

            if (!response.transactionSuccess()) {
                player.sendMessage(ChatColor.DARK_RED + "Something went wrong...");
                player.sendMessage(ChatColor.DARK_RED + "Please give this code to a staff member: MB1");

                return;
            }

            ItemStack item = new ItemStack(mat, amount);

            player.getInventory().addItem(item);

            ActionBarAPI.sendActionBar(player, ChatColor.GREEN + "" + ChatColor.BOLD + "You bought " + amount + " " + lines[2] + " for " + lines[3]);
        }

        // Using a sell sign
        else if (lines[0].equalsIgnoreCase(Utils.getColourizedString(ConfigManager.Signs.SellLineAfter))) {
            if (!Markets.getPermissions().has(player, "markets.sell.use") && !player.isOp()) return;

            Economy economy = Markets.getEconomy();

            int amount = SignHelper.getAmount(lines);
            double price = SignHelper.getPrice(lines);
            Material mat = SignHelper.getMaterial(lines);

            ItemStack item = new ItemStack(mat, amount);

            if (!player.getInventory().containsAtLeast(new ItemStack(item.getType(), 1), amount)) {
                player.sendMessage(ChatColor.RED + "You do not have " + amount + " " + lines[2]);

                return;
            }

            EconomyResponse response = economy.depositPlayer(player, price);

            if (!response.transactionSuccess()) {
                player.sendMessage(ChatColor.DARK_RED + "Something went wrong...");
                player.sendMessage(ChatColor.DARK_RED + "Please give this code to a staff member: MS1");

                return;
            }

            player.getInventory().removeItem(item);

            ActionBarAPI.sendActionBar(player, ChatColor.GREEN + "" + ChatColor.BOLD + "You sold " + amount + " " + lines[2] + " for " + lines[3]);

            SignHelper.updateSellPrice(sign, price, ConfigManager.change.get(mat.toString()), amount, mat.toString());
        }
    }
}
