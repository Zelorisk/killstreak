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
import java.util.List;
import java.util.ArrayList;

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

        // Only award killstreaks and bounties for player-vs-player kills
        // victim.getKiller() returns null for non-player causes (entities, environment, etc.)
        if (killer == null || !(killer instanceof Player)) {
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
        // Give blood essence for the kill
        plugin.getBloodEssenceManager().addEssence(killer.getUniqueId(), 10);

        // Check if victim has valuable items for bounty
        plugin.getBountyManager().checkInventoryForBounty(victim);

        // reward killer if victim had a bounty
        int bounty = bountyManager.getBounty(victim.getUniqueId());
        if (bounty > 0) {
            // Give rewards based on bounty value
            List<ItemStack> rewards = new ArrayList<>();
            String rewardMessage = "";

            if (bounty >= 5000) {
                rewards.add(new ItemStack(Material.GOLD_BLOCK, 64));
                rewards.add(new ItemStack(Material.DIAMOND, 32));
                rewards.add(com.example.killstreak.CustomItems.createBountyBlade(this.plugin));
                rewards.add(com.example.killstreak.CustomItems.createDashCrystal(this.plugin));
                rewardMessage = "64 Gold Blocks, 32 Diamonds, Bounty Blade, Dash Crystal!";
            } else if (bounty >= 2500) {
                rewards.add(new ItemStack(Material.GOLD_BLOCK, 32));
                rewards.add(new ItemStack(Material.DIAMOND, 16));
                rewards.add(com.example.killstreak.CustomItems.createBountyBlade(this.plugin));
                rewardMessage = "32 Gold Blocks, 16 Diamonds, Bounty Blade!";
            } else if (bounty >= 1000) {
                rewards.add(new ItemStack(Material.GOLD_BLOCK, 16));
                rewards.add(new ItemStack(Material.DIAMOND, 8));
                rewards.add(com.example.killstreak.CustomItems.createPhoenixFeather(this.plugin));
                rewardMessage = "16 Gold Blocks, 8 Diamonds, Phoenix Feather!";
            } else if (bounty >= 500) {
                rewards.add(new ItemStack(Material.GOLD_BLOCK, 8));
                rewards.add(new ItemStack(Material.DIAMOND, 4));
                rewards.add(com.example.killstreak.CustomItems.createMace(this.plugin));
                rewardMessage = "8 Gold Blocks, 4 Diamonds, Mace!";
            } else {
                rewards.add(new ItemStack(Material.GOLD_BLOCK, 4));
                rewards.add(new ItemStack(Material.DIAMOND, 2));
                rewardMessage = "4 Gold Blocks, 2 Diamonds!";
            }

            // Give rewards to killer
            for (ItemStack reward : rewards) {
                killer.getInventory().addItem(reward);
            }

            killer.sendMessage("\u00A7aYou collected a bounty! Rewards: " + rewardMessage);
            killer.getWorld().spawnParticle(org.bukkit.Particle.TOTEM_OF_UNDYING, killer.getLocation().add(0, 2, 0), 50, 0.5, 0.5, 0.5, 0.1);
            killer.playSound(killer.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

            bountyManager.clearBounty(victim.getUniqueId());
        }
        // reset victim
        ksManager.reset(victim.getUniqueId());
    }
}
