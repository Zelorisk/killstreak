package com.example.killstreak.gui;

import com.example.killstreak.KSPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import java.util.Map;
import java.util.HashMap;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RecipesGUI implements Listener {
    private final KSPlugin plugin;

    public RecipesGUI(KSPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.previewOpen = new HashMap<>();
    }

    private final Map<java.util.UUID, Inventory> previewOpen;

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Special Recipes");
        // Border/background
        ItemStack border = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta bm = border.getItemMeta();
        bm.setDisplayName(" ");
        border.setItemMeta(bm);
        for (int i = 0; i < 54; i++) inv.setItem(i, border);

        // Bounty Blade
        ItemStack bountyBlade = com.example.killstreak.CustomItems.createBountyBlade(plugin);
        ItemMeta bbm = bountyBlade.getItemMeta();
        if (bbm != null) bbm.setLore(java.util.List.of(ChatColor.GOLD + "Sharpness VI, Fire II", ChatColor.GRAY + "Recipe: NETHERITE_INGOT, STICK, BLAZE_POWDER"));
        bountyBlade.setItemMeta(bbm);
        inv.setItem(10, bountyBlade);

        // Mace
        ItemStack mace = com.example.killstreak.CustomItems.createMace(plugin);
        ItemMeta mm = mace.getItemMeta();
        if (mm != null) mm.setLore(java.util.List.of(ChatColor.DARK_GRAY + "Custom melee weapon with lifesteal", ChatColor.GRAY + "Recipe: IRON_INGOT, STICK, NETHER_STAR"));
        mace.setItemMeta(mm);
        inv.setItem(12, mace);

        // Phoenix Feather
        ItemStack feather = com.example.killstreak.CustomItems.createPhoenixFeather(plugin);
        ItemMeta fm = feather.getItemMeta();
        if (fm != null) fm.setLore(java.util.List.of(ChatColor.YELLOW + "Revives you on death, single use", ChatColor.GRAY + "Recipe: FEATHER, GOLD_INGOT, NETHER_STAR"));
        feather.setItemMeta(fm);
        inv.setItem(14, feather);

        // Voidwalker Boots removed

        // Meteor Staff
        ItemStack staff = com.example.killstreak.CustomItems.createMeteorStaff(plugin);
        ItemMeta sm = staff.getItemMeta();
        if (sm != null) sm.setLore(java.util.List.of(ChatColor.GOLD + "Right-click to summon meteors (3 uses)", ChatColor.GRAY + "Recipe: FIRE_CHARGE, BLAZE_POWDER, BLAZE_ROD, NETHER_STAR"));
        staff.setItemMeta(sm);
        inv.setItem(28, staff);
        // New custom items
        ItemStack cleaver = com.example.killstreak.CustomItems.createVoidCleaver(plugin);
        ItemMeta cm = cleaver.getItemMeta();
        if (cm != null) cm.setLore(java.util.List.of(ChatColor.DARK_RED + "High armor-penetration cleaver", ChatColor.GRAY + "Recipe: NETHERITE_INGOT, STICK"));
        cleaver.setItemMeta(cm);
        inv.setItem(30, cleaver);
        ItemStack cloak = com.example.killstreak.CustomItems.createCelestialCloak(plugin);
        ItemMeta clm = cloak.getItemMeta();
        if (clm != null) clm.setLore(java.util.List.of(ChatColor.AQUA + "Grants Elytra flight with 2x speed", ChatColor.GRAY + "Recipe: DIAMOND, NETHER_STAR, GOLD_BLOCK"));
        cloak.setItemMeta(clm);
        inv.setItem(32, cloak);
        ItemStack dashCrystal = com.example.killstreak.CustomItems.createDashCrystal(plugin);
        ItemMeta dcm = dashCrystal.getItemMeta();
        if (dcm != null) dcm.setLore(java.util.List.of(ChatColor.YELLOW + "Grants dash and teleport abilities. Cannot be lost on death.", ChatColor.GRAY + "Recipe: AMETHYST_SHARD, NETHER_STAR"));
        dashCrystal.setItemMeta(dcm);
        inv.setItem(34, dashCrystal);

        // Info item
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infom = info.getItemMeta();
        infom.setDisplayName(ChatColor.AQUA + "Recipes Info");
        infom.setLore(java.util.List.of(
            ChatColor.WHITE + "Special items can be crafted.",
            ChatColor.YELLOW + "Hover for recipe details!"
        ));
        info.setItemMeta(infom);
        inv.setItem(4, info);

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "Close");
        close.setItemMeta(closeMeta);
        inv.setItem(49, close);

        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (!e.getView().getTitle().contains("Special Recipes")) { 
            // filter for workbench preview close
            if (e.getView().getTitle().contains("Recipe Preview")) {
                e.setCancelled(true);
            }
            return;
        }
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;
        String name = e.getCurrentItem().getItemMeta().getDisplayName();
        if (name != null && name.contains("Close")) {
            p.closeInventory();
            p.playSound(p.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
            return;
        }
        if (name != null) {
            // craft preview
            org.bukkit.inventory.Inventory preview = Bukkit.createInventory(null, InventoryType.WORKBENCH, "Recipe Preview: " + name);
            // Fill preview grid depending on the item clicked
            if (name.contains("Bounty Blade")) {
                preview.setItem(1, new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_INGOT));
                preview.setItem(3, new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_INGOT));
                preview.setItem(4, new org.bukkit.inventory.ItemStack(org.bukkit.Material.STICK));
                preview.setItem(5, new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_INGOT));
                preview.setItem(7, new org.bukkit.inventory.ItemStack(org.bukkit.Material.BLAZE_POWDER));
                preview.setItem(0, com.example.killstreak.CustomItems.createBountyBlade(plugin));
            } else if (name.contains("Mace") || name.contains("Mace (custom)")) {
                preview.setItem(1, new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_INGOT));
                preview.setItem(2, new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHER_STAR));
                preview.setItem(3, new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_INGOT));
                preview.setItem(4, new org.bukkit.inventory.ItemStack(org.bukkit.Material.STICK));
                preview.setItem(5, new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_INGOT));
                preview.setItem(7, new org.bukkit.inventory.ItemStack(org.bukkit.Material.STICK));
                preview.setItem(0, com.example.killstreak.CustomItems.createMace(plugin));
            } else if (name.contains("Phoenix Feather")) {
                preview.setItem(1, new org.bukkit.inventory.ItemStack(org.bukkit.Material.FEATHER));
                preview.setItem(3, new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLD_INGOT));
                preview.setItem(4, new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHER_STAR));
                preview.setItem(5, new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLD_INGOT));
                preview.setItem(7, new org.bukkit.inventory.ItemStack(org.bukkit.Material.FEATHER));
                preview.setItem(0, com.example.killstreak.CustomItems.createPhoenixFeather(plugin));
            // Voidwalker Boots removed
            } else if (name.contains("Meteor Staff")) {
                preview.setItem(1, new org.bukkit.inventory.ItemStack(org.bukkit.Material.FIRE_CHARGE));
                preview.setItem(3, new org.bukkit.inventory.ItemStack(org.bukkit.Material.BLAZE_POWDER));
                preview.setItem(4, new org.bukkit.inventory.ItemStack(org.bukkit.Material.BLAZE_ROD));
                preview.setItem(5, new org.bukkit.inventory.ItemStack(org.bukkit.Material.BLAZE_POWDER));
                preview.setItem(7, new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHER_STAR));
                preview.setItem(0, com.example.killstreak.CustomItems.createMeteorStaff(plugin));
            } else if (name.contains("Void Cleaver") || name.contains("Void Cleaver")) {
                preview.setItem(0, new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_INGOT));
                preview.setItem(1, new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_INGOT));
                preview.setItem(3, new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_INGOT));
                preview.setItem(4, new org.bukkit.inventory.ItemStack(org.bukkit.Material.STICK));
                preview.setItem(5, new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_INGOT));
                preview.setItem(7, new org.bukkit.inventory.ItemStack(org.bukkit.Material.STICK));
                preview.setItem(10, com.example.killstreak.CustomItems.createVoidCleaver(plugin));
            } else if (name.contains("Celestial Cloak") || name.contains("Celestial Cloak")) {
                preview.setItem(0, new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND));
                preview.setItem(1, new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND));
                preview.setItem(2, new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND));
                preview.setItem(3, new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND));
                preview.setItem(4, new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHER_STAR));
                preview.setItem(5, new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND));
                preview.setItem(6, new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLD_BLOCK));
                preview.setItem(7, new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLD_BLOCK));
                preview.setItem(8, new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLD_BLOCK));
                preview.setItem(10, com.example.killstreak.CustomItems.createCelestialCloak(plugin));
            } else if (name.contains("Dash Crystal")) {
                preview.setItem(1, new org.bukkit.inventory.ItemStack(org.bukkit.Material.AMETHYST_SHARD));
                preview.setItem(3, new org.bukkit.inventory.ItemStack(org.bukkit.Material.AMETHYST_SHARD));
                preview.setItem(4, new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHER_STAR));
                preview.setItem(5, new org.bukkit.inventory.ItemStack(org.bukkit.Material.AMETHYST_SHARD));
                preview.setItem(7, new org.bukkit.inventory.ItemStack(org.bukkit.Material.AMETHYST_SHARD));
                preview.setItem(0, com.example.killstreak.CustomItems.createDashCrystal(plugin));
            }
            p.openInventory(preview);
            previewOpen.put(p.getUniqueId(), preview);
        }
    }
}
