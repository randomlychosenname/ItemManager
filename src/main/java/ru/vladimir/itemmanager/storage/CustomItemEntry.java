package ru.vladimir.itemmanager.storage;

import java.util.*;

record CustomItemEntry(
        String materialName,
        String displayName,
        List<String> lore,
        int customModelDataId,
        List<EnchantmentEntry> enchantmentEntries,
        List<AttributeEntry> attributeEntries,
        Set<String> keys
) {
    CustomItemEntry(
            String materialName,
            String displayName,
            List<String> lore,
            int customModelDataId,
            List<EnchantmentEntry> enchantmentEntries,
            List<AttributeEntry> attributeEntries,
            Set<String> keys
    ) {
        this.materialName = materialName.strip().toUpperCase(Locale.ROOT).replaceAll("\\s", "_");
        this.displayName = displayName;
        this.lore = lore;
        this.customModelDataId = customModelDataId;
        this.enchantmentEntries = enchantmentEntries;
        this.attributeEntries = attributeEntries;
        this.keys = keys;
    }

    List<Map<String, ?>> enchantmentEntriesToMap() {
        final List<Map<String, ?>> enchantments = new ArrayList<>();

        for (final EnchantmentEntry entry : enchantmentEntries) {
            enchantments.add(entry.toMap());
        }

        return List.copyOf(enchantments);
    }

    List<Map<String, ?>> attributeEntriesToMap() {
        final List<Map<String, ?>> attributes = new ArrayList<>();

        for (final AttributeEntry entry : attributeEntries) {
            attributes.add(entry.toMap());
        }

        return List.copyOf(attributes);
    }

    Map<String, ?> toMap() {
        return Map.of(
                "material", materialName,
                "display-name", displayName,
                "lore", lore,
                "model-id", customModelDataId,
                "enchantments", enchantmentEntriesToMap(),
                "attributes", attributeEntriesToMap(),
                "keys", keys
        );
    }

    record EnchantmentEntry(String name, int level) {
        EnchantmentEntry(String name, int level) {
            this.name = name.strip().toLowerCase(Locale.ROOT).replaceAll("\\s", "_");
            this.level = Math.clamp(level, 0, 255);
        }

        Map<String, ?> toMap() {
            return Map.of(
                    "name", name,
                    "level", level
            );
        }
    }

    record AttributeEntry(String name, List<AttributeModifierEntry> modifierEntries) {
        AttributeEntry(String name, List<AttributeModifierEntry> modifierEntries) {
            this.name = name.strip().toLowerCase(Locale.ROOT).replaceAll("\\s", "_");
            this.modifierEntries = modifierEntries;
        }

        Map<String, ?> toMap() {
            final List<Map<String, ?>> modifiers = new ArrayList<>();

            for (final AttributeModifierEntry entry : modifierEntries) {
                modifiers.add(entry.toMap());
            }

            return Map.of(
                    "name", name,
                    "modifiers", modifiers
            );
        }
    }

    record AttributeModifierEntry(String operationName, double amount, String slotName) {
        AttributeModifierEntry(String operationName, double amount, String slotName) {
            this.operationName = operationName.strip().toUpperCase(Locale.ROOT).replaceAll("\\s", "");
            this.amount = Math.clamp(amount, -1024, 60000000);
            this.slotName = slotName.strip().toUpperCase(Locale.ROOT).replaceAll("\\s", "");
        }

        Map<String, ?> toMap() {
            return Map.of(
                    "operation", operationName,
                    "amount", amount,
                    "slot", slotName
            );
        }
    }
}
