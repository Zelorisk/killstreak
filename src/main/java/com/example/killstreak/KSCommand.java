package com.example.killstreak;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.command.TabCompleter;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class KSCommand implements CommandExecutor, TabCompleter {
    private final KSPlugin plugin;
    private final KillstreakManager ksManager;
    private final BountyManager bountyManager;
    private final ConfigManager cfg;
    private final AbilitiesManager abilitiesManager;
    private final com.example.killstreak.gui.ConfigGUI configGUI;
    private final com.example.killstreak.gui.BountiesGUI bountiesGUI;
    private final com.example.killstreak.gui.RecipesGUI recipesGUI;
    private final EventManager eventManager;

    public KSCommand(KSPlugin plugin, KillstreakManager ksManager, BountyManager bountyManager, ConfigManager cfg, AbilitiesManager abilitiesManager, com.example.killstreak.gui.ConfigGUI configGUI, com.example.killstreak.gui.BountiesGUI bountiesGUI, com.example.killstreak.gui.RecipesGUI recipesGUI, EventManager eventManager) {
        this.plugin = plugin;
        this.ksManager = ksManager;
        this.bountyManager = bountyManager;
        this.cfg = cfg;
        this.abilitiesManager = abilitiesManager;
        this.configGUI = configGUI;
        this.bountiesGUI = bountiesGUI;
        this.recipesGUI = recipesGUI;
        this.eventManager = eventManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("/ks bounties | /ks config | /ks recipes | /ks admin | /ks forceevent | /ks giveitem <player> <item>");
            return true;
        }
                if (args[0].equalsIgnoreCase("giveitem")) {
                    if (!sender.hasPermission("ks.admin")) { sender.sendMessage("No permission"); return true; }
                    if (args.length < 3) { sender.sendMessage("Usage: /ks giveitem <player> <item>"); return true; }
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) { sender.sendMessage("Player not found"); return true; }
                    String item = args[2].toLowerCase();
                    org.bukkit.inventory.ItemStack stack = null;
                    if (item.equals("bountyblade")) {
                        stack = com.example.killstreak.CustomItems.createBountyBlade(plugin);
                                } else if (item.equals("voidcleaver")) {
                                    stack = com.example.killstreak.CustomItems.createVoidCleaver(plugin);
                                } else if (item.equals("celestialcloak")) {
                                    stack = com.example.killstreak.CustomItems.createCelestialCloak(plugin);
                    } else if (item.equals("mace")) {
                        stack = com.example.killstreak.CustomItems.createMace(plugin);
                    } else if (item.equals("phoenixfeather")) {
                        stack = com.example.killstreak.CustomItems.createPhoenixFeather(plugin);
                    } else if (item.equals("voidwalkerboots")) {
                        // stack = com.example.killstreak.CustomItems.createVoidwalkerBoots(plugin); // Removed Voidwalker Boots
                    } else if (item.equals("meteorstaff")) {
                        stack = com.example.killstreak.CustomItems.createMeteorStaff(plugin);
                    }
                    if (stack != null) {
                        target.getInventory().addItem(stack);
                        sender.sendMessage("Gave " + target.getName() + " a " + item);
                    } else {
                        sender.sendMessage("Unknown item. Valid: bountyblade, mace, phoenixfeather, voidwalkerboots, meteorstaff, voidcleaver, celestialcloak");
                    }
                    return true;
                }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "bounties":
                if (sender instanceof Player) {
                    bountiesGUI.open((Player)sender);
                    return true;
                } else {
                    sender.sendMessage("Console: Top bounties:");
                    int i = 1;
                    for (var e : bountyManager.topBounties(10)) {
                        sender.sendMessage(i++ + ". " + (Bukkit.getOfflinePlayer(e.getKey()).getName()) + " - " + e.getValue());
                    }
                    return true;
                }
            case "config":
                if (sender instanceof Player) { configGUI.open((Player)sender); return true; }
                sender.sendMessage("Only players can open config GUI");
                return true;
            case "recipes":
                if (sender instanceof Player) { recipesGUI.open((Player)sender); return true; }
                sender.sendMessage("Only players can view recipes GUI");
                return true;
            case "forceevent":
                if (!sender.hasPermission("ks.admin")) { sender.sendMessage("No permission"); return true; }
                eventManager.triggerEvent();
                sender.sendMessage(ChatColor.GREEN + "Event triggered");
                return true;
            case "admin":
                if (!sender.hasPermission("ks.admin")) { sender.sendMessage("No permission"); return true; }
                if (args.length >= 2 && args[1].equalsIgnoreCase("giveks")) {
                    if (args.length < 3) { sender.sendMessage("Usage: /ks admin giveks <player> [amount]"); return true; }
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null) { sender.sendMessage("Player not found"); return true; }
                    int amount = 1;
                    if (args.length >= 4) amount = Integer.parseInt(args[3]);
                    for (int i=0;i<amount;i++) ksManager.addKill(target, target);
                    sender.sendMessage("Gave " + amount + " kills to " + target.getName());
                    return true;
                }
                if (args.length >= 3 && args[1].equalsIgnoreCase("setbounty")) {
                    if (args.length < 4) { sender.sendMessage("Usage: /ks admin setbounty <player> <amount>"); return true; }
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null) { sender.sendMessage("Player not found"); return true; }
                    int amount = Integer.parseInt(args[3]);
                    bountyManager.setBounty(target.getUniqueId(), amount);
                    sender.sendMessage("Set bounty on " + target.getName() + " to " + amount);
                    return true;
                }
                sender.sendMessage("/ks admin giveks <player> [amount] | setbounty <player> <amount>");
                return true;
            default:
                sender.sendMessage("Unknown subcommand");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("bounties", "config", "recipes", "admin", "forceevent", "giveitem");
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("giveitem")) {
            return Arrays.asList("bountyblade", "mace", "phoenixfeather", "voidwalkerboots", "meteorstaff", "voidcleaver", "celestialcloak");
        }
        if (args.length >= 2 && args[0].equalsIgnoreCase("admin")) {
            if (args.length == 2) return Arrays.asList("giveks", "setbounty");
        }
        return Collections.emptyList();
    }
}
