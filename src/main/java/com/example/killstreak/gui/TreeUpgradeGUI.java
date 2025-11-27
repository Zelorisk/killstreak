package com.example.killstreak.gui;

import com.example.killstreak.AbilitiesManager;
import com.example.killstreak.KSPlugin;
import org.bukkit.Bukkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class TreeUpgradeGUI implements Listener {
    private final KSPlugin plugin;
    private final AbilitiesManager abilitiesManager;

    public TreeUpgradeGUI(KSPlugin plugin, AbilitiesManager abilitiesManager) {
        this.plugin = plugin;
        this.abilitiesManager = abilitiesManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player p, int level) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("Upgrade Tree (Lvl " + level + ")", NamedTextColor.DARK_GREEN));
        // Border/background
        ItemStack border = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta bm = border.getItemMeta();
        bm.displayName(Component.text(" "));
        border.setItemMeta(bm);
        for (int i = 0; i < 27; i++) inv.setItem(i, border);

        // Movement Root
        ItemStack movement = new ItemStack(Material.FEATHER);
        ItemMeta mm = movement.getItemMeta();
        mm.displayName(Component.text("Movement Root", NamedTextColor.AQUA));
        mm.lore(List.of(
            Component.text("+Speed, Dash improvements, small jump boost", NamedTextColor.GRAY),
            Component.text("Click to select!", NamedTextColor.DARK_GRAY)
        ));
        movement.setItemMeta(mm);
        inv.setItem(11, movement);

        // Combat Root
        ItemStack combat = new ItemStack(Material.IRON_SWORD);
        ItemMeta cm = combat.getItemMeta();
        cm.displayName(Component.text("Combat Root", NamedTextColor.RED));
        cm.lore(List.of(
            Component.text("+Strength, crit chance, small damage buffs", NamedTextColor.GRAY),
            Component.text("Click to select!", NamedTextColor.DARK_GRAY)
        ));
        combat.setItemMeta(cm);
        inv.setItem(13, combat);

        // Durability Root
        ItemStack dur = new ItemStack(Material.SHIELD);
        ItemMeta dm = dur.getItemMeta();
        dm.displayName(Component.text("Durability Root", NamedTextColor.GOLD));
        dm.lore(List.of(
            Component.text("Better armor/durability, reduced knockback", NamedTextColor.GRAY),
            Component.text("Click to select!", NamedTextColor.DARK_GRAY)
        ));
        dur.setItemMeta(dm);
        inv.setItem(15, dur);

        // Info item
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infom = info.getItemMeta();
        infom.displayName(Component.text("Upgrade Tree Info", NamedTextColor.AQUA));
        infom.lore(List.of(
            Component.text("Pick a root to specialize your perks.", NamedTextColor.WHITE),
            Component.text("Each root gives unique bonuses!", NamedTextColor.GRAY)
        ));
        info.setItemMeta(infom);
        inv.setItem(4, info);

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta cm2 = close.getItemMeta();
        cm2.displayName(Component.text("Close", NamedTextColor.RED));
        close.setItemMeta(cm2);
        inv.setItem(22, close);

        p.openInventory(inv);
        p.setMetadata("ks_tree_level", new org.bukkit.metadata.FixedMetadataValue(plugin, level));
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        // Use Component title for inventory
        Component title = e.getView().title();
        if (title == null || !LegacyComponentSerializer.legacySection().serialize(title).contains("Upgrade Tree")) return;
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;
        int level = 1;
        if (p.hasMetadata("ks_tree_level")) {
            level = p.getMetadata("ks_tree_level").get(0).asInt();
            p.removeMetadata("ks_tree_level", plugin);
        }
        ItemMeta meta = e.getCurrentItem().getItemMeta();
        Component displayName = meta.displayName();
        String name = displayName != null ? LegacyComponentSerializer.legacySection().serialize(displayName) : "";
        if (name.isEmpty()) return;
        if (name.contains("Close")) {
            p.closeInventory();
            p.playSound(p.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
            return;
        }
        if (name.contains("Movement")) {
            abilitiesManager.setChoice(p.getUniqueId(), level, "SPEED");
            abilitiesManager.setChoice(p.getUniqueId(), level+100, "DASH_UPGRADE");
            p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_PARROT_FLY, 1f, 1f);
        } else if (name.contains("Combat")) {
            abilitiesManager.setChoice(p.getUniqueId(), level, "STRENGTH");
            abilitiesManager.setChoice(p.getUniqueId(), level+100, "CRIT_BOOST");
            p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_IRON_GOLEM_REPAIR, 1f, 1f);
        } else if (name.contains("Durability")) {
            abilitiesManager.setChoice(p.getUniqueId(), level, "ARMOR_BUFF");
            abilitiesManager.setChoice(p.getUniqueId(), level+100, "KNOCKBACK_RESIST");
            p.playSound(p.getLocation(), org.bukkit.Sound.ITEM_SHIELD_BLOCK, 1f, 1f);
        }
        // apply immediate effects for the main root selection
        abilitiesManager.applyImmediate(p, level);
        p.sendMessage(LegacyComponentSerializer.legacySection().deserialize("§aSelected " + name + " upgrades for level " + level));
        p.closeInventory();
    }
}
