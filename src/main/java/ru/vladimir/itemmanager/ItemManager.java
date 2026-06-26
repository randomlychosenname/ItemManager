package ru.vladimir.itemmanager;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import ru.vladimir.itemmanager.api.ItemManagerApi;
import ru.vladimir.itemmanager.command.ItemManagerCommand;

public final class ItemManager extends JavaPlugin {
    
    private static ItemManager instance;
    private static ItemManagerApi api;
    
    @Override
    public void onEnable() {
        instance = this;
        api = new ItemManagerApi(this);

        final PluginCommand command = this.getCommand("itemmanager");
        if (command == null) throw new IllegalStateException("Command 'itemmanager' not found in plugin.yml");
        
        final ItemManagerCommand commandHandler = new ItemManagerCommand();
        command.setExecutor(commandHandler);
        command.setTabCompleter(commandHandler);
    }

    public void onReload() {

    }

    @Override
    public void onDisable() {
        
    }

    public static @NotNull ItemManager getInstance() {
        if (instance == null) 
            throw new IllegalStateException("Attempted to get instance before it was initialized.");
        return instance;
    }

    public static @NotNull ItemManagerApi getApi() {
        if (api == null) 
            throw new IllegalStateException("Attempted to get API before it was available.");
        return api;
    }
}