package ru.vladimir.itemmanager.storage;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import ru.vladimir.itemmanager.utils.Logger;

import java.util.*;

final class CustomItemDeserializer {

    private static final String LOG_NAME = "Custom Item Deserializer";
    private static final MiniMessage MINI_MESSAGE_PARSER = MiniMessage.miniMessage();

    private CustomItemDeserializer() {}

    static byte[] deserializeItem(String pluginName, String itemId, ConfigurationSection itemEntry) {

        final Material material = getMaterial(itemId, itemEntry.getString("material"));
        if (material == null) return null;

        final Component displayName = getDisplayName(itemId, itemEntry.getString("display-name"));
        if (displayName == null) return null;

        final List<Component> lore = getLore(itemId, itemEntry.getList("lore"));
        if (lore == null) return null;

        final Map<Enchantment, Integer> enchantments = getEnchantments(itemId, itemEntry.getList("enchantments"));
        if (enchantments == null) return null;

        final Map<Attribute, List<AttributeModifier>> attributes = getAttributes(itemId, itemEntry.getList("attributes"));
        if (attributes == null) return null;

        final Set<NamespacedKey> keys = getKeys(pluginName, itemId, itemEntry.getList("keys"));
        if (keys == null) return null;

        final ItemStack item = ItemStack.of(material);
        final ItemMeta meta = Bukkit.getItemFactory().getItemMeta(material);

        meta.displayName(displayName);
        meta.lore(lore);

        for (final Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }

        for (final Map.Entry<Attribute, List<AttributeModifier>> entry : attributes.entrySet()) {
            for (final AttributeModifier element : entry.getValue()) {
                meta.addAttributeModifier(entry.getKey(), element);
            }
        }

        final PersistentDataContainer container = meta.getPersistentDataContainer();
        for (final NamespacedKey key : keys) {
            container.set(key, PersistentDataType.BOOLEAN, true);
        }

        item.setItemMeta(meta);
        return item.serializeAsBytes();
    }

    private static Material getMaterial(String itemId, String materialName) {
        if (materialName == null) {
            Logger.getInstance().warn(LOG_NAME,
                    "Item '%s' missing material".formatted(itemId));
            return null;
        }

        final Material material = Material.matchMaterial(materialName);

        if (material == null) {
            Logger.getInstance().warn(LOG_NAME,
                    "Item '%s' invalid material '%s'".formatted(itemId, materialName));
            return null;
        }

        return material;
    }

    private static Component getDisplayName(String itemId, String rawDisplayName) {
        if (rawDisplayName == null) {
            Logger.getInstance().warn(LOG_NAME,
                    "Item '%s' missing display-name".formatted(itemId));
            return null;
        }

        return MINI_MESSAGE_PARSER.deserialize(rawDisplayName);
    }

    private static List<Component> getLore(String itemId, List<?> rawLore) {
        if (rawLore == null) {
            Logger.getInstance().warn(LOG_NAME,
                    "Item '%s' missing lore".formatted(itemId));
            return null;
        }

        final List<Component> lore = new ArrayList<>();

        for (final Object rawLine : rawLore) {
            lore.add(MINI_MESSAGE_PARSER.deserialize(String.valueOf(rawLine)));
        }

        return lore;
    }

