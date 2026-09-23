package com.azpbmd.privatecreative;

import org.bukkit.plugin.java.JavaPlugin;

public class PrivateCreative extends JavaPlugin {

    // Singleton instance for easy access from other classes
    private static PrivateCreative instance;

    // Managers
    private DataManager dataManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config if not exists
        saveDefaultConfig();

        // Initialize data manager (loads data.yml)
        dataManager = new DataManager();

        // Register commands
        getCommand("home").setExecutor(new CommandExecutor());
        getCommand("invite").setExecutor(new CommandExecutor());
        getCommand("banish").setExecutor(new CommandExecutor());
        getCommand("visit").setExecutor(new CommandExecutor());
        getCommand("visitlist").setExecutor(new CommandExecutor());
        getCommand("invitelist").setExecutor(new CommandExecutor());
        getCommand("pworld").setExecutor(new CommandExecutor());

        // Register event listeners
        getServer().getPluginManager().registerEvents(new EventListener(), this);

        getLogger().info("PrivateCreative enabled!");
    }

    @Override
    public void onDisable() {
        // Save data before shutdown
        dataManager.saveData();

        // Unload all private worlds (optional, but cleans up)
        getServer().getWorlds().stream()
                .filter(world -> world.getName().endsWith("_world"))
                .forEach(world -> getServer().unloadWorld(world, true));

        getLogger().info("PrivateCreative disabled!");
    }

    public static PrivateCreative getInstance() {
        return instance;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
