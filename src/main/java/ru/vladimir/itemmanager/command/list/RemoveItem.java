package ru.vladimir.itemmanager.command.list;

import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ru.vladimir.itemmanager.ItemManager;
import ru.vladimir.itemmanager.command.SubCommand;
import ru.vladimir.itemmanager.config.ConfigManager;
import ru.vladimir.itemmanager.utils.Messager;

public class RemoveItem implements SubCommand {

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length != 2) {
            Messager.sendMessage(sender, ConfigManager.getInstance().getMessages().invalidArguments(), Map.of("USAGE", "/itemmanager remove <name>"));
            return;
        }

        final String itemName = args[1];

        final boolean success = ItemManager.getApi().unregisterCustomItem(itemName);

        if (success) {
            Messager.sendMessage(sender, ConfigManager.getInstance().getMessages().itemUnregistered(), Map.of("ITEM", itemName));
        } else {
            Messager.sendMessage(sender, ConfigManager.getInstance().getMessages().itemNotFound(), Map.of("ITEM", itemName));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length == 2)
            return List.copyOf(ItemManager.getApi().getAllCustomItemIds());

        return List.of();
    }
}
