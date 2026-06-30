package ru.vladimir.itemmanager.command.list;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.Unmodifiable;
import ru.vladimir.itemmanager.ItemManager;
import ru.vladimir.itemmanager.command.SubCommand;
import ru.vladimir.itemmanager.config.MessageConfig;
import ru.vladimir.itemmanager.utils.Messenger;

public final class ReloadPlugin implements SubCommand {

    private static final Set<String> ALIASES = Set.of("reload");
    private static final Permission PERMISSION = new Permission("itemmanager.command.reload");
    private final MessageConfig messages;

    public ReloadPlugin(@NotNull MessageConfig messages) {
        this.messages = messages;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length != 1) {
            Messenger.sendMessage(sender, messages.invalidArguments(), Map.of("USAGE", "/itemmanager reload"));
            return;
        }

        ItemManager.getApi().reloadPlugin();

        Messenger.sendMessage(sender, messages.pluginReloaded());
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return List.of();
    }

    public static @NotNull @Unmodifiable Set<String> getAliases() {
        return ALIASES;
    }

    public static @NotNull Permission getPermission() {
        return PERMISSION;
    }
}
