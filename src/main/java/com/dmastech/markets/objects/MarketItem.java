package com.dmastech.markets.objects;

import com.destroystokyo.paper.event.entity.WitchReadyPotionEvent;
import com.dmastech.markets.Utils;
import com.dmastech.markets.enums.SignType;
import com.dmastech.markets.helpers.SignHelper;
import com.dmastech.markets.managers.ConfigManager;
import com.dmastech.markets.managers.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;

import java.util.List;

public class MarketItem {

    private Material item;

    private double baseBuyPrice;
    private double baseSellPrice;
    private double buyPrice;
    private double sellPrice;
    private double change;

    public MarketItem(Material type) {
        this.item = type;
        this.baseBuyPrice = ConfigManager.buyPrices.get(type.toString());
        this.baseSellPrice = ConfigManager.sellPrices.get(type.toString());
        this.buyPrice = DataManager.getBuyPrice(type);
        this.sellPrice = DataManager.getSellPrice(type);
        this.change = ConfigManager.change.get(type.toString());
    }

    public MarketItem(Material type, double buyPrice, double sellPrice) {
        this.item = type;
        this.baseBuyPrice = ConfigManager.buyPrices.get(type.toString());
        this.baseSellPrice = ConfigManager.sellPrices.get(type.toString());
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.change = ConfigManager.change.get(type.toString());
    }

    public Material getItem() {
        return item;
    }

    public double getBaseBuyPrice() {
        return baseBuyPrice;
    }

    public void setBaseBuyPrice(double baseBuyPrice) {
        this.baseBuyPrice = baseBuyPrice;
    }

    public double getBaseSellPrice() {
        return baseSellPrice;
    }

    public void setBaseSellPrice(double baseSellPrice) {
        this.baseSellPrice = baseSellPrice;
    }

    public double getPriceChange() {
        return change;
    }

    public void setPriceChange(double change) {
        this.change = change;
    }

    public void setBuyPrice(double price) {
        this.buyPrice = price;

        updateDataStore();
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public void setSellPrice(double price) {
        this.sellPrice = price;

        updateDataStore();
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public void handleChange(int amount, SignType type) {
        if (type.equals(SignType.BUY) && !ConfigManager.General.EnableBuyScaling) return;
        if (type.equals(SignType.SELL) && !ConfigManager.General.EnableSellScaling) return;

        double price = 0;
        double minPrice = 0;

        switch (type) {
            case BUY:
                price = buyPrice;
                minPrice = (baseBuyPrice / 2);
                break;
            case SELL:
                price = sellPrice;
                minPrice = (baseSellPrice / 4);
                break;
        }

        double newPrice = price - (change * amount);

        if (newPrice < minPrice) {
            newPrice = minPrice;
        }

        switch (type) {
            case BUY:
                buyPrice = newPrice;
                break;
            case SELL:
                sellPrice = newPrice;
                break;
    }

        updateDataStore();
    }

    public void updateSigns() {
        if (ConfigManager.General.EnableBuyScaling) updateSigns(SignType.BUY);
        if (ConfigManager.General.EnableSellScaling) updateSigns(SignType.SELL);
    }

    public void updateSigns(SignType type) {
        if (type.equals(SignType.BUY) && !ConfigManager.General.EnableBuyScaling) return;
        if (type.equals(SignType.SELL) && !ConfigManager.General.EnableSellScaling) return;

        List<String> locStrings = null;

        double price = 0;

        switch (type) {
            case BUY:
                locStrings = DataManager.getBuySigns(item);
                price = buyPrice;
                break;
            case SELL:
                locStrings = DataManager.getSellSigns(item);
                price = sellPrice;
        }

        if (locStrings == null) return;

        for (String locString : locStrings) {
            Location signLoc = Utils.getLocationFromString(locString);

            if (!signLoc.getBlock().getType().toString().contains("_SIGN")) {
                continue;
            }

            Sign sign = (Sign) signLoc.getBlock().getState();

            int amount = SignHelper.getAmount(sign.getLines());

            sign.setLine(3, SignHelper.formatPrice(price * amount));

            sign.update();
        }
    }

    private void updateDataStore() {
        DataManager.setBuyPrice(item, buyPrice);
        DataManager.setSellPrice(item, sellPrice);
    }
}
