package com.dmastech.markets.tasks;

import com.dmastech.markets.Utils;
import com.dmastech.markets.enums.SignType;
import com.dmastech.markets.helpers.SignHelper;
import com.dmastech.markets.managers.ConfigManager;
import com.dmastech.markets.managers.DataManager;
import com.dmastech.markets.objects.MarketItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.scheduler.BukkitTask;

public class PriceUpdateTask implements Runnable {
    private BukkitTask task;

    public void setTask(BukkitTask task) {
        this.task = task;
    }

    public void cancelTask() {
        task.cancel();
    }

    @Override
    public void run() {
        for (MarketItem marketItem : DataManager.getMarketItems()) {
            if (marketItem.getBuyPrice() < marketItem.getBaseBuyPrice()) {
                double basePrice = marketItem.getBaseBuyPrice();
                double newPrice = marketItem.getBuyPrice() + marketItem.getPriceChange();

                if (newPrice > basePrice) {
                    newPrice = basePrice;
                }

                marketItem.setBuyPrice(newPrice);
                marketItem.updateSigns(SignType.BUY);
            }

            if (marketItem.getSellPrice() < marketItem.getBaseSellPrice()) {
                double basePrice = marketItem.getBaseSellPrice();
                double newPrice = marketItem.getSellPrice() + marketItem.getPriceChange();

                if (newPrice > basePrice) {
                    newPrice = basePrice;
                }

                marketItem.setSellPrice(newPrice);
                marketItem.updateSigns(SignType.SELL);
            }
        }
    }
}
