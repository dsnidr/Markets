package com.dmastech.markets.managers;

import com.dmastech.markets.Markets;
import com.dmastech.markets.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ConfigManager {

    public static class General {
        public static int AutosaveDelay;
        public static int PriceUpdateDelay;
        public static boolean EnableBuyScaling;
        public static boolean EnableSellScaling;
    }

    public static class Signs {
        public static String BuyLineBefore;
        public static String BuyLineAfter;
        public static String SellLineBefore;
        public static String SellLineAfter;
    }

    public static ArrayList<String> materials;
    public static HashMap<String, Double> buyPrices;
    public static HashMap<String, Double> sellPrices;
    public static HashMap<String, Double> change;

    public static void init() {
        FileConfiguration config = Markets.getInstance().getConfig();

        General.AutosaveDelay = config.getInt("General.AutosaveDelay") * 20;
        General.PriceUpdateDelay = config.getInt("General.PriceUpdateDelay") * 20;
        General.EnableBuyScaling = config.getBoolean("General.EnableBuyScaling");
        General.EnableSellScaling = config.getBoolean("General.EnableSellScaling");

        Signs.BuyLineBefore = config.getString("Signs.BuyLineBefore", "[Buy]");
        Signs.BuyLineAfter = config.getString("Signs.BuyLineAfter", "&1[Buy]");
        Signs.SellLineBefore = config.getString("Signs.SellLineBefore", "[Sell]");
        Signs.SellLineAfter = config.getString("Signs.SellLineAfter", "&1[Sell]");

        materials = new ArrayList<>();
        buyPrices = new HashMap<>();
        sellPrices = new HashMap<>();
        change = new HashMap<>();

        for (String matString : config.getConfigurationSection("Prices").getKeys(false)) {
            if (!Utils.isMaterial(matString)) {
                Markets.getConsole().sendMessage(ChatColor.DARK_RED + "Error: " + matString + " is not a valid item");
                continue;
            }

            materials.add(matString);

            String path = "Prices." + matString;

            double buyPrice = config.getDouble(path + ".Buy");
            double sellPrice = config.getDouble(path + ".Sell");
            double changeAmount = config.getDouble(path + ".Change");

            buyPrices.put(matString, buyPrice);
            sellPrices.put(matString, sellPrice);
            change.put(matString, changeAmount);
        }
    }

    public static void reload() {
        Markets.getInstance().reloadConfig();

        init();

        try {
            DataManager.loadMarketItems();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
