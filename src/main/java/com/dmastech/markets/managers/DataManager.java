package com.dmastech.markets.managers;

import com.dmastech.markets.Markets;
import com.dmastech.markets.Utils;
import com.dmastech.markets.objects.MarketItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class DataManager {
    private static List<String> buySigns = new ArrayList<>();
    private static List<String> sellSigns = new ArrayList<>();
    private static HashMap<Material, List<String>> matBuySigns = new HashMap<>();
    private static HashMap<Material, List<String>> matSellSigns = new HashMap<>();
    private static HashMap<Material, Double> buyPrices = new HashMap<>();
    private static HashMap<Material, Double> sellPrices = new HashMap<>();
    private static HashMap<Material, MarketItem> items = new HashMap<>();

    public static void registerBuySign(Location signLoc, Material type) {
        registerBuySign(Utils.getStringFromLocation(signLoc), type);
    }

    public static void registerBuySign(String locString, Material type) {
        if (buySigns.contains(locString)) return;

        buySigns.add(locString);
        registerMatBuySign(locString, type);
    }

    public static void registerSellSign(Location signLoc, Material type) {
        registerSellSign(Utils.getStringFromLocation(signLoc), type);
    }

    public static void registerSellSign(String locString, Material type) {
        if (sellSigns.contains(locString)) return;

        sellSigns.add(locString);
        registerMatSellSign(locString, type);
    }

    private static void registerMatBuySign(String locString, Material type) {
        List<String> list = null;

        if (matBuySigns.containsKey(type)) {
            list = matBuySigns.get(type);
        }

        else {
            list = new ArrayList<>();
        }

        if (list.contains(locString)) return;

        list.add(locString);

        matBuySigns.put(type, list);
    }

    private static void unregisterMatBuySign(String locString, Material type) {
        if (!matBuySigns.containsKey(type)) return;

        List<String> list = matBuySigns.get(type);

        if (!list.contains(locString)) return;

        list.remove(locString);

        matBuySigns.put(type, list);
    }

    private static void registerMatSellSign(String locString, Material type) {
        List<String> list = null;

        if (matSellSigns.containsKey(type)) {
            list = matSellSigns.get(type);
        }

        else {
            list = new ArrayList<>();
        }

        if (list.contains(locString)) return;

        list.add(locString);

        matSellSigns.put(type, list);
    }

    private static void unregisterMatSellSign(String locString, Material type) {
        if (!matSellSigns.containsKey(type)) return;

        List<String> list = matSellSigns.get(type);

        if (!list.contains(locString)) return;

        list.remove(locString);

        matSellSigns.put(type, list);
    }

    public static void unregisterSellSign(Location signLoc, Material type) {
        unregisterSellSign(Utils.getStringFromLocation(signLoc), type);
    }

    public static void unregisterSellSign(String locString, Material type) {
        if (!sellSigns.contains(locString)) return;

        sellSigns.remove(locString);
        unregisterMatSellSign(locString, type);
    }

    public static void unregisterBuySign(Location signLoc, Material type) {
        unregisterBuySign(Utils.getStringFromLocation(signLoc), type);
    }

    public static void unregisterBuySign(String locString, Material type) {
        if (!buySigns.contains(locString)) return;

        buySigns.remove(locString);
        unregisterMatBuySign(locString, type);
    }

    public static List<String> getBuySigns() {
        return buySigns;
    }

    public static List<String> getSellSigns() {
        return sellSigns;
    }

    public static List<String> getBuySigns(Material type) {
        return matBuySigns.get(type);
    }

    public static List<String> getSellSigns(Material type) {
        return matSellSigns.get(type);
    }

    public static void setBuyPrice(Material type, double price) {
        buyPrices.put(type, price);
    }

    public static void setSellPrice(Material type, double price) {
        sellPrices.put(type, price);
    }

    public static double getBuyPrice(Material type) {
        return buyPrices.get(type);
    }

    public static double getSellPrice(Material type) {
        return sellPrices.get(type);
    }

    public static MarketItem getMarketItem(Material type) {
        return items.get(type);
    }

    public static Collection<MarketItem> getMarketItems() {
        return items.values();
    }

    public static boolean marketItemExists(Material material) {
        return items.containsKey(material);
    }

    public static void registerMarketItem(Material material) {
        items.put(material, new MarketItem(material));
    }

    public static void load() throws IOException {
        File file = new File(Markets.getInstance().getDataFolder(), "data.yml");

        if (!file.exists()) {
            file.createNewFile();
        }

        FileConfiguration data = YamlConfiguration.loadConfiguration(file);

        buySigns = data.getStringList("BuySigns");
        sellSigns = data.getStringList("SellSigns");

        // Load sorted signs
        if (data.contains("Sorted")) {
            for (String key : data.getConfigurationSection("Sorted").getKeys(false)) {
                if (!Utils.isMaterial(key)) {
                    Markets.getConsole().sendMessage(ChatColor.RED + key + " is not a valid material (data.yml)");
                    continue;
                }

                Material type = Material.getMaterial(key);

                List<String> buyList = data.getStringList("Sorted." + key + ".Buy");
                List<String> sellList = data.getStringList("Sorted." + key + ".Sell");

                matBuySigns.put(type, buyList);
                matSellSigns.put(type, sellList);
            }
        }

        // Load buy prices
        if (data.contains("Prices.Buy")) {
            for (String key : data.getConfigurationSection("Prices.Buy").getKeys(false)) {
                if (!Utils.isMaterial(key)) {
                    Markets.getConsole().sendMessage(ChatColor.RED + key + " is not a valid material (data.yml)");
                    continue;
                }

                Material type = Material.getMaterial(key);

                buyPrices.put(type, data.getDouble("Prices.Buy." + key));
            }
        }

        else {
            // If no existing buy prices are found, load defaults
            for (String key : ConfigManager.buyPrices.keySet()) {
                if (!Utils.isMaterial(key)) {
                    Markets.getConsole().sendMessage(ChatColor.RED + key + " is not a valid material (data.yml)");
                    continue;
                }

                Material type = Material.getMaterial(key);

                buyPrices.put(type, ConfigManager.buyPrices.get(key));
            }
        }

        // Load sell prices
        if (data.contains("Prices.Sell")) {
            for (String key : data.getConfigurationSection("Prices.Sell").getKeys(false)) {
                if (!Utils.isMaterial(key)) {
                    Markets.getConsole().sendMessage(ChatColor.RED + key + " is not a valid material (data.yml)");
                    continue;
                }

                Material type = Material.getMaterial(key);

                sellPrices.put(type, data.getDouble("Prices.Sell." + key));
            }
        }

        else {
            // If no existing sell prices are found, load defaults
            for (String key : ConfigManager.sellPrices.keySet()) {
                if (!Utils.isMaterial(key)) {
                    Markets.getConsole().sendMessage(ChatColor.RED + key + " is not a valid material (data.yml)");
                    continue;
                }

                Material type = Material.getMaterial(key);

                sellPrices.put(type, ConfigManager.sellPrices.get(key));
            }
        }

        loadMarketItems();
    }

    public static void loadMarketItems() throws IOException {
        File file = new File(Markets.getInstance().getDataFolder(), "data.yml");

        if (!file.exists()) {
            file.createNewFile();
        }

        FileConfiguration data = YamlConfiguration.loadConfiguration(file);

        for (String key : ConfigManager.materials) {
            if (!Utils.isMaterial(key)) {
                Markets.getConsole().sendMessage(ChatColor.RED + key + " is not a valid material (config.yml)");
                continue;
            }

            Markets.getConsole().sendMessage("Trying to load " + key);

            Material material = Material.getMaterial(key);
            MarketItem item = null;

            if (!data.contains("Prices.Buy." + key) || !data.contains("Prices.Buy." + key)) {
                Markets.getConsole().sendMessage("Does not contain price data for " + key + ". Loading from config...");
                item = new MarketItem(material, ConfigManager.buyPrices.get(key), ConfigManager.sellPrices.get(key));
            }

            else {
                item = new MarketItem(material);
            }

            items.put(material, item);
        }
    }

    public static void save() throws IOException {
        File file = new File(Markets.getInstance().getDataFolder(), "data.yml");

        if (!Markets.getInstance().getDataFolder().exists()) {
            Markets.getInstance().getDataFolder().mkdirs();
        }

        if (!file.exists()) {
            file.createNewFile();
        }

        FileConfiguration data = YamlConfiguration.loadConfiguration(file);

        // Save general lists
        data.set("BuySigns", buySigns);
        data.set("SellSigns", sellSigns);

        // Save material specific buy signs
        for (Material key : matBuySigns.keySet()) {
            List<String> list = matBuySigns.get(key);

            data.set("Sorted." + key + ".Buy", list);
        }

        // Save material specific sell signs
        for (Material key : matSellSigns.keySet()) {
            List<String> list = matSellSigns.get(key);

            data.set("Sorted." + key + ".Sell", list);
        }

        // Save buy prices
        for (Material key : buyPrices.keySet()) {
            data.set("Prices.Buy." + key, buyPrices.get(key));
        }

        // Save sell prices
        for (Material key : sellPrices.keySet()) {
            data.set("Prices.Sell." + key, sellPrices.get(key));
        }

        data.save(file);
    }
}
