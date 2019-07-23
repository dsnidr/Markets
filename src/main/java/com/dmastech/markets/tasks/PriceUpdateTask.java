package com.dmastech.markets.tasks;

import com.dmastech.markets.Utils;
import com.dmastech.markets.helpers.SignHelper;
import com.dmastech.markets.managers.ConfigManager;
import com.dmastech.markets.managers.DataManager;
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
        for (String locString : DataManager.getSellSigns()) {
            Location signLoc = Utils.getLocationFromString(locString);

            if (!signLoc.getBlock().getType().toString().contains("SIGN")) {
                continue;
            }

            Sign sign = (Sign) signLoc.getBlock().getState();
            String[] lines = sign.getLines();

            int amount = SignHelper.getAmount(lines);
            double price = SignHelper.getPrice(lines);
            String type = SignHelper.getMaterial(lines).toString();

            double basePrice = ConfigManager.sellPrices.get(type) * amount;
            double changeAmount = ConfigManager.change.get(type);

            if (price < basePrice) {
                price += changeAmount * amount;

                if (price > basePrice) {
                    price = basePrice;
                }

                sign.setLine(3, SignHelper.formatPrice(price));
                sign.update();
            }
        }
    }
}
