package com.dmastech.markets.managers;

import com.dmastech.markets.Markets;
import com.dmastech.markets.Utils;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static List<String> buySigns = new ArrayList<>();
    private static List<String> sellSigns = new ArrayList<>();

    public static void registerBuySign(Location signLoc) {
        registerBuySign(Utils.getStringFromLocation(signLoc));
    }

    public static void registerBuySign(String locString) {
        if (buySigns.contains(locString)) return;

        buySigns.add(locString);
    }

    public static void registerSellSign(Location signLoc) {
        registerSellSign(Utils.getStringFromLocation(signLoc));
    }

    public static void registerSellSign(String locString) {
        if (sellSigns.contains(locString)) return;

        sellSigns.add(locString);
    }

    public static void unregisterSellSign(Location signLoc) {
        unregisterSellSign(Utils.getStringFromLocation(signLoc));
    }

    public static void unregisterSellSign(String locString) {
        if (!sellSigns.contains(locString)) return;

        sellSigns.remove(locString);
    }

    public static void unregisterBuySign(Location signLoc) {
        unregisterBuySign(Utils.getStringFromLocation(signLoc));
    }

    public static void unregisterBuySign(String locString) {
        if (!buySigns.contains(locString)) return;

        buySigns.remove(locString);
    }

    public static List<String> getBuySigns() {
        return buySigns;
    }

    public static List<String> getSellSigns() {
        return sellSigns;
    }

    public static void load() throws IOException {
        File file = new File(Markets.getInstance().getDataFolder(), "data.yml");

        if (!file.exists()) {
            file.createNewFile();
        }

        FileConfiguration data = YamlConfiguration.loadConfiguration(file);

        buySigns = data.getStringList("BuySigns");
        sellSigns = data.getStringList("SellSigns");
    }

    public static void save() throws IOException {
        File file = new File(Markets.getInstance().getDataFolder(), "data.yml");

        if (!file.exists()) {
            file.createNewFile();
        }

        FileConfiguration data = YamlConfiguration.loadConfiguration(file);

        data.set("BuySigns", buySigns);
        data.set("SellSigns", sellSigns);

        data.save(file);
    }
}
