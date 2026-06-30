package ru.vladimir.itemmanager.command.list;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.Unmodifiable;
import ru.vladimir.itemmanager.ItemManager;
import ru.vladimir.itemmanager.command.SubCommand;
import ru.vladimir.itemmanager.config.MessageConfig;
import ru.vladimir.itemmanager.utils.Messenger;

public final class GiveItem implements SubCommand {

    private static final Set<String> ALIASES = Set.of("give");
    private static final Permission PERMISSION = new Permission("itemmanager.command.give");
    private final MessageConfig messages;

    public GiveItem(@NotNull MessageConfig messages) {
        this.messages = messages;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length < 3 || args.length > 4) {
            Messenger.sendMessage(sender, messages.invalidArguments(), Map.of("USAGE", "/itemmanager give <player> <name> [amount]"));
            return;
        }

        final String targetPlayerName = args[1];
        final Player targetPlayer = Bukkit.getPlayer(targetPlayerName);

        if (targetPlayer == null) {
            Messenger.sendMessage(sender, messages.playerNotFound(), Map.of("PLAYER", targetPlayerName));
            return;
        }

        final String itemName = args[2];
        final Optional<ItemStack> optionalItem = ItemManager.getApi().getCustomItem(itemName);

        if (optionalItem.isEmpty()) {
            Messenger.sendMessage(sender, messages.itemNotFound(), Map.of("ITEM", itemName));
            return;
        }

        final ItemStack item = optionalItem.get();

        int itemAmount = 1;

        if (args.length == 4) {
            try {
                itemAmount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                Messenger.sendMessage(sender, messages.invalidAmount(), Map.of("AMOUNT", args[3]));
                return;
            }
        }

        item.setAmount(itemAmount);

        final var itemsNotFitted = targetPlayer.getInventory().addItem(item);

        for (final var entry : itemsNotFitted.entrySet()) {
            targetPlayer.getWorld().dropItemNaturally(targetPlayer.getLocation(), entry.getValue());
        }

        Messenger.sendMessage(sender, messages.itemGiven(), Map.of("PLAYER", targetPlayerName, "ITEM", itemName, "AMOUNT", String.valueOf(itemAmount)));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length == 2)
            return null; // Let Bukkit handle player name suggestions

        if (args.length == 3)
            return List.copyOf(ItemManager.getApi().getAllCustomItemIds());

        return List.of();
    }

    public static @NotNull @Unmodifiable Set<String> getAliases() {
        return ALIASES;
    }

    public static @NotNull Permission getPermission() {
        return PERMISSION;
    }
}
