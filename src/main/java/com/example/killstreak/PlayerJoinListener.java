package com.example.killstreak;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class PlayerJoinListener implements Listener {
    private final KSPlugin plugin;

    public PlayerJoinListener(KSPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        // Always set scoreboard on join
        plugin.getBountyManager().getClass(); // ensure loaded
        try {
            java.lang.reflect.Field boardField = plugin.getBountyManager().getClass().getDeclaredField("board");
            boardField.setAccessible(true);
            org.bukkit.scoreboard.Scoreboard board = (org.bukkit.scoreboard.Scoreboard) boardField.get(plugin.getBountyManager());
            p.setScoreboard(board);
        } catch (Exception ignored) {}
        // give 30 minute grace
        p.setMetadata("ks_grace", new FixedMetadataValue(plugin, true));
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            p.removeMetadata("ks_grace", plugin);
            p.sendMessage("\u00A7aYour 30-minute grace period has ended.");
        }, 30 * 60 * 20L);

        // first-join one-time GUI
        java.util.List<String> seen = plugin.getConfig().getStringList("seenPlayers");
        plugin.getLogger().info("Player " + p.getName() + " joining. Seen players: " + seen.size() + ", UUID in seen: " + seen.contains(p.getUniqueId().toString()));
        if (!seen.contains(p.getUniqueId().toString())) {
            // open simple info GUI
            new com.example.killstreak.gui.FirstJoinGUI(plugin).open(p);
            // give Dash Crystal ONLY on first join to prevent duplication
            plugin.getLogger().info("Giving first join Dash Crystal to " + p.getName());
            ItemStack crystal = com.example.killstreak.CustomItems.createDashCrystal(plugin);
            p.getInventory().addItem(crystal);
            seen.add(p.getUniqueId().toString());
            plugin.getConfig().set("seenPlayers", seen);
            plugin.saveConfig();
            plugin.getLogger().info("Saved seen players list with " + seen.size() + " players");
        } else {
            plugin.getLogger().info("Player " + p.getName() + " already seen, not giving Dash Crystal");
        }
    }
}
