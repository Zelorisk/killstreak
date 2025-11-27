package com.example.killstreak;

import com.example.killstreak.listeners.PlayerKillListener;
import org.bukkit.plugin.java.JavaPlugin;

public class KSPlugin extends JavaPlugin {
    private static KSPlugin instance;
    private KillstreakManager ksManager;
    private BountyManager bountyManager;
    private EventManager eventManager;
    private ConfigManager cfg;
    private AbilitiesManager abilitiesManager;
    private com.example.killstreak.gui.AbilityPickGUI abilityPickGUI;
    private com.example.killstreak.gui.ConfigGUI configGUI;
    private com.example.killstreak.gui.BountiesGUI bountiesGUI;
    private com.example.killstreak.gui.RecipesGUI recipesGUI;
    private DataManager dataManager;
    private DropItemListener dropItemListener;
    private BloodEssenceManager bloodEssenceManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        cfg = new ConfigManager(this);

        dataManager = new DataManager(this);
        bountyManager = new BountyManager(this);
        bloodEssenceManager = new BloodEssenceManager(this);
        ksManager = new KillstreakManager(this, bountyManager);
        eventManager = new EventManager(this, bountyManager, ksManager);
        abilitiesManager = new AbilitiesManager(this);
        abilityPickGUI = new com.example.killstreak.gui.AbilityPickGUI(this, abilitiesManager);
        // register tree GUI once
        new com.example.killstreak.gui.TreeUpgradeGUI(this, abilitiesManager);
        // register FirstJoinGUI as listener for click events
        new com.example.killstreak.gui.FirstJoinGUI(this);
        configGUI = new com.example.killstreak.gui.ConfigGUI(this, cfg);
        bountiesGUI = new com.example.killstreak.gui.BountiesGUI(this, bountyManager);
        recipesGUI = new com.example.killstreak.gui.RecipesGUI(this);

        getServer().getPluginManager().registerEvents(new com.example.killstreak.listeners.PlayerKillListener(this, ksManager, bountyManager), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        dropItemListener = new DropItemListener(this, ksManager, cfg);
        getServer().getPluginManager().registerEvents(dropItemListener, this);

        KSCommand cmdExec = new KSCommand(this, ksManager, bountyManager, cfg, abilitiesManager, configGUI, bountiesGUI, recipesGUI, eventManager);
        getCommand("killstreak").setExecutor(cmdExec);
        getCommand("killstreak").setTabCompleter(cmdExec);
        getCommand("ks").setExecutor(cmdExec);
        getCommand("ks").setTabCompleter(cmdExec);
        // register custom recipes
        registerRecipes();
        // register custom item behaviors
        new com.example.killstreak.listeners.CustomItemListener(this);

        eventManager.startScheduling();
        getLogger().info("KillStreaks enabled");
        // ensure scoreboard refreshed
        bountyManager.getBounties();
        // load persisted data
        // load bounties
        try {
            var b = dataManager.loadBounties();
            for (var e : b.entrySet()) bountyManager.setBounty(e.getKey(), e.getValue());
            var abs = dataManager.loadAbilities();
            for (var pentry : abs.entrySet()) {
                java.util.UUID id = java.util.UUID.fromString(pentry.getKey());
                for (var lvl : pentry.getValue().entrySet()) abilitiesManager.setChoice(id, lvl.getKey(), lvl.getValue());
            }
        } catch (Exception ex) { getLogger().info("No persisted data or failed to load: " + ex.getMessage()); }
    }

    @Override
    public void onDisable() {
        // persist bounties and abilities
        try {
            dataManager.saveAll(bountyManager.getBounties(), abilitiesManager == null ? java.util.Map.of() : abilitiesManager.getAllChoices());
        } catch (Exception ex) { getLogger().warning("Failed to save data: " + ex.getMessage()); }
        getLogger().info("KillStreaks disabled");
    }
    public static KSPlugin get() { return instance; }
    public KillstreakManager getKsManager() { return ksManager; }
    public BountyManager getBountyManager() { return bountyManager; }
    public EventManager getEventManager() { return eventManager; }
    public ConfigManager getCfg() { return cfg; }
    
    public AbilitiesManager getAbilitiesManager() { return abilitiesManager; }
    public com.example.killstreak.gui.AbilityPickGUI getAbilityPickGUI() { return abilityPickGUI; }
    public DataManager getDataManager() { return dataManager; }
    public DropItemListener getDropItemListener() { return dropItemListener; }
    public BloodEssenceManager getBloodEssenceManager() { return bloodEssenceManager; }

