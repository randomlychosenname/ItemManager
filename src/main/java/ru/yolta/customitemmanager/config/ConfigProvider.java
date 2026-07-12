package ru.yolta.customitemmanager.config;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import ru.yolta.customitemmanager.CustomItemManager;
import ru.yolta.customitemmanager.utils.Logger;

public final class ConfigProvider {

    private static final String MESSAGE_CONFIG_FILE_NAME = "messages.yml";
    private final MessageConfig messageConfig;

    public ConfigProvider(@NotNull CustomItemManager plugin) {
        Logger.debug(this, "Initializing...");

        final File messageConfigFile = new File(plugin.getDataFolder(), MESSAGE_CONFIG_FILE_NAME);
        ensureFileExists(plugin, messageConfigFile, false);
        this.messageConfig = MessageConfig.parseMessageConfig(this, messageConfigFile, getFileConfig(messageConfigFile));

        Guides.overwriteGuides(plugin, this);

        Logger.debug(this, "Initialized successfully.");
    }

    void saveConfig(@NotNull File file, @NotNull FileConfiguration fileConfig) {
        try {
            fileConfig.save(file);
        } catch (IOException e) {
            Logger.error(this, "Failed to save config.", e);
        }
    }

    void ensureFileExists(@NotNull CustomItemManager plugin, @NotNull File file, boolean shouldReplace) {
        ensureFileExists(plugin, file, file.getName(), shouldReplace);
    }

    private void ensureFileExists(@NotNull CustomItemManager plugin, @NotNull File file, @NotNull String path, boolean shouldReplace) {
        if (!file.exists()) {
            Logger.warn(this, "File '{}' not found. Creating it now.", file.getName());
            plugin.saveResource(file.getName(), shouldReplace);
        }
    }

    private FileConfiguration getFileConfig(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }

    public @NotNull MessageConfig getMessageConfig() {
        return messageConfig;
    }
}
