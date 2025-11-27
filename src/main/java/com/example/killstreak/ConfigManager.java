package com.example.killstreak;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final KSPlugin plugin;
    private boolean dropTriggersAbility;

    public ConfigManager(KSPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        FileConfiguration c = plugin.getConfig();
        dropTriggersAbility = c.getBoolean("trigger.drop-activates-ability", true);
    }

    public boolean isDropTriggersAbility() { return dropTriggersAbility; }

    public void setDropTriggersAbility(boolean v) {
        dropTriggersAbility = v;
        plugin.getConfig().set("trigger.drop-activates-ability", v);
        plugin.saveConfig();
    }
}
