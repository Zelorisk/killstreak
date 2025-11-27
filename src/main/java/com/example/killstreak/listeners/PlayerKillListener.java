package com.example.killstreak.listeners;

import com.example.killstreak.BountyManager;
import com.example.killstreak.KSPlugin;
import com.example.killstreak.KillstreakManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;

public class PlayerKillListener implements Listener {
    private final KSPlugin plugin;
    private final KillstreakManager ksManager;
    private final BountyManager bountyManager;

    public PlayerKillListener(KSPlugin plugin, KillstreakManager ksManager, BountyManager bountyManager) {
        this.plugin = plugin;
        this.ksManager = ksManager;
        this.bountyManager = bountyManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) {
            ksManager.reset(victim.getUniqueId());
            return;
        }
        // if victim had grace, do not count
        // simple grace check: if they have metadata 'ks_grace' skip
        if (victim.hasMetadata("ks_grace")) {
            killer.sendMessage("\u00A7eVictim is in grace period; no killstreak awarded.");
            ksManager.reset(victim.getUniqueId());
            return;
        }
        ksManager.addKill(killer, victim);
        // reward killer if victim had a bounty
        int bounty = bountyManager.getBounty(victim.getUniqueId());
        if (bounty > 0) {
            // Example rewarding: netherite ingots, gold blocks, enchanted golden apple, and a custom sword
            ItemStack netherite = new ItemStack(Material.NETHERITE_INGOT, Math.min(8, Math.max(1, bounty/100)));
            ItemStack goldBlocks = new ItemStack(Material.GOLD_BLOCK, Math.min(8, Math.max(1, bounty/200)));
            ItemStack egap = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1);
            ItemStack sword = com.example.killstreak.CustomItems.createBountyBlade(this.plugin);

            killer.getInventory().addItem(netherite);
            killer.getInventory().addItem(goldBlocks);
            killer.getInventory().addItem(egap);
            killer.getInventory().addItem(sword);
            killer.sendMessage("\u00A7aYou collected a bounty of " + bounty + "! Rewards granted.");
            bountyManager.clearBounty(victim.getUniqueId());
        }
        // reset victim
        ksManager.reset(victim.getUniqueId());
    }
}
