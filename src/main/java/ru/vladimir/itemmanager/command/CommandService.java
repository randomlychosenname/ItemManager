package ru.vladimir.itemmanager.command;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import ru.vladimir.itemmanager.command.list.AddItem;
import ru.vladimir.itemmanager.command.list.GiveItem;
import ru.vladimir.itemmanager.command.list.ListItems;
import ru.vladimir.itemmanager.command.list.PluginHelp;
import ru.vladimir.itemmanager.command.list.ReloadPlugin;
import ru.vladimir.itemmanager.command.list.RemoveItem;
import ru.vladimir.itemmanager.config.MessageConfig;
import ru.vladimir.itemmanager.utils.Logger;

public final class CommandService {
    private final Map<String, SubCommandWrapper> subCommandRegistry;

    public CommandService(@NotNull MessageConfig messages) {
        Logger.getInstance().debug(this, "Initializing...");

        this.subCommandRegistry = new ConcurrentHashMap<>();

        registerSubCommands(messages);

        Logger.getInstance().debug(this, "Initialized successfully.");
    }

    private void registerSubCommands(MessageConfig messages) {
        final var addWrapper = new SubCommandWrapper(new AddItem(messages), AddItem.getAliases(), AddItem.getPermission());
        registerSubCommand(addWrapper.aliases(), addWrapper);

        final var removeWrapper = new SubCommandWrapper(new RemoveItem(messages), RemoveItem.getAliases(), RemoveItem.getPermission());
        registerSubCommand(removeWrapper.aliases(), removeWrapper);

        final var giveWrapper = new SubCommandWrapper(new GiveItem(messages), GiveItem.getAliases(), GiveItem.getPermission());
        registerSubCommand(giveWrapper.aliases(), giveWrapper);

        final var listWrapper = new SubCommandWrapper(new ListItems(messages), ListItems.getAliases(), ListItems.getPermission());
        registerSubCommand(listWrapper.aliases(), listWrapper);

        final var reloadWrapper = new SubCommandWrapper(new ReloadPlugin(messages), ReloadPlugin.getAliases(), ReloadPlugin.getPermission());
        registerSubCommand(reloadWrapper.aliases(), reloadWrapper);

        final var helpWrapper = new SubCommandWrapper(new PluginHelp(messages), PluginHelp.getAliases(), PluginHelp.getPermission());
        registerSubCommand(helpWrapper.aliases(), helpWrapper);
    }

    private void registerSubCommand(Set<String> aliases, SubCommandWrapper wrapper) {
        for (final String alias : aliases) {
            if (subCommandRegistry.containsKey(alias)) {
                Logger.getInstance().warn(this, "Failed to add alias '%s': Already registered.".formatted(alias));
                continue;
            }

            subCommandRegistry.put(alias, wrapper);
        }
    }

    Optional<SubCommandWrapper> getWrapperForAlias(String alias) {
        return Optional.ofNullable(subCommandRegistry.get(alias));
    }

    Set<String> getAliasesFor(CommandSender sender) {
        final Set<String> aliases = new HashSet<>();

        for (final var entry : subCommandRegistry.entrySet()) {
            if (!sender.hasPermission(entry.getValue().permission())) continue;

            aliases.add(entry.getKey());
        }

        return aliases;
    }
}