    private void registerRecipes() {
        // Item references for craft limiting
        org.bukkit.inventory.ItemStack bountyBlade = com.example.killstreak.CustomItems.createBountyBlade(this);
        org.bukkit.inventory.ItemStack mace = com.example.killstreak.CustomItems.createMace(this);
        org.bukkit.inventory.ItemStack phoenixFeather = com.example.killstreak.CustomItems.createPhoenixFeather(this);
        org.bukkit.inventory.ItemStack meteorStaff = com.example.killstreak.CustomItems.createMeteorStaff(this);
        org.bukkit.inventory.ItemStack voidCleaver = com.example.killstreak.CustomItems.createVoidCleaver(this);
        org.bukkit.inventory.ItemStack celestialCloak = com.example.killstreak.CustomItems.createCelestialCloak(this);
        org.bukkit.inventory.ItemStack dashCrystal = com.example.killstreak.CustomItems.createDashCrystal(this);

        // Bounty Blade recipe
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(this, "bounty_blade");
        org.bukkit.inventory.ShapedRecipe r = new org.bukkit.inventory.ShapedRecipe(key, bountyBlade);
        r.shape(" N ", "NSN", " B ");
        r.setIngredient('N', org.bukkit.Material.NETHERITE_INGOT);
        r.setIngredient('S', org.bukkit.Material.STICK);
        r.setIngredient('B', org.bukkit.Material.BLAZE_POWDER);
        try { getServer().addRecipe(r); } catch (Exception ignored) {}

        // Mace recipe
        org.bukkit.NamespacedKey key2 = new org.bukkit.NamespacedKey(this, "mace");
        org.bukkit.inventory.ShapedRecipe r2 = new org.bukkit.inventory.ShapedRecipe(key2, mace);
        r2.shape(" I ", "ISI", " S ");
        r2.setIngredient('I', org.bukkit.Material.IRON_INGOT);
        r2.setIngredient('S', org.bukkit.Material.STICK);
        try { getServer().addRecipe(r2); } catch (Exception ignored) {}

        // Phoenix Feather (revive on death)
        org.bukkit.NamespacedKey key3 = new org.bukkit.NamespacedKey(this, "phoenix_feather");
        org.bukkit.inventory.ShapedRecipe r3 = new org.bukkit.inventory.ShapedRecipe(key3, phoenixFeather);
        r3.shape(" F ", "GSG", " F ");
        r3.setIngredient('F', org.bukkit.Material.FEATHER);
        r3.setIngredient('G', org.bukkit.Material.GOLD_INGOT);
        r3.setIngredient('S', org.bukkit.Material.NETHER_STAR);
        try { getServer().addRecipe(r3); } catch (Exception ignored) {}

        // Meteor Staff (summon meteors)
        org.bukkit.NamespacedKey key5 = new org.bukkit.NamespacedKey(this, "meteor_staff");
        org.bukkit.inventory.ShapedRecipe r5 = new org.bukkit.inventory.ShapedRecipe(key5, meteorStaff);
        r5.shape(" F ", "BRB", " N ");
        r5.setIngredient('F', org.bukkit.Material.FIRE_CHARGE);
        r5.setIngredient('B', org.bukkit.Material.BLAZE_POWDER);
        r5.setIngredient('R', org.bukkit.Material.BLAZE_ROD);
        r5.setIngredient('N', org.bukkit.Material.NETHER_STAR);
        try { getServer().addRecipe(r5); } catch (Exception ignored) {}

        // Void Cleaver
        org.bukkit.NamespacedKey key6 = new org.bukkit.NamespacedKey(this, "void_cleaver");
        org.bukkit.inventory.ShapedRecipe r6 = new org.bukkit.inventory.ShapedRecipe(key6, voidCleaver);
        r6.shape("NN ", "NSN", " S ");
        r6.setIngredient('N', org.bukkit.Material.NETHERITE_INGOT);
        r6.setIngredient('S', org.bukkit.Material.STICK);
        try { getServer().addRecipe(r6); } catch (Exception ignored) {}

        // Celestial Cloak
        org.bukkit.NamespacedKey key7 = new org.bukkit.NamespacedKey(this, "celestial_cloak");
        org.bukkit.inventory.ShapedRecipe r7 = new org.bukkit.inventory.ShapedRecipe(key7, celestialCloak);
        r7.shape("DDD", "DLD", "GGG");
        r7.setIngredient('D', org.bukkit.Material.DIAMOND);
        r7.setIngredient('L', org.bukkit.Material.NETHER_STAR);
        r7.setIngredient('G', org.bukkit.Material.GOLD_BLOCK);
        try { getServer().addRecipe(r7); } catch (Exception ignored) {}

        // Dash Crystal (limited crafts)
        org.bukkit.NamespacedKey key8 = new org.bukkit.NamespacedKey(this, "dash_crystal");
        org.bukkit.inventory.ShapedRecipe r8 = new org.bukkit.inventory.ShapedRecipe(key8, dashCrystal);
        r8.shape(" A ", "ASA", " A ");
        r8.setIngredient('A', org.bukkit.Material.AMETHYST_SHARD);
        r8.setIngredient('S', org.bukkit.Material.NETHER_STAR);
        try {
            getServer().addRecipe(r8);
        } catch (Exception ignored) {}

        // Register craft limit logic
        getServer().getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onCraft(org.bukkit.event.inventory.CraftItemEvent e) {
                org.bukkit.inventory.ItemStack result = e.getRecipe().getResult();
                String itemType = "";
                if (result.isSimilar(dashCrystal)) itemType = "dashcrystal";
                else if (result.isSimilar(bountyBlade)) itemType = "bountyblade";
                else if (result.isSimilar(mace)) itemType = "mace";
                else if (result.isSimilar(phoenixFeather)) itemType = "phoenixfeather";
                else if (result.isSimilar(meteorStaff)) itemType = "meteorstaff";
                else if (result.isSimilar(voidCleaver)) itemType = "voidcleaver";
                else if (result.isSimilar(celestialCloak)) itemType = "celestialcloak";
                if (!itemType.isEmpty()) {
                    if (!dropItemListener.canCraft(itemType)) {
                        e.setCancelled(true);
                        e.getWhoClicked().sendMessage("\u00A7cNo more " + itemType + "s can be crafted!");
                    } else {
                        dropItemListener.decrementCraft(itemType);
                    }
                }
            }
        }, this);
    }
}
