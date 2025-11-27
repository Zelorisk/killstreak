package com.example.killstreak.gui;

import com.example.killstreak.BountyManager;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BountiesGUI implements Listener {
    private final KSPlugin plugin;
    private final BountyManager bountyManager;

    public BountiesGUI(KSPlugin plugin, BountyManager bountyManager) {
        this.plugin = plugin;
        this.bountyManager = bountyManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Top Bounties");
        // Border/background
        ItemStack border = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta bm = border.getItemMeta();
        bm.setDisplayName(" ");
        border.setItemMeta(bm);
        for (int i = 0; i < 27; i++) inv.setItem(i, border);

        List<Map.Entry<UUID,Integer>> top = bountyManager.topBounties(3);
        int[] slots = {11, 13, 15};
        int i = 0;
        for (Map.Entry<UUID,Integer> e : top) {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta m = skull.getItemMeta();
            String name = Bukkit.getOfflinePlayer(e.getKey()).getName();
            m.setDisplayName(ChatColor.YELLOW + (name == null ? e.getKey().toString() : name));
            m.setLore(List.of(ChatColor.GOLD + "Bounty: " + ChatColor.RED + e.getValue(), ChatColor.GRAY + "Take them down for a reward!"));
            skull.setItemMeta(m);
            inv.setItem(slots[i++], skull);
        }
        // Info item
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infom = info.getItemMeta();
        infom.setDisplayName(ChatColor.AQUA + "Bounties Info");
        infom.setLore(List.of(
                ChatColor.WHITE + "Players with high-value items get bounties.",
                ChatColor.YELLOW + "Top 3 bounties are shown here.",
                ChatColor.GRAY + "Kill them to claim the reward!"
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
        if (!e.getView().getTitle().contains("Top Bounties")) return;
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;
        String name = e.getCurrentItem().getItemMeta().getDisplayName();
        if (name != null && name.contains("Close")) {
            p.closeInventory();
            p.playSound(p.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        }
    }
}
