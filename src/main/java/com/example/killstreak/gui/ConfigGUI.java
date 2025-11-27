package com.example.killstreak.gui;

import com.example.killstreak.ConfigManager;
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

public class ConfigGUI implements Listener {
    private final KSPlugin plugin;
    private final ConfigManager cfg;

    public ConfigGUI(KSPlugin plugin, ConfigManager cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.BLUE + "KS Config");
        // Border/background
        ItemStack border = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        ItemMeta bm = border.getItemMeta();
        bm.setDisplayName(" ");
        border.setItemMeta(bm);
        for (int i = 0; i < 27; i++) inv.setItem(i, border);

        // Drop ability toggle
        ItemStack drop = new ItemStack(Material.DROPPER);
        ItemMeta dm = drop.getItemMeta();
        dm.setDisplayName(ChatColor.YELLOW + "Drop triggers ability: " + (cfg.isDropTriggersAbility() ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
        dm.setLore(java.util.List.of(ChatColor.GRAY + "Toggle if dropping an item triggers your ability."));
        drop.setItemMeta(dm);
        inv.setItem(13, drop);

        // Info item
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infom = info.getItemMeta();
        infom.setDisplayName(ChatColor.AQUA + "Config Info");
        infom.setLore(java.util.List.of(
            ChatColor.WHITE + "Configure plugin options here.",
            ChatColor.GRAY + "More options coming soon!"
        ));
        info.setItemMeta(infom);
        inv.setItem(4, info);

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
        if (!e.getView().getTitle().contains("KS Config")) return;
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;
        String name = e.getCurrentItem().getItemMeta().getDisplayName();
        if (name != null && name.contains("Close")) {
            p.closeInventory();
            p.playSound(p.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
            return;
        }
        if (e.getCurrentItem().getType() == Material.DROPPER) {
            cfg.setDropTriggersAbility(!cfg.isDropTriggersAbility());
            p.sendMessage(ChatColor.GREEN + "Toggled drop trigger: " + (cfg.isDropTriggersAbility() ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
            p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_LEVER_CLICK, 1f, 1f);
            p.closeInventory();
        }
    }
}
