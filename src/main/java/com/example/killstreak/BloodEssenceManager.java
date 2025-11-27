package com.example.killstreak;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BloodEssenceManager {
    private final KSPlugin plugin;
    private final Map<UUID, Integer> essence = new HashMap<>();
    private final Map<UUID, Integer> upgrades = new HashMap<>(); // bitmask for upgrades

    // Upgrade constants
    public static final int UPGRADE_HEALTH = 1;
    public static final int UPGRADE_DAMAGE = 2;
    public static final int UPGRADE_SPEED = 4;
    public static final int UPGRADE_REGEN = 8;
    public static final int UPGRADE_LUCK = 16;

    public BloodEssenceManager(KSPlugin plugin) {
        this.plugin = plugin;
    }

    public void addEssence(UUID player, int amount) {
        essence.put(player, essence.getOrDefault(player, 0) + amount);
        Player p = Bukkit.getPlayer(player);
        if (p != null) {
            p.sendMessage("§c+" + amount + " Blood Essence! §7(/ks essence to spend)");
        }
    }

    public int getEssence(UUID player) {
        return essence.getOrDefault(player, 0);
    }

    public boolean spendEssence(UUID player, int amount) {
        int current = getEssence(player);
        if (current >= amount) {
            essence.put(player, current - amount);
            return true;
        }
        return false;
    }

    public void giveStartingEssence(UUID player) {
        addEssence(player, 50); // Starting essence
    }

    public boolean hasUpgrade(UUID player, int upgrade) {
        return (upgrades.getOrDefault(player, 0) & upgrade) != 0;
    }

    public boolean buyUpgrade(UUID player, int upgrade, int cost) {
        if (hasUpgrade(player, upgrade)) return false;
        if (!spendEssence(player, cost)) return false;

        int current = upgrades.getOrDefault(player, 0);
        upgrades.put(player, current | upgrade);

        // Apply upgrade permanently
        Player p = Bukkit.getPlayer(player);
        if (p != null) {
            applyUpgrade(p, upgrade);
        }

        return true;
    }

    private void applyUpgrade(Player p, int upgrade) {
        switch (upgrade) {
            case UPGRADE_HEALTH:
                double maxHealth = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
                p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth + 2.0); // +1 heart
                p.sendMessage("§aBlood Essence Upgrade: §c+1 Heart!");
                break;
            case UPGRADE_DAMAGE:
                // This would require persistent potion effects or custom damage handling
                p.sendMessage("§aBlood Essence Upgrade: §c+10% Damage!");
                break;
            case UPGRADE_SPEED:
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
                p.sendMessage("§aBlood Essence Upgrade: §cPermanent Speed I!");
                break;
            case UPGRADE_REGEN:
                p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, false, false));
                p.sendMessage("§aBlood Essence Upgrade: §cPermanent Regeneration!");
                break;
            case UPGRADE_LUCK:
                p.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, 0, false, false));
                p.sendMessage("§aBlood Essence Upgrade: §cPermanent Luck!");
                break;
        }
    }

    public void applyAllUpgrades(Player p) {
        int playerUpgrades = upgrades.getOrDefault(p.getUniqueId(), 0);
        if ((playerUpgrades & UPGRADE_HEALTH) != 0) {
            double maxHealth = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
            if (maxHealth < 24.0) { // Don't exceed reasonable limits
                p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth + 2.0);
            }
        }
        if ((playerUpgrades & UPGRADE_SPEED) != 0) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
        }
        if ((playerUpgrades & UPGRADE_REGEN) != 0) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, false, false));
        }
        if ((playerUpgrades & UPGRADE_LUCK) != 0) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, 0, false, false));
        }
    }

    public Map<UUID, Integer> getAllEssence() {
        return new HashMap<>(essence);
    }

    public Map<UUID, Integer> getAllUpgrades() {
        return new HashMap<>(upgrades);
    }

    public void loadData(Map<UUID, Integer> essenceData, Map<UUID, Integer> upgradesData) {
        essence.putAll(essenceData);
        upgrades.putAll(upgradesData);
    }
}
