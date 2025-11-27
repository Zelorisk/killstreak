package com.example.killstreak;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.Random;

public class EventManager implements Listener {
    private final KSPlugin plugin;
    private final BountyManager bountyManager;
    private final KillstreakManager ksManager;
    private final Random random = new Random();

    public EventManager(KSPlugin plugin, BountyManager bountyManager, KillstreakManager ksManager) {
        this.plugin = plugin;
        this.bountyManager = bountyManager;
        this.ksManager = ksManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void startScheduling() {
        scheduleNext();
    }

    private void scheduleNext() {
        // schedule between 1 and 3 hours
        int hours = 1 + random.nextInt(3);
        long ticks = hours * 60L * 60L * 20L;
        new BukkitRunnable(){
            @Override public void run(){ triggerEvent(); scheduleNext(); }
        }.runTaskLater(plugin, ticks);
    }

    public void triggerEvent() {
        World world = plugin.getServer().getWorlds().get(0);
        Location spawn = world.getSpawnLocation();
        // Find a random above-ground location within 4000 blocks of spawn
        Location bossLoc = null;
        int tries = 0;
        while (tries++ < 30) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double dist = 500 + random.nextDouble() * 3500; // 500-4000 blocks
            int x = (int) (spawn.getX() + Math.cos(angle) * dist);
            int z = (int) (spawn.getZ() + Math.sin(angle) * dist);
            int y = world.getHighestBlockYAt(x, z) + 1;
            if (y < 60 || y > 200) continue; // avoid caves/void/sky
            bossLoc = new Location(world, x + 0.5, y, z + 0.5);
            Material block = world.getBlockAt(x, y - 1, z).getType();
            if (block.isAir() || block == Material.WATER || block == Material.LAVA) continue;
            break;
        }
        if (bossLoc == null) bossLoc = spawn.add(100, 1, 100); // fallback

        // Randomly choose between Wither or Meteor Golem event
        if (random.nextBoolean()) {
            // Wither event (as before)
            LivingEntity wither = (LivingEntity) world.spawnEntity(bossLoc, EntityType.WITHER);
            wither.setCustomName("§5Event Wither");
            wither.setCustomNameVisible(true);
            wither.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(600.0);
            wither.setHealth(600.0);
            wither.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(20.0);
            wither.setGlowing(true);
            final Location eventCenter = bossLoc.clone();
            new BukkitRunnable() {
                int ticks = 0;
                @Override public void run() {
                    if (!wither.isValid() || wither.isDead()) { cancel(); return; }
                    double dist = wither.getLocation().distance(eventCenter);
                    if (dist > 200) {
                        Vector pull = eventCenter.toVector().subtract(wither.getLocation().toVector()).normalize().multiply(2.0);
                        wither.setVelocity(pull);
                    }
                    if (wither.getLocation().getY() < eventCenter.getY() - 2) {
                        Location tp = wither.getLocation().clone();
                        tp.setY(eventCenter.getY());
                        wither.teleport(tp);
                    }
                    if (ticks % (20*10) == 0 && ticks > 0) {
                        world.createExplosion(wither.getLocation(), 2.5f, false, false, wither);
                        for (Player p : world.getPlayers()) {
                            if (p.getLocation().distance(wither.getLocation()) < 8) {
                                p.damage(4.0, wither);
                            }
                        }
                    }
                    world.spawnParticle(org.bukkit.Particle.CLOUD, wither.getLocation(), 40, 2, 2, 2, 0.1);
                    if (++ticks > 20*60*10) cancel();
                }
            }.runTaskTimer(plugin, 0, 10);
            String coords = String.format("X: %d, Z: %d", bossLoc.getBlockX(), bossLoc.getBlockZ());
            String legacyMsg = "§6A massive Event Wither has spawned at §b" + coords + "§6! Defeat it for great rewards!";
            Bukkit.broadcastMessage(legacyMsg);
        } else {
            // Meteor Golem event
            LivingEntity golem = (LivingEntity) world.spawnEntity(bossLoc, EntityType.IRON_GOLEM);
            golem.setCustomName("§cMeteor Golem");
            golem.setCustomNameVisible(true);
            golem.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(800.0);
            golem.setHealth(800.0);
            golem.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(30.0);
            golem.setGlowing(true);
            final Location eventCenter = bossLoc.clone();
            new BukkitRunnable() {
                int ticks = 0;
                @Override public void run() {
                    if (!golem.isValid() || golem.isDead()) { cancel(); return; }
                    double dist = golem.getLocation().distance(eventCenter);
                    if (dist > 200) {
                        Vector pull = eventCenter.toVector().subtract(golem.getLocation().toVector()).normalize().multiply(2.0);
                        golem.setVelocity(pull);
                    }
                    if (golem.getLocation().getY() < eventCenter.getY() - 2) {
                        Location tp = golem.getLocation().clone();
                        tp.setY(eventCenter.getY());
                        golem.teleport(tp);
                    }
                    // Meteor shower every 10 seconds
                    if (ticks % (20*10) == 0 && ticks > 0) {
                        for (int i = 0; i < 8; i++) {
                            Location meteorLoc = golem.getLocation().clone().add(random.nextInt(16)-8, 20, random.nextInt(16)-8);
                            world.spawnEntity(meteorLoc, EntityType.FALLING_BLOCK).setVelocity(new Vector(0, -1.5, 0));
                            world.createExplosion(meteorLoc, 2.0f, false, false, golem);
                        }
                        for (Player p : world.getPlayers()) {
                            if (p.getLocation().distance(golem.getLocation()) < 12) {
                                p.damage(6.0, golem);
                            }
                        }
                    }
                    world.spawnParticle(org.bukkit.Particle.FLAME, golem.getLocation(), 60, 2, 2, 2, 0.2);
                    if (++ticks > 20*60*10) cancel();
                }
            }.runTaskTimer(plugin, 0, 10);
            String coords = String.format("X: %d, Z: %d", bossLoc.getBlockX(), bossLoc.getBlockZ());
            String legacyMsg = "§6A Meteor Golem event has started at §b" + coords + "§6! Defeat it for a legendary reward!";
            Bukkit.broadcastMessage(legacyMsg);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (!(e.getEntity().getType() == EntityType.WITHER || e.getEntity().getType() == EntityType.IRON_GOLEM)) return;
        if (!e.getEntity().getLocation().getWorld().equals(plugin.getServer().getWorlds().get(0))) return;
        Player killer = e.getEntity().getKiller();
        if (killer != null) {
            if (e.getEntity().getType() == EntityType.WITHER) {
                killer.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_BLOCK, 2));
                killer.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.BEACON, 1));
                bountyManager.addBounty(killer.getUniqueId(), 1000);
                ksManager.addKill(killer, killer);
                killer.sendMessage("§dYou have defeated the Event Wither! Rewards granted.");
            } else if (e.getEntity().getType() == EntityType.IRON_GOLEM) {
                // Legendary reward: Meteor Staff
                org.bukkit.inventory.ItemStack staff = com.example.killstreak.CustomItems.createMeteorStaff(plugin);
                killer.getInventory().addItem(staff);
                killer.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHER_STAR, 1));
                bountyManager.addBounty(killer.getUniqueId(), 2000);
                ksManager.addKill(killer, killer);
                killer.sendMessage("§dYou have defeated the Meteor Golem! Legendary rewards granted.");
            }
        }
    }
}
