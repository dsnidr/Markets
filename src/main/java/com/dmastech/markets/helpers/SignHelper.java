package com.dmastech.markets.helpers;

import com.dmastech.markets.Utils;
import com.dmastech.markets.managers.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;

import java.text.DecimalFormat;

public class SignHelper {

    public static boolean itemValid(String line) {
        return ConfigManager.materials.contains(translateItemType(line));
    }

    public static boolean amountValid(String line) {
        try {
            Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    public static int getAmount(String[] lines) {
        try {
            return Integer.parseInt(lines[1]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static double getPrice(String[] lines) {
        try {
            return Double.parseDouble(lines[3].replace("$", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public static Material getMaterial(String[] lines) {
        return Material.getMaterial(translateItemType(lines[2]));
    }

    public static boolean validateSign(SignChangeEvent event) {
        String[] lines = event.getLines();

        boolean error = false;

        if (!amountValid(lines[1])) {
            event.setLine(1, ChatColor.RED + "Invalid amount");

            error = true;
        }

        if (!itemValid(lines[2])) {
            event.setLine(2, ChatColor.RED + "Invalid item");

            error = true;
        }

        if (error) {
            event.setLine(0, ChatColor.RED + ChatColor.stripColor(lines[0]));

            return false;
        }

        return true;
    }

    public static String formatPrice(double unformatted) {
        return "$" + new DecimalFormat("0.00").format(unformatted);
    }

    public static void updateSellPrice(Sign sign, double oldPrice, double changeAmount, int amount, String type) {
        double newPrice = oldPrice - changeAmount * amount;
        double minPrice = (ConfigManager.sellPrices.get(type) / 4) * amount;

        if (newPrice < minPrice) {
            newPrice = minPrice;
        }

        sign.setLine(3, SignHelper.formatPrice(newPrice));
        sign.update();
    }

    public static String translateItemType(String input) {
        return input.replace(" ", "_").toUpperCase();
    }
}
