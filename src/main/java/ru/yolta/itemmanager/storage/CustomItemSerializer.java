package ru.yolta.itemmanager.storage;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
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
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private CustomItemSerializer() {}

    static void serializeItemAndWriteToSection(ItemStack item, ConfigurationSection section) {
        final CustomItemEntry entry = serializeItem(item);

        section.set("material", entry.materialName());
        section.set("display-name", entry.displayName());
        section.set("lore", entry.lore());
        section.set("model-id", entry.customModelDataId());
        section.set("enchantments", entry.enchantmentEntriesToMap());
        section.set("attributes", entry.attributeEntriesToMap());
        section.set("keys", List.copyOf(entry.keys()));
    }

    static CustomItemEntry serializeItem(ItemStack item) {
        final Material material = item.getType();
        final String materialName = material.toString();

        final Component displayName = item.displayName();
        final String rawDisplayName = serializeDisplayName(displayName, materialName, !item.getEnchantments().isEmpty());

        final List<Component> lore = Objects.requireNonNullElse(item.lore(), List.of());
        final List<String> rawLore = new ArrayList<>(lore.size());

        for (final Component line : lore) {
            rawLore.add(MINI_MESSAGE.serialize(line));
        }

        final Map<Enchantment, Integer> enchantments = item.getEnchantments();
        final List<CustomItemEntry.EnchantmentEntry> rawEnchantments = new ArrayList<>(enchantments.size());

        for (final Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            rawEnchantments.add(new CustomItemEntry.EnchantmentEntry(entry.getKey().getKey().toString(), entry.getValue()));
        }

        final ItemMeta meta = item.getItemMeta();
        if (meta == null) return new CustomItemEntry(materialName, rawDisplayName, rawLore, -1, rawEnchantments, List.of(), Set.of());

        final int customDataModelId = meta.hasCustomModelData() ? meta.getCustomModelData() : -1;

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

            rawAttributes.add(new CustomItemEntry.AttributeEntry(entry.getKey().getKey().toString(), rawAttributeModifiers));
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
                customDataModelId,
                rawEnchantments,
                rawAttributes,
                rawKeys
        );
    }

    private static String serializeDisplayName(Component displayName, String materialName, boolean hasEnchantments) {
        displayName = displayName.clickEvent(null).hoverEvent(null).insertion(null);
        return MINI_MESSAGE.serialize(displayName).replaceAll("<[^>]*lang:chat\\.square_brackets[^>]*:'([^']*)'>", "$1");
    }
}
