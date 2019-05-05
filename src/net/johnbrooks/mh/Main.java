package net.johnbrooks.mh;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.johnbrooks.mh.commands.CommandMobCapture;
import net.johnbrooks.mh.events.EventManager;
import net.johnbrooks.mh.managers.PermissionManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Main extends JavaPlugin {
    public static Main plugin;
    public static Logger logger;

    public static EventManager eventManager = null;
    public static PermissionManager permissionManager = null;
    public static Economy economy = null;

    public static GriefPrevention griefPrevention;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        getCommand("MobCapture").setExecutor(new CommandMobCapture());
        logger = getLogger();
        permissionManager = new PermissionManager();
        Settings.load();
        eventManager = new EventManager();
        eventManager.initialize();
        getLogger().info(getDescription().getName() + " is now enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info(getDescription().getName() + " is now disabled!");
    }
}
