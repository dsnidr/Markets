package com.dmastech.markets;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Utils {

    public static String getStringFromLocation(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    public static Location getLocationFromString(String locString) {
        String split[] = locString.split(",");

        String world = split[0];
        int x = Integer.parseInt(split[1]);
        int y = Integer.parseInt(split[2]);
        int z = Integer.parseInt(split[3]);

        return Bukkit.getWorld(world).getBlockAt(x, y, z).getLocation();
    }

    public static String getColourizedString(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static boolean isMaterial(String matString) {
        return Material.getMaterial(matString) != null;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }
}
