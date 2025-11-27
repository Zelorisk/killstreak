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
            sender.sendMessage("/ks bounties | /ks config | /ks recipes | /ks admin | /ks forceevent | /ks giveitem <player> <item> | /ks craftitem <item> | /ks essence");
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
                    } else if (item.equals("dashcrystal")) {
                        stack = com.example.killstreak.CustomItems.createDashCrystal(plugin);
                    }
                    if (stack != null) {
                        target.getInventory().addItem(stack);
                        sender.sendMessage("Gave " + target.getName() + " a " + item);
                    } else {
                        sender.sendMessage("Unknown item. Valid: bountyblade, mace, phoenixfeather, voidwalkerboots, meteorstaff, voidcleaver, celestialcloak, dashcrystal");
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("craftitem")) {
                    if (!(sender instanceof Player)) { sender.sendMessage("Only players can craft items"); return true; }
                    if (args.length < 2) { sender.sendMessage("Usage: /ks craftitem <item>"); return true; }
                    Player p = (Player) sender;
                    String item = args[1].toLowerCase();
                    org.bukkit.inventory.ItemStack result = null;
                    boolean hasMaterials = false;
                    // Check craft limit first
                    DropItemListener dropListener = plugin.getDropItemListener();
                    if (dropListener != null && !dropListener.canCraft(item)) {
                        p.sendMessage("\u00A7cNo more " + item + "s can be crafted!");
                        return true;
                    }
                    // Check materials and craft
                    if (item.equals("bountyblade")) {
                        hasMaterials = hasItems(p, org.bukkit.Material.NETHERITE_INGOT, 3) && hasItems(p, org.bukkit.Material.STICK, 1) && hasItems(p, org.bukkit.Material.BLAZE_POWDER, 1);
                        if (hasMaterials) {
                            removeItems(p, org.bukkit.Material.NETHERITE_INGOT, 3);
                            removeItems(p, org.bukkit.Material.STICK, 1);
                            removeItems(p, org.bukkit.Material.BLAZE_POWDER, 1);
                            result = com.example.killstreak.CustomItems.createBountyBlade(plugin);
                        }
                    } else if (item.equals("mace")) {
                        hasMaterials = hasItems(p, org.bukkit.Material.IRON_INGOT, 5) && hasItems(p, org.bukkit.Material.STICK, 3) && hasItems(p, org.bukkit.Material.NETHER_STAR, 1);
                        if (hasMaterials) {
                            removeItems(p, org.bukkit.Material.IRON_INGOT, 5);
                            removeItems(p, org.bukkit.Material.STICK, 3);
                            removeItems(p, org.bukkit.Material.NETHER_STAR, 1);
                            result = com.example.killstreak.CustomItems.createMace(plugin);
                        }
                    } else if (item.equals("phoenixfeather")) {
                        hasMaterials = hasItems(p, org.bukkit.Material.FEATHER, 2) && hasItems(p, org.bukkit.Material.GOLD_INGOT, 2) && hasItems(p, org.bukkit.Material.NETHER_STAR, 1);
                        if (hasMaterials) {
                            removeItems(p, org.bukkit.Material.FEATHER, 2);
                            removeItems(p, org.bukkit.Material.GOLD_INGOT, 2);
                            removeItems(p, org.bukkit.Material.NETHER_STAR, 1);
                            result = com.example.killstreak.CustomItems.createPhoenixFeather(plugin);
                        }
                    } else if (item.equals("meteorstaff")) {
                        hasMaterials = hasItems(p, org.bukkit.Material.FIRE_CHARGE, 1) && hasItems(p, org.bukkit.Material.BLAZE_POWDER, 2) && hasItems(p, org.bukkit.Material.BLAZE_ROD, 1) && hasItems(p, org.bukkit.Material.NETHER_STAR, 1);
                        if (hasMaterials) {
                            removeItems(p, org.bukkit.Material.FIRE_CHARGE, 1);
                            removeItems(p, org.bukkit.Material.BLAZE_POWDER, 2);
                            removeItems(p, org.bukkit.Material.BLAZE_ROD, 1);
                            removeItems(p, org.bukkit.Material.NETHER_STAR, 1);
                            result = com.example.killstreak.CustomItems.createMeteorStaff(plugin);
                        }
                    } else if (item.equals("voidcleaver")) {
                        hasMaterials = hasItems(p, org.bukkit.Material.NETHERITE_INGOT, 3) && hasItems(p, org.bukkit.Material.STICK, 2);
                        if (hasMaterials) {
                            removeItems(p, org.bukkit.Material.NETHERITE_INGOT, 3);
                            removeItems(p, org.bukkit.Material.STICK, 2);
                            result = com.example.killstreak.CustomItems.createVoidCleaver(plugin);
                        }
                    } else if (item.equals("celestialcloak")) {
                        hasMaterials = hasItems(p, org.bukkit.Material.DIAMOND, 3) && hasItems(p, org.bukkit.Material.NETHER_STAR, 1) && hasItems(p, org.bukkit.Material.GOLD_BLOCK, 3);
                        if (hasMaterials) {
                            removeItems(p, org.bukkit.Material.DIAMOND, 3);
                            removeItems(p, org.bukkit.Material.NETHER_STAR, 1);
                            removeItems(p, org.bukkit.Material.GOLD_BLOCK, 3);
                            result = com.example.killstreak.CustomItems.createCelestialCloak(plugin);
                        }
                    } else if (item.equals("dashcrystal")) {
                        hasMaterials = hasItems(p, org.bukkit.Material.AMETHYST_SHARD, 4) && hasItems(p, org.bukkit.Material.NETHER_STAR, 1);
                        if (hasMaterials) {
                            removeItems(p, org.bukkit.Material.AMETHYST_SHARD, 4);
                            removeItems(p, org.bukkit.Material.NETHER_STAR, 1);
                            result = com.example.killstreak.CustomItems.createDashCrystal(plugin);
                        }
                    }
                    if (hasMaterials && result != null && dropListener != null) {
                        dropListener.decrementCraft(item);
                    }
                    if (result != null) {
                        p.getInventory().addItem(result);
                        p.sendMessage("Crafted a " + item + "!");
                    } else if (!hasMaterials) {
                        p.sendMessage("\u00A7cYou don't have the required materials to craft " + item);
                    } else {
                        p.sendMessage("\u00A7cUnknown item to craft. Valid: bountyblade, mace, phoenixfeather, meteorstaff, voidcleaver, celestialcloak, dashcrystal");
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
                if (args.length >= 2 && args[1].equalsIgnoreCase("giveessence")) {
                    if (args.length < 4) { sender.sendMessage("Usage: /ks admin giveessence <player> <amount>"); return true; }
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null) { sender.sendMessage("Player not found"); return true; }
                    int amount = Integer.parseInt(args[3]);
                    plugin.getBloodEssenceManager().addEssence(target.getUniqueId(), amount);
                    sender.sendMessage("Gave " + amount + " Blood Essence to " + target.getName());
                    return true;
                }
                if (args.length >= 2 && args[1].equalsIgnoreCase("grace")) {
                    if (args.length < 3) { sender.sendMessage("Usage: /ks admin grace <on|off>"); return true; }
                    boolean enabled = args[2].equalsIgnoreCase("on");
                    plugin.getConfig().set("grace-period-enabled", enabled);
                    plugin.saveConfig();
                    sender.sendMessage("Grace period " + (enabled ? "enabled" : "disabled"));
                    return true;
                }
                sender.sendMessage("/ks admin giveks <player> [amount] | setbounty <player> <amount> | giveessence <player> <amount> | grace <on|off>");
                return true;
            case "essence":
                if (!(sender instanceof Player)) { sender.sendMessage("Only players can use essence"); return true; }
                Player p = (Player) sender;
                BloodEssenceManager essenceManager = plugin.getBloodEssenceManager();
                if (args.length == 1) {
                    // Show essence and upgrades
                    int essence = essenceManager.getEssence(p.getUniqueId());
                    p.sendMessage("§cBlood Essence: " + essence);
                    p.sendMessage("§7Available Upgrades:");
                    if (!essenceManager.hasUpgrade(p.getUniqueId(), BloodEssenceManager.UPGRADE_HEALTH)) {
                        p.sendMessage("§a/health §7(100 essence) - +1 Heart permanently");
                    }
                    if (!essenceManager.hasUpgrade(p.getUniqueId(), BloodEssenceManager.UPGRADE_SPEED)) {
                        p.sendMessage("§a/speed §7(150 essence) - Permanent Speed I");
                    }
                    if (!essenceManager.hasUpgrade(p.getUniqueId(), BloodEssenceManager.UPGRADE_REGEN)) {
                        p.sendMessage("§a/regen §7(200 essence) - Permanent Regeneration");
                    }
                    if (!essenceManager.hasUpgrade(p.getUniqueId(), BloodEssenceManager.UPGRADE_LUCK)) {
                        p.sendMessage("§a/luck §7(120 essence) - Permanent Luck");
                    }
                    return true;
                } else if (args.length == 2) {
                    String upgrade = args[1].toLowerCase();
                    int upgradeId = -1;
                    int cost = 0;
                    switch (upgrade) {
                        case "health": upgradeId = BloodEssenceManager.UPGRADE_HEALTH; cost = 100; break;
                        case "speed": upgradeId = BloodEssenceManager.UPGRADE_SPEED; cost = 150; break;
                        case "regen": upgradeId = BloodEssenceManager.UPGRADE_REGEN; cost = 200; break;
                        case "luck": upgradeId = BloodEssenceManager.UPGRADE_LUCK; cost = 120; break;
                        default: p.sendMessage("§cInvalid upgrade. Use /ks essence to see options."); return true;
                    }
                    if (essenceManager.hasUpgrade(p.getUniqueId(), upgradeId)) {
                        p.sendMessage("§cYou already have this upgrade!");
                        return true;
                    }
                    if (essenceManager.buyUpgrade(p.getUniqueId(), upgradeId, cost)) {
                        p.sendMessage("§aUpgrade purchased successfully!");
                    } else {
                        p.sendMessage("§cNot enough Blood Essence!");
                    }
                    return true;
                }
                p.sendMessage("§cUsage: /ks essence [upgrade]");
                return true;
            default:
                sender.sendMessage("Unknown subcommand");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("bounties", "config", "recipes", "admin", "forceevent", "giveitem", "craftitem", "essence");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("craftitem")) {
            return Arrays.asList("bountyblade", "mace", "phoenixfeather", "meteorstaff", "voidcleaver", "celestialcloak", "dashcrystal");
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("giveitem")) {
            return Arrays.asList("bountyblade", "mace", "phoenixfeather", "voidwalkerboots", "meteorstaff", "voidcleaver", "celestialcloak", "dashcrystal");
        }
        if (args.length >= 2 && args[0].equalsIgnoreCase("admin")) {
            if (args.length == 2) return Arrays.asList("giveks", "setbounty", "giveessence", "grace");
        }
        return Collections.emptyList();
    }

    private boolean hasItems(Player p, org.bukkit.Material mat, int amount) {
        int count = 0;
        for (org.bukkit.inventory.ItemStack it : p.getInventory().getContents()) {
            if (it != null && it.getType() == mat) {
                count += it.getAmount();
                if (count >= amount) return true;
            }
        }
        return false;
    }

    private void removeItems(Player p, org.bukkit.Material mat, int amount) {
        for (int i = 0; i < p.getInventory().getSize() && amount > 0; i++) {
            org.bukkit.inventory.ItemStack it = p.getInventory().getItem(i);
            if (it != null && it.getType() == mat) {
                int remove = Math.min(amount, it.getAmount());
                it.setAmount(it.getAmount() - remove);
                amount -= remove;
                if (it.getAmount() <= 0) p.getInventory().setItem(i, null);
                else p.getInventory().setItem(i, it);
            }
        }
    }
}
