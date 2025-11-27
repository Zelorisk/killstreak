package com.example.killstreak;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {
    private final KSPlugin plugin;
    private final File file;
    private YamlConfiguration cfg;

    public DataManager(KSPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException ignored) {}
        }
        cfg = YamlConfiguration.loadConfiguration(file);
    }

    public Map<java.util.UUID, Integer> loadBounties() {
        Map<UUID,Integer> map = new HashMap<>();
        if (!cfg.isConfigurationSection("bounties")) return map;
        for (String key : cfg.getConfigurationSection("bounties").getKeys(false)) {
            try { map.put(UUID.fromString(key), cfg.getInt("bounties."+key)); } catch (Exception ignored) {}
        }
        return map;
    }

    public Map<String, Map<Integer,String>> loadAbilities() {
        Map<String, Map<Integer,String>> out = new HashMap<>();
        if (!cfg.isConfigurationSection("abilities")) return out;
        for (String key : cfg.getConfigurationSection("abilities").getKeys(false)) {
            Map<Integer,String> m = new HashMap<>();
            for (String lvl : cfg.getConfigurationSection("abilities."+key).getKeys(false)) {
                m.put(Integer.parseInt(lvl), cfg.getString("abilities."+key+"."+lvl));
            }
            out.put(key, m);
        }
        return out;
    }

    public void saveAll(Map<java.util.UUID,Integer> bounties, Map<java.util.UUID, Map<Integer,String>> abilities) {
        cfg.set("bounties", null);
        cfg.set("abilities", null);
        for (Map.Entry<UUID,Integer> e : bounties.entrySet()) {
            cfg.set("bounties."+e.getKey().toString(), e.getValue());
        }
        for (Map.Entry<UUID, Map<Integer,String>> e : abilities.entrySet()) {
            String base = "abilities."+e.getKey().toString()+".";
            for (Map.Entry<Integer,String> aa : e.getValue().entrySet()) {
                cfg.set(base + aa.getKey(), aa.getValue());
            }
        }
        try { cfg.save(file); } catch (IOException ex) { plugin.getLogger().warning("Failed to save data.yml: " + ex.getMessage()); }
    }
}
