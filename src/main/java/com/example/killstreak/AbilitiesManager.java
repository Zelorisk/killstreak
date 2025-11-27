package com.example.killstreak;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import com.example.killstreak.CustomItems;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilitiesManager implements Listener {
    private final KSPlugin plugin;
    // mapping player -> level -> ability name
    private final Map<UUID, Map<Integer, String>> choices = new HashMap<>();

    public AbilitiesManager(KSPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        // regen task
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (UUID id : choices.keySet()) {
                Player p = Bukkit.getPlayer(id);
                if (p == null) continue;
                Map<Integer,String> map = choices.get(id);
                if (map == null) continue;
                if (map.containsValue("REGEN_AURA")) {
                    if (p.getHealth() < p.getMaxHealth()) {
                        Bukkit.getScheduler().runTask(plugin, () -> p.setHealth(Math.min(p.getMaxHealth(), p.getHealth()+1.0)));
                    }
                }
            }
        }, 20L*5, 20L*5);
    }

    public Map<java.util.UUID, Map<Integer,String>> getAllChoices() {
        return choices;
    }

    public void setChoice(UUID player, int level, String ability) {
        choices.computeIfAbsent(player, k -> new HashMap<>()).put(level, ability);
    }

    public String getChoice(UUID player, int level) {
        Map<Integer,String> m = choices.get(player);
        return m == null ? null : m.get(level);
    }

    public void applyImmediate(Player p, int level) {
        String ability = getChoice(p.getUniqueId(), level);
        if (ability == null) return;
        switch (ability) {
            case "STRENGTH":
                PotionEffectType streng = PotionEffectType.getByName("INCREASE_DAMAGE");
                if (streng != null) p.addPotionEffect(new PotionEffect(streng, Integer.MAX_VALUE, 2, false, false)); // stronger
                break;
            case "SPEED":
                PotionEffectType sp = PotionEffectType.getByName("SPEED");
                if (sp != null) p.addPotionEffect(new PotionEffect(sp, Integer.MAX_VALUE, 2, false, false));
                break;
            case "REGEN_AURA":
                // regen handled by scheduled task
                break;
            case "LIFESTEAL":
                // handled in damage listener
                break;
            case "INVIS":
                PotionEffectType inv = PotionEffectType.getByName("INVISIBILITY");
                if (inv != null) p.addPotionEffect(new PotionEffect(inv, Integer.MAX_VALUE, 0, false, false));
                break;
            case "LEAP":
                // short upward boost once
                p.setVelocity(p.getLocation().getDirection().multiply(1.5).setY(0.6));
                break;
            case "TELEPORT":
                // Teleport is used via drop/crystal; no immediate effect
                p.sendMessage("\u00A7eTeleport upgrade selected: you can now teleport with the Dash Crystal (10 min cooldown). ");
                break;
            default:
                break;
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        Entity dam = e.getDamager();
        if (!(dam instanceof Player)) return;
        Player attacker = (Player) dam;
        String ls = getChoice(attacker.getUniqueId(), 0) ; // check level-less ability? also check all assigned abilities
        // instead, check if attacker has any LIFESTEAL among their choices
        Map<Integer,String> m = choices.get(attacker.getUniqueId());
        if (m == null) return;
        boolean lifesteal = m.values().stream().anyMatch(s -> s.equalsIgnoreCase("LIFESTEAL"));
        if (lifesteal && e.getEntity() instanceof Player) {
            Player victim = (Player) e.getEntity();
            double heal = Math.min(attacker.getMaxHealth(), attacker.getHealth() + (e.getFinalDamage() * 0.75) + 2.0);
            Bukkit.getScheduler().runTask(plugin, () -> attacker.setHealth(Math.min(attacker.getMaxHealth(), heal)));
        }
        // Determine if attacker has custom weapon and apply effects
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if (weapon != null && weapon.hasItemMeta()) {
            if (CustomItems.isCustom(weapon, plugin, "void_cleaver")) {
                // bypass armor: apply extra raw health damage equal to 50% of final damage
                if (e.getEntity() instanceof org.bukkit.entity.LivingEntity) {
                    org.bukkit.entity.LivingEntity victim = (org.bukkit.entity.LivingEntity) e.getEntity();
                    double extra = e.getFinalDamage() * 0.5;
                    double newHp = Math.max(0.0, victim.getHealth() - extra);
                    Bukkit.getScheduler().runTask(plugin, () -> victim.setHealth(newHp));
                }
            }
            if (CustomItems.isCustom(weapon, plugin, "bounty_blade")) {
                // bounty blade heals attacker based on damage
                double heal = Math.min(attacker.getMaxHealth(), attacker.getHealth() + (e.getFinalDamage()*0.5));
                Bukkit.getScheduler().runTask(plugin, () -> attacker.setHealth(Math.min(attacker.getMaxHealth(), heal)));
            }
            if (CustomItems.isCustom(weapon, plugin, "void_cleaver")) {
                // Extra particle and knockback
                e.getEntity().getWorld().spawnParticle(org.bukkit.Particle.CRIT, e.getEntity().getLocation(), 10);
            }
        }
    }
}
