package com.example.killstreak;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KillstreakManager {
    private final KSPlugin plugin;
    private final BountyManager bountyManager;
    private final Map<UUID, Integer> streaks = new HashMap<>();
    private final Map<UUID, Boolean> hasDash = new HashMap<>();
    private final com.example.killstreak.AbilitiesManager abilitiesManager;

    public KillstreakManager(KSPlugin plugin, BountyManager bountyManager) {
        this.plugin = plugin;
        this.bountyManager = bountyManager;
        this.abilitiesManager = plugin.getAbilitiesManager();
    }

    public int get(UUID id) { return streaks.getOrDefault(id, 0); }

    public void reset(UUID id) {
        streaks.remove(id);
        hasDash.remove(id);
        Player p = Bukkit.getPlayer(id);
        if (p != null) {
            PotionEffectType strength = PotionEffectType.getByName("INCREASE_DAMAGE");
            PotionEffectType fireRes = PotionEffectType.getByName("FIRE_RESISTANCE");
            PotionEffectType speed = PotionEffectType.getByName("SPEED");
            if (strength != null) p.removePotionEffect(strength);
            if (fireRes != null) p.removePotionEffect(fireRes);
            if (speed != null) p.removePotionEffect(speed);
            // reset health attribute if needed
            p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        }
    }

    public void addKill(Player killer, Player victim) {
        if (killer == null) return;
        UUID id = killer.getUniqueId();
        int n = get(id) + 1;
        streaks.put(id, n);
        applyEffects(killer, n);
        // if ability manager exists and this level should allow pick, open GUI
        if (abilitiesManager != null) {
            int level = n;
            // odd numbered killstreaks except 1: open tree upgrade choices (3,5,7,9...)
            if (level % 2 == 1 && level != 1) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    var gui = plugin.getAbilityPickGUI();
                    // ensure we have tree GUI registered; reuse abilityPickGUI slot is okay but we created a separate TreeUpgradeGUI in plugin
                    new com.example.killstreak.gui.TreeUpgradeGUI(plugin, abilitiesManager).open(killer, level);
                });
            } else {
                // allow ability picks for specific levels: 1,2,4,5,7,8,9 but we've handled odds separately
                if ((level == 1) || (level == 2) || (level == 4) || (level == 5) || (level == 8) || (level == 10)) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        plugin.getAbilityPickGUI().open(killer, level);
                    });
                }
                // apply any immediate chosen abilities for player across choices
                for (var entry : abilitiesManager.getAllChoices().getOrDefault(id, java.util.Map.of()).entrySet()) {
                    abilitiesManager.applyImmediate(killer, entry.getKey());
                }
            }
        }
        // place bounty on victim if player held valuable items
        bountyManager.checkInventoryForBounty(victim);
    }

    private void applyEffects(Player p, int streak) {
        // durations in ticks (long lasting)
        int dur = 9999999;
        if (streak >= 10) {
            // 5 extra hearts
            p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0 + 10.0);
            // give custom sword
            ItemStack sword = new ItemStack(org.bukkit.Material.NETHERITE_SWORD);
            ItemMeta m = sword.getItemMeta();
            m.setDisplayName("\u00A7cKillstreak Sword");
            Enchantment sharp = Enchantment.getByKey(NamespacedKey.minecraft("sharpness"));
            Enchantment unb = Enchantment.getByKey(NamespacedKey.minecraft("unbreaking"));
            Enchantment fire = Enchantment.getByKey(NamespacedKey.minecraft("fire_aspect"));
            Enchantment kb = Enchantment.getByKey(NamespacedKey.minecraft("knockback"));
            if (sharp != null) m.addEnchant(sharp, 6, true);
            if (unb != null) m.addEnchant(unb, 5, true);
            if (fire != null) m.addEnchant(fire, 2, true);
            if (kb != null) m.addEnchant(kb, 2, true);
            sword.setItemMeta(m);
            p.getInventory().addItem(sword);
            p.sendMessage("\u00A7aKillstreak " + streak + ": +5 hearts and Killstreak Sword!");
        } else if (streak >= 6) {
            hasDash.put(p.getUniqueId(), true);
            p.sendMessage("\u00A7eKillstreak " + streak + ": You unlocked Dash ability! Press Q to dash.");
        } else if (streak >= 4) {
            PotionEffectType strength = PotionEffectType.STRENGTH;
            PotionEffectType fireRes = PotionEffectType.FIRE_RESISTANCE;
            PotionEffectType speed = PotionEffectType.SPEED;
            if (strength != null) p.addPotionEffect(new PotionEffect(strength, dur, 1, false, false));
            if (fireRes != null) p.addPotionEffect(new PotionEffect(fireRes, dur, 0, false, false));
            if (speed != null) p.addPotionEffect(new PotionEffect(speed, dur, 1, false, false));
            p.sendMessage("\u00A7aKillstreak " + streak + ": Strength II, Fire Resistance, Speed II!");
        } else if (streak >= 2) {
            PotionEffectType strength = PotionEffectType.STRENGTH;
            PotionEffectType fireRes = PotionEffectType.FIRE_RESISTANCE;
            if (strength != null) p.addPotionEffect(new PotionEffect(strength, dur, 0, false, false));
            if (fireRes != null) p.addPotionEffect(new PotionEffect(fireRes, dur, 0, false, false));
            p.sendMessage("\u00A7aKillstreak " + streak + ": Strength I, Fire Resistance!");
        }
    }

    public boolean hasDash(UUID id) { return hasDash.getOrDefault(id, false); }

    public Map<UUID, Integer> getAllStreaks() { return streaks; }
}
