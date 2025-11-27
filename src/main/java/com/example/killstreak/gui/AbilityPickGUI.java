package com.example.killstreak.gui;

import com.example.killstreak.AbilitiesManager;
import com.example.killstreak.KSPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class AbilityPickGUI implements Listener {
    private final KSPlugin plugin;
    private final AbilitiesManager abilitiesManager;

    public AbilityPickGUI(KSPlugin plugin, AbilitiesManager abilitiesManager) {
        this.plugin = plugin;
        this.abilitiesManager = abilitiesManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player p, int level) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Pick Ability (Level " + level + ")");
        // Border/background
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bm = border.getItemMeta();
        bm.setDisplayName(" ");
        border.setItemMeta(bm);
        for (int i = 0; i < 27; i++) inv.setItem(i, border);

        // Ability items with color, lore, and slot spacing
        ItemStack strength = new ItemStack(Material.IRON_SWORD);
        ItemMeta sm = strength.getItemMeta();
        sm.setDisplayName(ChatColor.RED + "Strength Boost");
        sm.setLore(Arrays.asList(ChatColor.GRAY + "Permanent Strength I", ChatColor.DARK_GRAY + "Click to select!"));
        strength.setItemMeta(sm);

        ItemStack speed = new ItemStack(Material.SUGAR);
        ItemMeta sp = speed.getItemMeta();
        sp.setDisplayName(ChatColor.YELLOW + "Speed Boost");
        sp.setLore(Arrays.asList(ChatColor.GRAY + "Permanent Speed I", ChatColor.DARK_GRAY + "Click to select!"));
        speed.setItemMeta(sp);

        ItemStack regen = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta rm = regen.getItemMeta();
        rm.setDisplayName(ChatColor.GREEN + "Regen Aura");
        rm.setLore(Arrays.asList(ChatColor.GRAY + "Heals you every 10s", ChatColor.DARK_GRAY + "Click to select!"));
        regen.setItemMeta(rm);

        ItemStack lifesteal = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta lm = lifesteal.getItemMeta();
        lm.setDisplayName(ChatColor.LIGHT_PURPLE + "Lifesteal");
        lm.setLore(Arrays.asList(ChatColor.GRAY + "Heal 1 heart on hit", ChatColor.DARK_GRAY + "Click to select!"));
        lifesteal.setItemMeta(lm);

        ItemStack invis = new ItemStack(Material.GLASS);
        ItemMeta im = invis.getItemMeta();
        im.setDisplayName(ChatColor.AQUA + "Invisibility");
        im.setLore(Arrays.asList(ChatColor.GRAY + "Permanent Invisibility", ChatColor.DARK_GRAY + "Click to select!"));
        invis.setItemMeta(im);

        ItemStack teleport = new ItemStack(Material.ENDER_PEARL);
        ItemMeta fm = teleport.getItemMeta();
        fm.setDisplayName(ChatColor.WHITE + "Teleport");
        fm.setLore(Arrays.asList(ChatColor.GRAY + "Teleport up to 75 blocks using Dash Crystal (10 min cooldown)", ChatColor.DARK_GRAY + "Click to select!"));
        teleport.setItemMeta(fm);

        // Place abilities in center row
        inv.setItem(10, strength);
        inv.setItem(11, speed);
        inv.setItem(12, regen);
        inv.setItem(13, lifesteal);
        inv.setItem(14, invis);
        inv.setItem(15, teleport);

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta cm = close.getItemMeta();
        cm.setDisplayName(ChatColor.RED + "Close");
        close.setItemMeta(cm);
        inv.setItem(22, close);

        // Info item
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infom = info.getItemMeta();
        infom.setDisplayName(ChatColor.AQUA + "How to Pick Abilities");
        infom.setLore(Arrays.asList(
            ChatColor.WHITE + "Pick one ability for this level.",
            ChatColor.GRAY + "Each ability is permanent.",
            ChatColor.YELLOW + "Hover for details, click to select!"
        ));
        info.setItemMeta(infom);
        inv.setItem(4, info);

        p.openInventory(inv);
        // store desired level in metadata so click handler can apply
        p.setMetadata("ks_pick_level", new org.bukkit.metadata.FixedMetadataValue(plugin, level));
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getView().getTitle() == null) return;
        if (!e.getView().getTitle().contains("Pick Ability")) return;
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;
        int level = 1;
        if (p.hasMetadata("ks_pick_level")) {
            level = p.getMetadata("ks_pick_level").get(0).asInt();
            p.removeMetadata("ks_pick_level", plugin);
        }
        String name = e.getCurrentItem().getItemMeta().getDisplayName();
        if (name == null) return;
        if (name.contains("Close")) {
            p.closeInventory();
            p.playSound(p.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
            return;
        }
        String chosen = null;
        String msg = null;
        org.bukkit.Sound sound = org.bukkit.Sound.ENTITY_PLAYER_LEVELUP;
        if (name.contains("Strength")) {
            chosen = "STRENGTH";
            msg = ChatColor.RED + "You picked Strength Boost!";
            sound = org.bukkit.Sound.ENTITY_IRON_GOLEM_REPAIR;
        } else if (name.contains("Speed")) {
            chosen = "SPEED";
            msg = ChatColor.YELLOW + "You picked Speed Boost!";
            sound = org.bukkit.Sound.ENTITY_BAT_TAKEOFF;
        } else if (name.contains("Regen")) {
            chosen = "REGEN_AURA";
            msg = ChatColor.GREEN + "You picked Regen Aura!";
            sound = org.bukkit.Sound.BLOCK_BREWING_STAND_BREW;
        } else if (name.contains("Lifesteal")) {
            chosen = "LIFESTEAL";
            msg = ChatColor.LIGHT_PURPLE + "You picked Lifesteal!";
            sound = org.bukkit.Sound.ENTITY_ZOMBIE_VILLAGER_CURE;
        } else if (name.contains("Invis")) {
            chosen = "INVIS";
            msg = ChatColor.AQUA + "You picked Invisibility!";
            sound = org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT;
        } else if (name.contains("Teleport")) {
            chosen = "TELEPORT";
            msg = ChatColor.WHITE + "You picked Teleport!";
            sound = org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT;
        }
        if (chosen != null) {
            abilitiesManager.setChoice(p.getUniqueId(), level, chosen);
            abilitiesManager.applyImmediate(p, level);
            p.sendMessage(msg);
            p.playSound(p.getLocation(), sound, 1f, 1f);
            p.closeInventory();
        }
    }
}
