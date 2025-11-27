package com.example.killstreak.listeners;

import com.example.killstreak.CustomItems;
import com.example.killstreak.KSPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class CustomItemListener implements Listener {
    private final KSPlugin plugin;
    private final Random rand = new Random();
    private final java.util.Map<java.util.UUID, Long> meteorCooldown = new ConcurrentHashMap<>();
    private final java.util.Map<java.util.UUID, Integer> dashCrystalBackup = new ConcurrentHashMap<>();

    public CustomItemListener(KSPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.OFF_HAND) return; // ignore offhand
        Player p = e.getPlayer();
        ItemStack it = p.getInventory().getItemInMainHand();
        if (it == null || !it.hasItemMeta()) return;
        if (!(e.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR || e.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)) return;
        // Meteor Staff
        if (CustomItems.isCustom(it, plugin, "meteor_staff")) {
            long now = System.currentTimeMillis();
            long last = meteorCooldown.getOrDefault(p.getUniqueId(), 0L);
            if (now - last < 5000) {
                p.sendMessage("\u00A7cMeteor Staff is on cooldown.");
                return;
            }
            e.setCancelled(true);
            Integer uses = it.getItemMeta().getPersistentDataContainer().get(CustomItems.key(plugin, "meteor_staff"), PersistentDataType.INTEGER);
            if (uses == null || uses <= 0) {
                p.sendMessage("\u00A7cYour Meteor Staff has no charges left.");
                return;
            }
            World world = p.getWorld();
            // spawn a meteor shower near where player is looking
            for (int i=0;i<6;i++) {
                Vector dir = p.getLocation().getDirection().clone();
                double dx = (rand.nextDouble()-0.5)*8;
                double dz = (rand.nextDouble()-0.5)*8;
                org.bukkit.Location loc = p.getLocation().clone().add(dir.multiply(6)).add(dx, 20, dz);
                // show meteor particles and explode after a short delay but without spawning blocks
                world.spawnParticle(org.bukkit.Particle.FLAME, loc, 80, 2, 2, 2, 0.2);
                int delay = 18 + rand.nextInt(8);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    world.createExplosion(loc, 2.0f, false, false);
                    // radial damage bypassing armor: subtract directly from health
                    double rawHpDamage = 10.0; // 5 hearts
                    for (Player other : world.getPlayers()) {
                        if (other.getLocation().distance(loc) < 5 && other != p) { // Exclude the caster
                            double newHP = Math.max(0.0, other.getHealth() - rawHpDamage);
                            other.setHealth(newHP);
                            other.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION, other.getLocation(), 10);
                            other.sendMessage("\u00A7cYou were hit by a meteor and took " + (rawHpDamage / 2) + " hearts of damage!");
                        }
                    }
                }, delay);
            }
            // decrement uses
            int newUses = uses - 1;
            ItemStack clone = it.clone();
            var meta = clone.getItemMeta();
            meta.getPersistentDataContainer().set(CustomItems.key(plugin, "meteor_staff"), PersistentDataType.INTEGER, newUses);
            meta.setLore(java.util.List.of("Right-click to summon meteors (" + newUses + " uses left)"));
            clone.setItemMeta(meta);
            if (newUses <= 0) {
                p.getInventory().setItemInMainHand(null);
            } else {
                p.getInventory().setItemInMainHand(clone);
                meteorCooldown.put(p.getUniqueId(), now);
            }
            // particles and shockwave
            p.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, p.getLocation(), 20, 0.5, 0.5, 0.5, 0.01);
            p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_EVOKER_CAST_SPELL, 1f, 1f);
            p.sendMessage("\u00A7aYou summoned meteors (" + newUses + " uses left)");
            return;
        }

    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player)e.getEntity();
        // Prevent void death if wearing voidwalker boots
        if (e.getCause() == DamageCause.VOID) {
            ItemStack boots = p.getInventory().getBoots();
            if (boots != null && boots.hasItemMeta() && CustomItems.isCustom(boots, plugin, "voidwalker_boots")) {
                e.setCancelled(true);
                p.setFallDistance(0);
                p.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOW_FALLING, 20*10, 0, false, false));
                p.teleport(p.getWorld().getSpawnLocation());
                p.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, p.getLocation(), 30, 0.2, 0.2, 0.2, 0.01);
                p.playSound(p.getLocation(), org.bukkit.Sound.ITEM_TOTEM_USE, 1f, 1f);
                p.sendMessage("\u00A7aYour Voidwalker Boots saved you from the void!");
                return;
            }
        }
        // Phoenix Feather: prevent lethal damage
        if (e.getFinalDamage() >= p.getHealth()) {
            // find feather in inventory
            for (int i=0;i<p.getInventory().getSize();i++) {
                ItemStack it = p.getInventory().getItem(i);
                if (it == null) continue;
                if (CustomItems.isCustom(it, plugin, "phoenix_feather")) {
                    // consume one
                    ItemStack clone = it.clone();
                    clone.setAmount(it.getAmount()-1);
                    if (clone.getAmount() <= 0) p.getInventory().setItem(i, null); else p.getInventory().setItem(i, clone);
                    e.setCancelled(true);
                    p.setHealth(p.getMaxHealth());
                    p.setFireTicks(0);
                    p.setNoDamageTicks(20*5);
                    p.sendMessage("\u00A7eYour Phoenix Feather revived you!");
                    return;
                }
            }
        }
        // Voidwalker Boots: Grant Speed II and Jump Boost II
        if (e.getCause() == DamageCause.FALL) {
            ItemStack boots = p.getInventory().getBoots();
            if (boots != null && boots.hasItemMeta() && CustomItems.isCustom(boots, plugin, "voidwalker_boots")) {
                e.setCancelled(true);
                p.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 20*10, 1));
                p.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.JUMP_BOOST, 20*10, 1));
                p.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, p.getLocation(), 30, 0.2, 0.2, 0.2, 0.01);
                p.playSound(p.getLocation(), org.bukkit.Sound.ITEM_TOTEM_USE, 1f, 1f);
                p.sendMessage("\u00A7aYour Voidwalker Boots granted you agility!");
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent e) {
        Player p = e.getEntity();
        // Remove Dash Crystal from drops without duplicating
        for (org.bukkit.inventory.ItemStack it : new java.util.ArrayList<>(e.getDrops())) {
            if (it == null) continue;
            if (CustomItems.isCustom(it, plugin, "dash_crystal")) {
                e.getDrops().remove(it);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(org.bukkit.event.player.PlayerRespawnEvent e) {
        // Dash Crystals are no longer restored automatically to prevent duplication
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        ItemStack it = e.getItemDrop().getItemStack();
        if (it == null || !it.hasItemMeta()) return;

        // Dash Crystal cannot be dropped
        if (CustomItems.isCustom(it, plugin, "dash_crystal")) {
            e.setCancelled(true);
            p.sendMessage("\u00A7cDash Crystals cannot be dropped!");
        }
    }
}
