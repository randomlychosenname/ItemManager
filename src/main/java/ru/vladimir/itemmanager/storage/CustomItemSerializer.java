package ru.vladimir.itemmanager.storage;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

final class CustomItemSerializer {
    private static final MiniMessage MINI_MESSAGE_PARSER = MiniMessage.miniMessage();

    private CustomItemSerializer() {}

    static void serializeItemAndWriteToSection(ItemStack item, ConfigurationSection section) {
        final CustomItemEntry entry = serializeItem(item);

        section.set("material", entry.materialName());
        section.set("display-name", entry.displayName());
        section.set("lore", entry.lore());
        section.set("enchantments", entry.enchantmentEntriesToMap());
        section.set("attributes", entry.attributeEntriesToMap());
        section.set("keys", List.copyOf(entry.keys()));
    }

    static CustomItemEntry serializeItem(ItemStack item) {
        final Material material = item.getType();
        final String materialName = material.toString();

        final Component displayName = item.displayName();
        final String rawDisplayName = serializeDisplayName(displayName, material, !item.getEnchantments().isEmpty());

        final List<Component> lore = Objects.requireNonNullElse(item.lore(), List.of());
        final List<String> rawLore = new ArrayList<>(lore.size());

        for (final Component line : lore) {
            rawLore.add(MINI_MESSAGE_PARSER.serialize(line));
        }

        final Map<Enchantment, Integer> enchantments = item.getEnchantments();
        final List<CustomItemEntry.EnchantmentEntry> rawEnchantments = new ArrayList<>(enchantments.size());

        for (final Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            rawEnchantments.add(new CustomItemEntry.EnchantmentEntry(entry.getKey().getKey().getKey(), entry.getValue()));
        }

        final ItemMeta meta = item.getItemMeta();
        if (meta == null) return new CustomItemEntry(materialName, rawDisplayName, rawLore, rawEnchantments, List.of(), Set.of());

        final Multimap<Attribute, AttributeModifier> attributes = Objects.requireNonNullElse(meta.getAttributeModifiers(), ArrayListMultimap.create());
        final List<CustomItemEntry.AttributeEntry> rawAttributes = new ArrayList<>(attributes.keys().size());

        for (final Map.Entry<Attribute, Collection<AttributeModifier>> entry : attributes.asMap().entrySet()) {

            final Collection<AttributeModifier> attributeModifiers = entry.getValue();
            final List<CustomItemEntry.AttributeModifierEntry> rawAttributeModifiers = new ArrayList<>(attributeModifiers.size());

            for (final AttributeModifier modifier : attributeModifiers) {
                rawAttributeModifiers.add(new CustomItemEntry.AttributeModifierEntry(
                        modifier.getOperation().toString(),
                        modifier.getAmount(),
                        modifier.getSlotGroup().toString()
                ));
            }

            rawAttributes.add(new CustomItemEntry.AttributeEntry(entry.getKey().getKey().getKey(), rawAttributeModifiers));
        }

        final Set<NamespacedKey> keys = item.getPersistentDataContainer().getKeys();
        final Set<String> rawKeys = new HashSet<>();

        for (final NamespacedKey key : keys) {
            rawKeys.add(key.toString());
        }

        return new CustomItemEntry(
                materialName,
                rawDisplayName,
                rawLore,
                rawEnchantments,
                rawAttributes,
                rawKeys
        );
    }

    private static String serializeDisplayName(Component displayName, Material material, boolean hasEnchantments) {
        displayName = displayName.clickEvent(null).hoverEvent(null).insertion(null);

        return MINI_MESSAGE_PARSER.serialize(displayName);
//
//        if (isTextComponentEmpty(displayName)) {
//            String materialName = material.toString().toLowerCase(Locale.ROOT).replace("_", " ");
//
//            final String[] splitMaterialName = materialName.split(" ");
//            final StringBuilder finalName = new StringBuilder();
//
//            for (final String partMaterialName : splitMaterialName) {
//                finalName.append(partMaterialName.toUpperCase(Locale.ROOT).charAt(0))
//                         .append(partMaterialName.substring(1))
//                         .append(' ');
//            }
//
//            final String enchantmentColorPrefix = hasEnchantments ? "<aqua>" : "";
//
//            return "<!italic>" + enchantmentColorPrefix + finalName.toString().strip();
//        }
//
//        Logger.getInstance().info(CustomItemSerializer.class, "%s is not treated as default.".formatted(MINI_MESSAGE_PARSER.serialize(displayName)));
//
//        if (!(displayName instanceof final TranslatableComponent tc)) {
//            Logger.getInstance().warn(CustomItemSerializer.class, "Irregular case of display name serialization: %s".formatted(displayName));
//            return MINI_MESSAGE_PARSER.serialize(displayName);
//        }
//
//
//
//        return MINI_MESSAGE_PARSER.serialize(displayName);
    }

    private static boolean isTextComponentEmpty(Component component) {
        if (component instanceof TextComponent tc && !tc.content().isEmpty()) return false;

        for (final Component c : component.children()) {
            if (c instanceof final TextComponent tc && !tc.content().isEmpty()) return false;
            if (!c.children().isEmpty() && !isTextComponentEmpty(c)) return false;
        }

        return true;
    }
}
