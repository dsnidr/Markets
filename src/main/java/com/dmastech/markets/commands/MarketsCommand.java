package com.dmastech.markets.commands;

import com.dmastech.markets.Markets;
import com.dmastech.markets.managers.ConfigManager;
import com.dmastech.markets.managers.DataManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class MarketsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.GREEN + "Markets " + ChatColor.DARK_GRAY + "version " + ChatColor.GREEN +
                    Markets.getPdf().getVersion() + ChatColor.DARK_GRAY + " by " + ChatColor.GREEN + "sniddunc");
        }

        else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!Markets.getPermissions().has(player, "markets.reload") && !player.isOp()) return true;
            ConfigManager.init();

            player.sendMessage(ChatColor.GREEN + "Markets has been reloaded");
        }

        return false;
    }
}
