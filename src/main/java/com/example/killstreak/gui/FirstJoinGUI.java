package com.example.killstreak.gui;

import com.example.killstreak.KSPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class FirstJoinGUI implements Listener {
    private final KSPlugin plugin;

    public FirstJoinGUI(KSPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Welcome to KillStreaks");
        // Border/background
        ItemStack border = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta bm = border.getItemMeta();
        bm.setDisplayName(" ");
        border.setItemMeta(bm);
        for (int i = 0; i < 27; i++) inv.setItem(i, border);

        // Guide book
        ItemStack info = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta m = info.getItemMeta();
        m.setDisplayName(ChatColor.AQUA + "KillStreaks Guide");
        m.setLore(List.of(
            ChatColor.WHITE + "Welcome! This plugin rewards killstreaks.",
            ChatColor.YELLOW + "Stack kills to unlock abilities!",
            ChatColor.GOLD + "Bounties are placed on players with rare items.",
            ChatColor.LIGHT_PURPLE + "Use /ks recipes for special crafts.",
            ChatColor.AQUA + "Use /ks config to tweak settings.",
            ChatColor.GRAY + "Enjoy and have fun!"
        ));
        info.setItemMeta(m);
        inv.setItem(13, info);


        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta cm = close.getItemMeta();
        cm.setDisplayName(ChatColor.RED + "Close");
        close.setItemMeta(cm);
        inv.setItem(22, close);

        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (!e.getView().getTitle().contains("Welcome to KillStreaks")) return;
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;
        String name = e.getCurrentItem().getItemMeta().getDisplayName();
        if (name == null) return;
        if (name.contains("Close")) {
            p.closeInventory();
            p.playSound(p.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
            return;
        }
        // nothing else to do
    }
}
