package com.dmastech.markets.listeners;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import com.dmastech.markets.Markets;
import com.dmastech.markets.Utils;
import com.dmastech.markets.enums.SignType;
import com.dmastech.markets.helpers.SignHelper;
import com.dmastech.markets.managers.ConfigManager;
import com.dmastech.markets.managers.DataManager;
import com.dmastech.markets.objects.MarketItem;
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

            MarketItem marketItem = DataManager.getMarketItem(Material.getMaterial(formattedItemName));

            int amount = SignHelper.getAmount(lines);

            event.setLine(0, Utils.getColourizedString(ConfigManager.Signs.BuyLineAfter));
            event.setLine(3, SignHelper.formatPrice(marketItem.getBuyPrice() * amount));

            DataManager.registerBuySign(event.getBlock().getLocation(), SignHelper.getMaterial(lines));
        }

        // Creating a sell sign
        else if (lines[0].equalsIgnoreCase(Utils.getColourizedString(ConfigManager.Signs.SellLineBefore))) {
            if (!Markets.getPermissions().has(player, "markets.sell.create") && !player.isOp()) return;

            boolean valid = SignHelper.validateSign(event);

            if (!valid) return;

            MarketItem marketItem = DataManager.getMarketItem(Material.getMaterial(formattedItemName));

            int amount = SignHelper.getAmount(lines);

            event.setLine(0, Utils.getColourizedString(ConfigManager.Signs.SellLineAfter));
            event.setLine(3, SignHelper.formatPrice(marketItem.getSellPrice() * amount));

            DataManager.registerSellSign(event.getBlock().getLocation(), SignHelper.getMaterial(lines));
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

            DataManager.unregisterBuySign(sign.getLocation(), SignHelper.getMaterial(lines));
        }

        // Deleting a sell sign
        else if (lines[0].equalsIgnoreCase(Utils.getColourizedString(ConfigManager.Signs.SellLineAfter))) {
            if (!Markets.getPermissions().has(player, "markets.sell.delete") && !player.isOp()) return;

            DataManager.unregisterSellSign(sign.getLocation(), SignHelper.getMaterial(lines));
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
            Material mat = SignHelper.getMaterial(lines);

            MarketItem marketItem = DataManager.getMarketItem(mat);

            if (!economy.has(player, marketItem.getBuyPrice())) {
                player.sendMessage(ChatColor.RED + "You can't afford this!");

                return;
            }

            EconomyResponse response = economy.withdrawPlayer(player, marketItem.getBuyPrice());

            if (!response.transactionSuccess()) {
                player.sendMessage(ChatColor.DARK_RED + "Something went wrong...");
                player.sendMessage(ChatColor.DARK_RED + "Please give this code to a staff member: MB1");

                return;
            }

            ItemStack item = new ItemStack(mat, amount);

            player.getInventory().addItem(item);

            ActionBarAPI.sendActionBar(player, ChatColor.GREEN + "" + ChatColor.BOLD + "You bought " + amount + " " + lines[2] + " for " + lines[3]);

            marketItem.handleChange(amount, SignType.BUY);
            marketItem.handleChange(amount, SignType.SELL);
            marketItem.updateSigns();
        }

        // Using a sell sign
        else if (lines[0].equalsIgnoreCase(Utils.getColourizedString(ConfigManager.Signs.SellLineAfter))) {
            if (!Markets.getPermissions().has(player, "markets.sell.use") && !player.isOp()) return;

            Economy economy = Markets.getEconomy();

            int amount = SignHelper.getAmount(lines);
            Material mat = SignHelper.getMaterial(lines);

            MarketItem marketItem = DataManager.getMarketItem(mat);

            ItemStack item = new ItemStack(mat, amount);

            if (!player.getInventory().containsAtLeast(new ItemStack(item.getType(), 1), amount)) {
                player.sendMessage(ChatColor.RED + "You do not have " + amount + " " + lines[2]);

                return;
            }

            EconomyResponse response = economy.depositPlayer(player, marketItem.getSellPrice());

            if (!response.transactionSuccess()) {
                player.sendMessage(ChatColor.DARK_RED + "Something went wrong...");
                player.sendMessage(ChatColor.DARK_RED + "Please give this code to a staff member: MS1");

                return;
            }

            player.getInventory().removeItem(item);

            ActionBarAPI.sendActionBar(player, ChatColor.GREEN + "" + ChatColor.BOLD + "You sold " + amount + " " + lines[2] + " for " + lines[3]);

            marketItem.handleChange(amount, SignType.SELL);
            marketItem.handleChange(amount, SignType.BUY);
            marketItem.updateSigns();
        }
    }
}