    private static Map<Enchantment, Integer> getEnchantments(String itemId, List<?> rawEnchantments) {

        if (rawEnchantments == null) {
            Logger.getInstance().warn(LOG_NAME,
                    "Item '%s' missing enchantments".formatted(itemId));
            return null;
        }

        final Map<Enchantment, Integer> enchantments = new HashMap<>();
        final Set<String> addedEnchantmentKeys = new HashSet<>();

        for (final Object obj : rawEnchantments) {

            if (!(obj instanceof final Map<?, ?> map)) {
                Logger.getInstance().warn(LOG_NAME,
                        "Item '%s' invalid enchantment entry: %s".formatted(itemId, obj));
                continue;
            }

            if (!map.containsKey("name") || !map.containsKey("level")) {
                Logger.getInstance().warn(LOG_NAME,
                        "Item '%s' enchantment missing fields: %s".formatted(itemId, map));
                continue;
            }

            final String rawKey = String.valueOf(map.get("name"))
                    .strip()
                    .replace(" ", "_")
                    .toLowerCase(Locale.ROOT);

            if (!addedEnchantmentKeys.add(rawKey)) {
                Logger.getInstance().warn(LOG_NAME,
                        "Item '%s' duplicate enchantment '%s'".formatted(itemId, rawKey));
                continue;
            }

            final String[] rawKeyParts = rawKey.split(":");

            if (rawKeyParts.length < 1 || rawKeyParts.length > 2) {
                Logger.getInstance().warn(LOG_NAME,
                        "Item '%s' invalid enchantment key format '%s'".formatted(itemId, rawKey));
                continue;
            }

            final NamespacedKey enchantmentKey = rawKeyParts.length == 1
                    ? new NamespacedKey("minecraft", rawKeyParts[0])
                    : new NamespacedKey(rawKeyParts[0], rawKeyParts[1]);

            final Enchantment enchantment = RegistryAccess.registryAccess()
                    .getRegistry(RegistryKey.ENCHANTMENT)
                    .get(enchantmentKey);

            if (enchantment == null) {
                Logger.getInstance().warn(LOG_NAME,
                        "Item '%s' unknown enchantment '%s'".formatted(itemId, rawKey));
                continue;
            }

            final String rawLevel = String.valueOf(map.get("level"))
                    .strip()
                    .replace(" ", "");

            final int level;

            try {
                level = Integer.parseInt(rawLevel);
            } catch (NumberFormatException e) {
                Logger.getInstance().warn(LOG_NAME,
                        "Item '%s' invalid enchantment level '%s' for '%s'".formatted(itemId, map.get("level"), rawKey));
                continue;
            }

            if (level < 0 || level > 255) {
                Logger.getInstance().warn(LOG_NAME,
                        "Item '%s' level '%d' of '%s' is outside Minecraft's supported range (0-255). It will be clamped."
                                .formatted(itemId, level, rawKey));
            }

            enchantments.put(enchantment, Math.clamp(level, 0, 255));
        }

        return enchantments;
    }

