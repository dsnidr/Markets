package com.dmastech.markets.helpers;

import com.dmastech.markets.Markets;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;

import java.util.UUID;

public class EconomyHelper {

    public static boolean hasEnough(UUID uuid, double amount) {
        double balance = Markets.getEconomy().getBalance(Bukkit.getOfflinePlayer(uuid));

        return balance >= amount;
    }

    public static boolean tryTransaction(UUID uuid, double amount) {
        Economy economy = Markets.getEconomy();

        EconomyResponse response = null;

        if (amount < 0) {
            response = economy.withdrawPlayer(Bukkit.getOfflinePlayer(uuid), amount);
        }

        else {
            response = economy.depositPlayer(Bukkit.getOfflinePlayer(uuid), amount);
        }

        return response.transactionSuccess();
    }
}
