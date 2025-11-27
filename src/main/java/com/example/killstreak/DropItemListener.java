package com.example.killstreak;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityDamageEvent;

public class DropItemListener implements Listener {
    private final KillstreakManager ksManager;
    private final ConfigManager cfg;
    private final KSPlugin plugin;
    private final Map<UUID, Long> cooldown = new HashMap<>();
    private final Map<UUID, Long> dashProtection = new HashMap<>();
    private final Map<UUID, Long> celestialCloakCooldown = new HashMap<>();

    private int bountyBladeCrafts = 3; // Maximum crafts allowed
    private int maceCrafts = 3; // Maximum crafts allowed
    private int phoenixFeatherCrafts = 3; // Maximum crafts allowed
    private int meteorStaffCrafts = 3; // Maximum crafts allowed
    private int voidCleaverCrafts = 3; // Maximum crafts allowed
    private int celestialCloakCrafts = 3; // Maximum crafts allowed
    private int dashCrystalCrafts = 3; // Maximum crafts allowed

    public DropItemListener(KSPlugin plugin, KillstreakManager ksManager, ConfigManager cfg) {
        this.plugin = plugin;
        this.ksManager = ksManager;
        this.cfg = cfg;
    }

    private org.bukkit.Location findSafeTeleportLocation(Player p, int maxDistance) {
        org.bukkit.Location start = p.getLocation().clone();
        Vector dir = start.getDirection().normalize();
        for (int step = 1; step <= maxDistance; step++) {
            org.bukkit.Location cand = start.clone().add(dir.clone().multiply(step));
            org.bukkit.block.Block b = cand.getBlock();
            if (!b.getType().isSolid() && !b.getRelative(0,1,0).getType().isSolid() && !b.getRelative(0,2,0).getType().isSolid()) {
                // ensure not in liquid
                if (b.getType() == org.bukkit.Material.WATER || b.getType() == org.bukkit.Material.LAVA) continue;
                return cand.add(0.5, 0, 0.5);
            }
            // allow inside caves slightly below with air; try downward search up to 5 blocks
            for (int down=1;down<=5;down++) {
                org.bukkit.block.Block b2 = b.getRelative(0, -down, 0);
                if (!b2.getType().isSolid() && !b2.getRelative(0,1,0).getType().isSolid()) {
                    return b2.getLocation().add(0.5, 0, 0.5);
                }
            }
        }
        return null;
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (e.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        Player p = (Player) e.getEntity();
        UUID id = p.getUniqueId();
        Long protectionExpiry = dashProtection.get(id);
        if (protectionExpiry != null && System.currentTimeMillis() < protectionExpiry) {
            e.setCancelled(true);
            p.setFallDistance(0);
            // Show particle effect to indicate protection
            p.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, p.getLocation().add(0, 0.5, 0), 10, 0.3, 0.1, 0.3, 0.01);
            // Remove protection after use
            dashProtection.remove(id);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.CREATIVE) return;

        // Check for Celestial Cloak boost ability FIRST (when wearing cloak and dropping anything)
        org.bukkit.inventory.ItemStack chest = p.getInventory().getChestplate();
        boolean hasCloakEquipped = chest != null && com.example.killstreak.CustomItems.isCustom(chest, plugin, "celestial_cloak");
        if (hasCloakEquipped) {
            // Add cooldown to prevent spamming
            long now = System.currentTimeMillis();
            long lastCooldown = celestialCloakCooldown.getOrDefault(p.getUniqueId(), 0L);
            if (now - lastCooldown < 3000) {
                p.sendMessage("\u00A7cCelestial Cloak ability is on cooldown.");
                return;
            }
            e.setCancelled(true);
            p.setVelocity(p.getLocation().getDirection().multiply(5).setY(2)); // Overpowered boost
            p.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION, p.getLocation(), 10, 1, 1, 1, 0.1);
            p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1f, 1f);
            p.sendMessage("\u00A7bYou activated the Celestial Cloak's boost ability!");
            celestialCloakCooldown.put(p.getUniqueId(), now);
            return;
        }
        
        if (!cfg.isDropTriggersAbility()) return;
        UUID id = p.getUniqueId();
        // If player lacks dash/teleport or dash crystal, nothing to trigger
        boolean hasCrystal = java.util.Arrays.stream(p.getInventory().getContents()).anyMatch(it -> it != null && com.example.killstreak.CustomItems.isCustom(it, plugin, "dash_crystal"));
        if (!ksManager.hasDash(id) && !hasCrystal) return;
        long now = System.currentTimeMillis();
        long cd = cooldown.getOrDefault(id, 0L);
        if (now - cd < 5000) { // 5s cooldown
            p.sendMessage("\u00A7cDash is on cooldown.");
            e.setCancelled(true);
            return;
        }
        // Cancel drop of dash crystal
        if (e.getItemDrop().getItemStack() != null && com.example.killstreak.CustomItems.isCustom(e.getItemDrop().getItemStack(), plugin, "dash_crystal")) {
            e.setCancelled(true);
            p.sendMessage("\u00A7cDash Crystals cannot be dropped!");
            return;
        }
        // Cancel drop and trigger dash ability (works with any item drop as long as crystal is in inventory)
        e.setCancelled(true);
        // If player has TELEPORT upgrade, attempt to teleport instead
        boolean teleportUpgrade = plugin.getAbilitiesManager().getAllChoices().getOrDefault(id, java.util.Map.of()).values().stream().anyMatch(x -> x.equalsIgnoreCase("TELEPORT"));
        Vector dir = p.getLocation().getDirection().normalize();
        if (teleportUpgrade) {
            // Teleport up to 75 blocks along look direction, including underground as long as destination has room
            org.bukkit.Location target = findSafeTeleportLocation(p, 75);
            if (target != null) {
                // 10 minute cooldown
                // use TeleportCooldowns singleton
                if (com.example.killstreak.util.TeleportCooldowns.get().isOnCooldown(id)) {
                    p.sendMessage("\u00A7cTeleport is on cooldown.");
                    return;
                }
                com.example.killstreak.util.TeleportCooldowns.get().setCooldown(id, 10*60*1000);
                p.teleport(target);
                p.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, target, 30, 0.5, 0.5, 0.5, 0.01);
                p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                return;
            } else {
                p.sendMessage("\u00A7cNo safe teleport location found.");
                return;
            }
        }
        // horizontal component (reduced for balance)
        Vector horiz = new Vector(dir.getX(), 0, dir.getZ());
        if (horiz.length() > 0.001) horiz = horiz.normalize().multiply(2.1); // was 3.0
        // vertical component scaled by look Y (slightly reduced)
        double vy = Math.max(0.4, dir.getY() * 2.2 + 0.18); // was 3.0, 0.2
        Vector finalV = horiz.setY(vy);
        p.setVelocity(finalV);
        cooldown.put(id, now);
        // Grant fall damage protection for 3 seconds after dash
        dashProtection.put(id, now + 3000);
        p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_FLAP, 0.7f, 1.2f);
        p.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, p.getLocation().add(0,1,0), 20, 0.3, 0.2, 0.3, 0.01);
    }

    @EventHandler
    public void onCustomItemDespawn(org.bukkit.event.entity.ItemDespawnEvent e) {
        org.bukkit.inventory.ItemStack stack = e.getEntity().getItemStack();
        if (CustomItems.isCustom(stack, plugin, "dash_crystal") ||
            CustomItems.isCustom(stack, plugin, "bounty_blade") ||
            CustomItems.isCustom(stack, plugin, "mace") ||
            CustomItems.isCustom(stack, plugin, "phoenix_feather") ||
            CustomItems.isCustom(stack, plugin, "meteor_staff") ||
            CustomItems.isCustom(stack, plugin, "void_cleaver") ||
            CustomItems.isCustom(stack, plugin, "celestial_cloak")) {
            e.setCancelled(true); // Prevent despawning
        }
    }

    @EventHandler
    public void onCustomItemDamage(org.bukkit.event.entity.EntityDamageEvent e) {
        if (e.getEntity() instanceof org.bukkit.entity.Item) {
            org.bukkit.entity.Item item = (org.bukkit.entity.Item) e.getEntity();
            org.bukkit.inventory.ItemStack stack = item.getItemStack();
            if (CustomItems.isCustom(stack, plugin, "dash_crystal")) {
                e.setCancelled(true);
                if (e.getFinalDamage() >= item.getHealth()) {
                    Bukkit.broadcastMessage("\u00A7dA Dash Crystal was destroyed! A craft slot is now available.");
                    dashCrystalCrafts++;
                    item.remove();
                }
            } else if (CustomItems.isCustom(stack, plugin, "bounty_blade")) {
                e.setCancelled(true);
                if (e.getFinalDamage() >= item.getHealth()) {
                    Bukkit.broadcastMessage("\u00A7dA Bounty Blade was destroyed! A craft slot is now available.");
                    bountyBladeCrafts++;
                    item.remove();
                }
            } else if (CustomItems.isCustom(stack, plugin, "mace")) {
                e.setCancelled(true);
                if (e.getFinalDamage() >= item.getHealth()) {
                    Bukkit.broadcastMessage("\u00A7dA Mace was destroyed! A craft slot is now available.");
                    maceCrafts++;
                    item.remove();
                }
            } else if (CustomItems.isCustom(stack, plugin, "phoenix_feather")) {
                e.setCancelled(true);
                if (e.getFinalDamage() >= item.getHealth()) {
                    Bukkit.broadcastMessage("\u00A7dA Phoenix Feather was destroyed! A craft slot is now available.");
                    phoenixFeatherCrafts++;
                    item.remove();
                }
            } else if (CustomItems.isCustom(stack, plugin, "meteor_staff")) {
                e.setCancelled(true);
                if (e.getFinalDamage() >= item.getHealth()) {
                    Bukkit.broadcastMessage("\u00A7dA Meteor Staff was destroyed! A craft slot is now available.");
                    meteorStaffCrafts++;
                    item.remove();
                }
            } else if (CustomItems.isCustom(stack, plugin, "void_cleaver")) {
                e.setCancelled(true);
                if (e.getFinalDamage() >= item.getHealth()) {
                    Bukkit.broadcastMessage("\u00A7dA Void Cleaver was destroyed! A craft slot is now available.");
                    voidCleaverCrafts++;
                    item.remove();
                }
            } else if (CustomItems.isCustom(stack, plugin, "celestial_cloak")) {
                e.setCancelled(true);
                if (e.getFinalDamage() >= item.getHealth()) {
                    Bukkit.broadcastMessage("\u00A7dA Celestial Cloak was destroyed! A craft slot is now available.");
                    celestialCloakCrafts++;
                    item.remove();
                }
            }
        }
    }

    public boolean canCraft(String item) {
        switch (item) {
            case "bountyblade": return bountyBladeCrafts > 0;
            case "mace": return maceCrafts > 0;
            case "phoenixfeather": return phoenixFeatherCrafts > 0;
            case "meteorstaff": return meteorStaffCrafts > 0;
            case "voidcleaver": return voidCleaverCrafts > 0;
            case "celestialcloak": return celestialCloakCrafts > 0;
            case "dashcrystal": return dashCrystalCrafts > 0;
            default: return true;
        }
    }

    public void decrementCraft(String item) {
        switch (item) {
            case "bountyblade": bountyBladeCrafts--; break;
            case "mace": maceCrafts--; break;
            case "phoenixfeather": phoenixFeatherCrafts--; break;
            case "meteorstaff": meteorStaffCrafts--; break;
            case "voidcleaver": voidCleaverCrafts--; break;
            case "celestialcloak": celestialCloakCrafts--; break;
            case "dashcrystal": dashCrystalCrafts--; break;
        }
    }
}
