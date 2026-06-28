package ru.vladimir.itemmanager.storage;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import ru.vladimir.itemmanager.ItemManager;
import ru.vladimir.itemmanager.utils.Logger;

public final class CustomItemStorage {

    private final ItemManager plugin;
    private final Map<String, byte[]> itemRegistry;

    public CustomItemStorage(@NotNull ItemManager plugin) {
        this.plugin = plugin;
        this.itemRegistry = new ConcurrentHashMap<>();

        refreshItemRegistry(getItemConfigFile(), getItemConfig());
    }

    private void refreshItemRegistry(File file, FileConfiguration itemConfig) {
        itemRegistry.clear(); 
        
        // Actual logic here.
    }

    private void appendItemToStorage(File file, FileConfiguration itemConfig, String itemId, ItemStack item) {
        refreshItemRegistry(file, itemConfig);

        // Here we append the item.

        saveItemConfig(file, itemConfig);

        refreshItemRegistry(file, itemConfig);
    }

    private void removeItemFromStorage(File file, FileConfiguration itemConfig, String itemId) {
        refreshItemRegistry(file, itemConfig);

        // Here we remove the item.

        saveItemConfig(file, itemConfig);

        refreshItemRegistry(file, itemConfig);
    }

    private File getItemConfigFile() {
        return new File(plugin.getDataFolder(), "items.yml");
    }

    private FileConfiguration getItemConfig() {
        return YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "items.yml"));
    }

    private void saveItemConfig(File file, FileConfiguration config) {
        if (!file.exists()) {
            plugin.saveResource("items.yml", false);
            Logger.debug(this, "items.yml was not found. A new version was created.");
        }

        try {
            config.save(file);
        } catch (IOException e) {
            Logger.error(this, "Failed to save file configuration.", e);
        }
    }

    public boolean registerCustomItem(@NotNull String itemId, @NotNull ItemStack item) {
        if (isCustomItem(itemId)) return false;

        appendItemToStorage(getItemConfigFile(), getItemConfig(), itemId, item);

        return true;
    }

    public boolean unregisterCustomItem(@NotNull String itemId) {
        if (!isCustomItem(itemId)) return false;

        removeItemFromStorage(getItemConfigFile(), getItemConfig(), itemId);
        
        return true;
    }

    public boolean isCustomItem(@NotNull String itemId) {
        return itemRegistry.containsKey(itemId);
    }

    @NotNull Optional<ItemStack> getCustomItem(@NotNull String itemId) {
        if (!isCustomItem(itemId)) return Optional.empty();

        return Optional.ofNullable(ItemStack.deserializeBytes(itemRegistry.get(itemId)));
    }

    public @NotNull @Unmodifiable Set<String> getCustomItemIds() {
        return Set.copyOf(itemRegistry.keySet());
    }
}
