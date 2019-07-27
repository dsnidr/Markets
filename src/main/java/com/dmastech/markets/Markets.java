package com.dmastech.markets;

import com.dmastech.markets.commands.MarketsCommand;
import com.dmastech.markets.commands.MarketsCommandCompleter;
import com.dmastech.markets.listeners.SignListener;
import com.dmastech.markets.managers.ConfigManager;
import com.dmastech.markets.managers.DataManager;
import com.dmastech.markets.tasks.AutosaveTask;
import com.dmastech.markets.tasks.PriceUpdateTask;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class Markets extends JavaPlugin {

    private static PluginDescriptionFile pdf;
    private static ConsoleCommandSender console;
    private static Markets instance;

    private static Economy economy;
    private static Permission permissions;

    @Override
    public void onEnable() {
        instance = this;

        pdf = this.getDescription();
        console = this.getServer().getConsoleSender();

        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdirs();
        }

        if (files() && commands() && listeners() && tasks() && permissions() && economy()) {
            console.sendMessage(ChatColor.GOLD + pdf.getName() + ChatColor.GREEN + " version "
                    + ChatColor.GOLD + pdf.getVersion() + ChatColor.GREEN + " has been enabled!");
        }

        else {
            console.sendMessage(ChatColor.DARK_RED + pdf.getName() + " could not be enabled!");
            this.setEnabled(false);
        }
    }

    @Override
    public void onDisable() {
        try {
            DataManager.save();
        } catch (IOException e) {
            e.printStackTrace();
        }

        console.sendMessage(ChatColor.YELLOW + pdf.getName() + " has been disabled!");
    }

    private boolean economy() {
        try {
            if (this.getServer().getPluginManager().getPlugin("Vault") == null) return false;

            RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);

            if (rsp == null) return false;

            economy = rsp.getProvider();
        } catch (Exception e) {
            console.sendMessage(ChatColor.RED + "Failed to setup economy!");
            return false;
        }

        return true;
    }

    private boolean permissions() {
        try {
            RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
            permissions = rsp.getProvider();

            if (permissions == null) return false;
        } catch (Exception e) {
            console.sendMessage(ChatColor.RED + "Failed to setup permissions!");
            return false;
        }

        return true;
    }

    private boolean files() {
        try {
            this.getConfig().options().copyDefaults(true);
            this.saveConfig();

            ConfigManager.init();

            DataManager.load();
        } catch (Exception e) {
            console.sendMessage(ChatColor.RED + "Failed to setup files!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean commands() {
        try {
            this.getCommand("markets").setExecutor(new MarketsCommand());
            this.getCommand("markets").setTabCompleter(new MarketsCommandCompleter());
        } catch (Exception e) {
            console.sendMessage(ChatColor.RED + "Failed to register commands!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean listeners() {
        try {
            this.getServer().getPluginManager().registerEvents(new SignListener(), this);
        } catch (Exception e) {
            console.sendMessage(ChatColor.RED + "Failed to implement listeners!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean tasks() {
        try {
            int autosaveTime = ConfigManager.General.AutosaveDelay;
            int priceUpdateTime = ConfigManager.General.PriceUpdateDelay;

            AutosaveTask at = new AutosaveTask();
            at.setTask(Bukkit.getScheduler().runTaskTimerAsynchronously(this, at, autosaveTime, autosaveTime));

            PriceUpdateTask put = new PriceUpdateTask();
            put.setTask(Bukkit.getScheduler().runTaskTimer(this, put, priceUpdateTime, priceUpdateTime));
        } catch (Exception e) {
            console.sendMessage(ChatColor.RED + "Failed to fire initial tasks!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    //

    public static Economy getEconomy() {
        return economy;
    }

    public static Permission getPermissions() {
        return permissions;
    }

    public static Markets getInstance() {
        return instance;
    }

    public static ConsoleCommandSender getConsole() {
        return console;
    }

    public static PluginDescriptionFile getPdf() {
        return pdf;
    }
}
