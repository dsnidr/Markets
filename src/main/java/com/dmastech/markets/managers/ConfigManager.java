package com.dmastech.markets.managers;

import com.dmastech.markets.Markets;
import com.dmastech.markets.Utils;
import com.dmastech.markets.enums.SignType;
import com.dmastech.markets.objects.MarketItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
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

    public static File getConfigFile() {
        return new File(Markets.getInstance().getDataFolder(), "config.yml");
    }

    public static void saveConfig(FileConfiguration config) {
        try {
            config.save(getConfigFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setBuyPrice(Material type, double price) {
        buyPrices.put(type.toString(), price);

        FileConfiguration config = Markets.getInstance().getConfig();

        config.set("Prices." + type.toString() + ".Buy", price);

        if (!materials.contains(type)) {
            materials.add(type.toString());
        }

        if (!DataManager.marketItemExists(type)) {
            double defaultSell = price / 2;
            double defaultChange = price * 0.02;

            config.set("Prices." + type.toString() + ".Sell", defaultSell);
            config.set("Prices." + type.toString() + ".Change", defaultChange);

            saveConfig(config);

            sellPrices.put(type.toString(), defaultSell);
            change.put(type.toString(), defaultChange);

            DataManager.setBuyPrice(type, price);
            DataManager.setSellPrice(type, defaultSell);

            DataManager.registerMarketItem(type);
        }

        else {
            MarketItem marketItem = DataManager.getMarketItem(type);

            marketItem.setBuyPrice(price);
            marketItem.setBaseBuyPrice(price);
            marketItem.updateSigns(SignType.BUY);

            saveConfig(config);
        }
    }

    public static void setSellPrice(Material type, double price) {
        sellPrices.put(type.toString(), price);

        FileConfiguration config = Markets.getInstance().getConfig();

        config.set("Prices." + type.toString() + ".Sell", price);

        if (!materials.contains(type)) {
            materials.add(type.toString());
        }

        if (!DataManager.marketItemExists(type)) {
            double defaultBuy = price * 2;
            double defaultChange = defaultBuy * 0.02;

            config.set("Prices." + type.toString() + ".Buy", defaultBuy);
            config.set("Prices." + type.toString() + ".Change", defaultChange);

            saveConfig(config);

            buyPrices.put(type.toString(), defaultBuy);
            change.put(type.toString(), defaultChange);

            DataManager.setSellPrice(type, price);
            DataManager.setBuyPrice(type, defaultBuy);

            DataManager.registerMarketItem(type);
        }

        else {
            MarketItem marketItem = DataManager.getMarketItem(type);

            marketItem.setSellPrice(price);
            marketItem.setBaseSellPrice(price);
            marketItem.updateSigns(SignType.SELL);

            saveConfig(config);
        }
    }

    public static void setChangeAmount(Material type, double amount) {
        change.put(type.toString(), amount);

        FileConfiguration config = Markets.getInstance().getConfig();

        config.set("Prices." + type.toString() + ".Change", amount);

        if (!materials.contains(type)) {
            materials.add(type.toString());
        }

        if (!DataManager.marketItemExists(type)) {
            double defaultBuy = amount / 0.02;
            double defaultSell = defaultBuy / 2;

            config.set("Prices." + type.toString() + ".Buy", defaultBuy);
            config.set("Prices." + type.toString() + ".Sell", defaultSell);

            saveConfig(config);

            buyPrices.put(type.toString(), defaultBuy);
            sellPrices.put(type.toString(), defaultSell);

            DataManager.setSellPrice(type, defaultBuy);
            DataManager.setBuyPrice(type, defaultSell);

            DataManager.registerMarketItem(type);
        }

        else {
            MarketItem marketItem = DataManager.getMarketItem(type);

            marketItem.setPriceChange(amount);

            saveConfig(config);
        }
    }
}
