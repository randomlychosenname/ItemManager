package ru.vladimir.itemmanager.command.list;

import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ru.vladimir.itemmanager.command.SubCommand;
import ru.vladimir.itemmanager.config.ConfigManager;
import ru.vladimir.itemmanager.utils.Messager;

public class PluginHelp implements SubCommand {

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length != 1) {
            Messager.sendMessage(sender, ConfigManager.getInstance().getMessages().invalidArguments(), Map.of("USAGE", "/itemmanager help"));
            return;
        }

        Messager.sendMessage(sender, ConfigManager.getInstance().getMessages().pluginHelp());
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
