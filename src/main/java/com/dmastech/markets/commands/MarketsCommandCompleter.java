package com.dmastech.markets.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MarketsCommandCompleter implements TabCompleter {
    private static List<String> subCommands;
    private static List<String> materialCommands;
    private static List<String> allMaterials;

    public MarketsCommandCompleter() {
        subCommands = new ArrayList<>();
        subCommands.add("reload");
        subCommands.add("reset");
        subCommands.add("setbuy");
        subCommands.add("setsell");
        subCommands.add("setchange");

        materialCommands = new ArrayList<>();

        materialCommands.add("setbuy");
        materialCommands.add("setsell");
        materialCommands.add("setchange");

        allMaterials = new ArrayList<>();
        for (Material material : Material.values()) {
            allMaterials.add(material.toString());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        List<String> options = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], subCommands, options);
        }

        else if (args.length == 2 && materialCommands.contains(args[0].toLowerCase())) {
            StringUtil.copyPartialMatches(args[1], allMaterials, options);
        }

        return options;
    }
}
