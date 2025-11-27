package com.example.killstreak;

import org.bukkit.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class CustomItems {
    public static NamespacedKey key(KSPlugin plugin, String id) { return new NamespacedKey(plugin, id); }

    public static ItemStack createBountyBlade(KSPlugin plugin) {
        ItemStack it = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName("\u00A7bBounty Blade");
        m.setLore(java.util.List.of("Sharpness VI, Fire II"));
        // add default enchants
        var sharp = org.bukkit.enchantments.Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft("sharpness"));
        var fire = org.bukkit.enchantments.Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft("fire_aspect"));
        if (sharp != null) m.addEnchant(sharp, 6, true);
        if (fire != null) m.addEnchant(fire, 2, true);
        // mark as custom
        m.getPersistentDataContainer().set(key(plugin, "bounty_blade"), PersistentDataType.BYTE, (byte)1);
        it.setItemMeta(m);
        return it;
    }

    public static ItemStack createMace(KSPlugin plugin) {
        ItemStack it = new ItemStack(Material.IRON_AXE);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName("\u00A77Mace");
        m.setLore(java.util.List.of("A heavy crushing weapon with lifesteal"));
        m.getPersistentDataContainer().set(key(plugin, "mace"), PersistentDataType.BYTE, (byte)1);
        it.setItemMeta(m);
        return it;
    }

    public static ItemStack createPhoenixFeather(KSPlugin plugin) {
        ItemStack it = new ItemStack(Material.PHANTOM_MEMBRANE);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName("\u00A76Phoenix Feather");
        m.setLore(java.util.List.of("Revives you on death, single use"));
        m.getPersistentDataContainer().set(key(plugin, "phoenix_feather"), PersistentDataType.INTEGER, 1);
        it.setItemMeta(m);
        return it;
    }

    public static ItemStack createMeteorStaff(KSPlugin plugin) {
        ItemStack it = new ItemStack(Material.BLAZE_ROD);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName("\u00A7cMeteor Staff");
        m.setLore(java.util.List.of("Right-click to summon meteors (3 uses)", "Deals 2.5 hearts in 5-block radius (bypasses armor)."));
        m.getPersistentDataContainer().set(key(plugin, "meteor_staff"), PersistentDataType.INTEGER, 3);
        it.setItemMeta(m);
        return it;
    }

    public static ItemStack createVoidCleaver(KSPlugin plugin) {
        ItemStack it = new ItemStack(Material.NETHERITE_AXE);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName("\u00A74Void Cleaver");
        m.setLore(java.util.List.of("High armor-penetration cleaver", "Hits ignore 50% of armor."));
        var sharp = org.bukkit.enchantments.Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft("sharpness"));
        var kb = org.bukkit.enchantments.Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft("knockback"));
        if (sharp != null) m.addEnchant(sharp, 8, true);
        if (kb != null) m.addEnchant(kb, 3, true);
        m.getPersistentDataContainer().set(key(plugin, "void_cleaver"), PersistentDataType.BYTE, (byte)1);
        it.setItemMeta(m);
        return it;
    }

    public static ItemStack createCelestialCloak(KSPlugin plugin) {
        ItemStack it = new ItemStack(Material.ELYTRA);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName("\u00A7bCelestial Cloak");
        m.setLore(java.util.List.of("Grants Elytra flight with 2x speed", "Press Q for an overpowered boost."));
        m.getPersistentDataContainer().set(key(plugin, "celestial_cloak"), PersistentDataType.BYTE, (byte)1);
        it.setItemMeta(m);
        return it;
    }

    public static ItemStack createDashCrystal(KSPlugin plugin) {
        ItemStack it = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName("\u00A7dDash Crystal");
        m.setLore(java.util.List.of("Grants dash and teleport abilities. Cannot be lost on death.", "Press Q (drop) to activate dash while in inventory."));
        m.getPersistentDataContainer().set(key(plugin, "dash_crystal"), PersistentDataType.BYTE, (byte)1);
        it.setItemMeta(m);
        return it;
    }

    public static boolean isCustom(ItemStack it, KSPlugin plugin, String id) {
        if (it == null || it.getItemMeta() == null) return false;
        return it.getItemMeta().getPersistentDataContainer().has(key(plugin, id), PersistentDataType.BYTE) ||
               it.getItemMeta().getPersistentDataContainer().has(key(plugin, id), PersistentDataType.INTEGER);
    }
}