    private static Map<Attribute, List<AttributeModifier>> getAttributes(String itemId, List<?> rawAttributes) {

        if (rawAttributes == null) {
            Logger.getInstance().warn(LOG_NAME,
                    "Item '%s' missing attributes".formatted(itemId));
            return null;
        }

        final Map<Attribute, List<AttributeModifier>> attributes = new HashMap<>();
        final Set<String> addedAttributeKeys = new HashSet<>();

        for (final Object obj : rawAttributes) {

            if (!(obj instanceof final Map<?, ?> map)) {
                Logger.getInstance().warn(LOG_NAME,
                        "Item '%s' invalid attribute entry: %s".formatted(itemId, obj));
                continue;
            }

            if (!map.containsKey("name") || !map.containsKey("modifiers")) {
                Logger.getInstance().warn(LOG_NAME,
                        "Item '%s' attribute missing fields: %s".formatted(itemId, map));
                continue;
            }

            final String rawKey = String.valueOf(map.get("name"))
                    .strip()
                    .replace(" ", "_")
                    .toLowerCase(Locale.ROOT);

            if (!addedAttributeKeys.add(rawKey)) {
                Logger.getInstance().warn(LOG_NAME,
                        "Item '%s' duplicate attribute '%s'".formatted(itemId, rawKey));
                continue;
            }

            final String[] rawKeyParts = rawKey.split(":");

            if (rawKeyParts.length < 1 || rawKeyParts.length > 2) {
                Logger.getInstance().warn(LOG_NAME,
                        "Item '%s' invalid attribute key '%s'".formatted(itemId, rawKey));
                continue;
            }

            final NamespacedKey attributeKey = rawKeyParts.length == 1
                    ? new NamespacedKey("minecraft", rawKeyParts[0])
                    : new NamespacedKey(rawKeyParts[0], rawKeyParts[1]);

            final Attribute attribute = RegistryAccess.registryAccess()
                    .getRegistry(RegistryKey.ATTRIBUTE)
                    .get(attributeKey);

            if (attribute == null) {
                Logger.getInstance().warn(LOG_NAME,
                        "Item '%s' unknown attribute '%s'".formatted(itemId, rawKey));
                continue;
            }

            final Object supposedRawModifiers = map.get("modifiers");

            if (!(supposedRawModifiers instanceof final List<?> rawModifiers)) {
                Logger.getInstance().warn(LOG_NAME,
                        "Item '%s' invalid modifiers list %s".formatted(itemId, supposedRawModifiers));
                continue;
            }

            final List<AttributeModifier> modifiers = new ArrayList<>();

            for (final Object mObj : rawModifiers) {

                if (!(mObj instanceof final Map<?, ?> rawModifier)) {
                    Logger.getInstance().warn(LOG_NAME,
                            "Item '%s' invalid modifier: %s".formatted(itemId, mObj));
                    continue;
                }

                if (!rawModifier.containsKey("operation") || !rawModifier.containsKey("amount") || !rawModifier.containsKey("slot")) {
                    Logger.getInstance().warn(LOG_NAME,
                            "Item '%s' modifier missing fields: %s".formatted(itemId, rawModifier));
                    continue;
                }

                final String operationName = String.valueOf(rawModifier.get("operation"))
                        .toUpperCase(Locale.ROOT)
                        .replace(" ", "");

                final AttributeModifier.Operation operation;

                try {
                    operation = AttributeModifier.Operation.valueOf(operationName);
                } catch (IllegalArgumentException e) {
                    Logger.getInstance().warn(LOG_NAME,
                            "Item '%s' invalid operation: %s".formatted(itemId, operationName));
                    continue;
                }

                final String rawAmount = String.valueOf(rawModifier.get("amount"))
                        .replace(" ", "");

                final double amount;

                try {
                    amount = Double.parseDouble(rawAmount);
                } catch (NumberFormatException e) {
                    Logger.getInstance().warn(LOG_NAME,
                            "Item '%s' invalid amount: %s".formatted(itemId, rawModifier.get("amount")));
                    continue;
                }

                final String slotName = String.valueOf(rawModifier.get("slot"))
                        .replace(" ", "")
                        .toLowerCase(Locale.ROOT);

                final EquipmentSlotGroup slotGroup = EquipmentSlotGroup.getByName(slotName);

                if (slotGroup == null) {
                    Logger.getInstance().warn(LOG_NAME,
                            "Item '%s' invalid slot: %s".formatted(itemId, rawModifier.get("slot")));
                    continue;
                }

                modifiers.add(new AttributeModifier(new NamespacedKey("minecraft", UUID.randomUUID().toString()),
                        amount,
                        operation,
                        slotGroup
                ));
            }

            attributes.put(attribute, modifiers);
        }

        return attributes;
    }

    private static Set<NamespacedKey> getKeys(String pluginName, String itemId, List<?> rawKeys) {
        if (rawKeys == null) {
            Logger.getInstance().warn(CustomItemDeserializer.class, "Failed to deserialize item '%s': No keys."
                    .formatted(itemId));
            return null;
        }

        final Set<NamespacedKey> keys = new HashSet<>();
        final Set<String> addedKeys = new HashSet<>();

        for (final Object rawKey : rawKeys) {

            final String rawKeyString = String.valueOf(rawKey)
                    .strip()
                    .replace(" ", "_")
                    .toLowerCase(Locale.ROOT);

            final String[] rawKeyParts = rawKeyString.split(":");

            if (rawKeyParts.length < 1 || rawKeyParts.length > 2) {
                Logger.getInstance().warn(LOG_NAME, "Failed to deserialize key '%s' of '%s': Invalid format."
                        .formatted(rawKeyString, itemId));
                continue;
            }

            if (!addedKeys.add(rawKeyString)) {
                Logger.getInstance().warn(LOG_NAME,
                        "Item '%s' duplicate key '%s'".formatted(itemId, rawKey));
                continue;
            }

            if (rawKeyParts.length == 1) {
                Logger.getInstance().warn(LOG_NAME,
                        "Item '%s' invalid key '%s'".formatted(itemId, rawKey));
                continue;
            }

            keys.add(new NamespacedKey(rawKeyParts[0], rawKeyParts[1]));
        }

        return keys;
    }
}
