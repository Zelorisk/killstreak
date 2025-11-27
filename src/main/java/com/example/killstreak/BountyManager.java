package com.example.killstreak;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;
import com.example.killstreak.CustomItems;

public class BountyManager {
    private final KSPlugin plugin;
    private final Map<UUID, Integer> bounties = new HashMap<>();
    private final Scoreboard board;
    private final Objective obj;

    public BountyManager(KSPlugin plugin) {
        this.plugin = plugin;
        board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective o = board.getObjective("bounties");
        if (o == null) {
            o = board.registerNewObjective(
                "bounties",
                "dummy",
                "Top Bounties"
            );
        }
        o.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj = o;
    }

    public void addBounty(UUID player, int amount) {
        bounties.put(player, bounties.getOrDefault(player, 0) + amount);
        refreshScoreboard();
    }

    public void setBounty(UUID player, int amount) {
        bounties.put(player, amount);
        refreshScoreboard();
    }

    public void removeBounty(UUID player) {
        bounties.remove(player);
        refreshScoreboard();
    }

    public int getBounty(UUID player) {
        return bounties.getOrDefault(player, 0);
    }

    public void clearBounty(UUID player) {
        bounties.remove(player);
        String name = Bukkit.getOfflinePlayer(player).getName();
        if (name != null) board.resetScores(name);
        refreshScoreboard();
    }

    public Map<UUID,Integer> getBounties(){ return Collections.unmodifiableMap(bounties); }

    private void updateScoreboard(UUID player) {
        refreshScoreboard();
    }

    private void refreshScoreboard() {
        // clear all entries in objective
        for (String entry : board.getEntries()) {
            try { board.resetScores(entry); } catch (Exception ignored) {}
        }
        // show top 10
        List<Map.Entry<UUID,Integer>> top = topBounties(10);
        int score = top.size();
        for (Map.Entry<UUID,Integer> e : top) {
            String name = Bukkit.getOfflinePlayer(e.getKey()).getName();
            if (name == null) name = e.getKey().toString().substring(0, 8);
            int bountyValue = e.getValue();
            // Show rewards based on bounty value
            String rewardText = getBountyRewardText(bountyValue);
            String entry = "§e" + name + " §6" + rewardText;
            obj.getScore(entry).setScore(score--);
        }
        // Show for all online players
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setScoreboard(board);
        }
    }

    public void checkInventoryForBounty(Player p) {
        if (p == null) return;
        // simple valuable items detection
        boolean hasValuable = false;
        for (org.bukkit.inventory.ItemStack it : p.getInventory().getContents()) {
            if (it == null) continue;
            Material m = it.getType();
            if (m == Material.DRAGON_EGG || m == Material.NETHER_STAR || m == Material.NETHERITE_INGOT ||
                m == Material.DIAMOND || m == Material.EMERALD || m == Material.GOLD_INGOT ||
                m == Material.IRON_INGOT || m == Material.NETHERITE_INGOT) {
                hasValuable = true; break;
            }
            if (it.getItemMeta() != null) {
                if (CustomItems.isCustom(it, plugin, "mace") || CustomItems.isCustom(it, plugin, "bounty_blade") ||
                    CustomItems.isCustom(it, plugin, "meteor_staff") || CustomItems.isCustom(it, plugin, "phoenix_feather") ||
                    CustomItems.isCustom(it, plugin, "void_cleaver") || CustomItems.isCustom(it, plugin, "celestial_cloak") ||
                    CustomItems.isCustom(it, plugin, "dash_crystal")) {
                    hasValuable = true; break;
                }
                if (it.getItemMeta().displayName() != null &&
                    LegacyComponentSerializer.legacySection().serialize(it.getItemMeta().displayName()).contains("Mace")) {
                    hasValuable = true; break;
                }
            }
        }
        // Always add bounty for testing - remove this line in production
        if (!hasValuable) hasValuable = true;
        if (hasValuable) addBounty(p.getUniqueId(), 500);
    }

    private String getBountyRewardText(int bountyValue) {
        if (bountyValue >= 5000) {
            return "[64 Gold Blocks, 32 Diamonds, Bounty Blade, Dash Crystal]";
        } else if (bountyValue >= 2500) {
            return "[32 Gold Blocks, 16 Diamonds, Bounty Blade]";
        } else if (bountyValue >= 1000) {
            return "[16 Gold Blocks, 8 Diamonds, Phoenix Feather]";
        } else if (bountyValue >= 500) {
            return "[8 Gold Blocks, 4 Diamonds, Mace]";
        } else {
            return "[4 Gold Blocks, 2 Diamonds]";
        }
    }

    public List<Map.Entry<UUID,Integer>> topBounties(int limit) {
        List<Map.Entry<UUID,Integer>> list = new ArrayList<>(bounties.entrySet());
        list.sort((a,b)->b.getValue()-a.getValue());
        return list.subList(0, Math.min(limit, list.size()));
    }
}
