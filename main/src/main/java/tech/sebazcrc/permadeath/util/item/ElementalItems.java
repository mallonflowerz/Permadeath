package tech.sebazcrc.permadeath.util.item;

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import tech.sebazcrc.permadeath.Main;
import tech.sebazcrc.permadeath.util.TextUtils;
import tech.sebazcrc.permadeath.util.lib.ElementalType;
import tech.sebazcrc.permadeath.util.lib.HiddenStringUtils;
import tech.sebazcrc.permadeath.util.lib.ItemBuilder;

public class ElementalItems {

    public static ItemStack craftElementalSpawnerIngot() {

        ItemStack s = new ItemBuilder(Material.EMERALD).setCustomModelData(1, !Main.optifineItemsEnabled())
                .setDisplayName(TextUtils.format("Elemental Spawner")).build();
        ItemMeta meta = s.getItemMeta();
        meta.setUnbreakable(true);
        meta.setLore(Arrays.asList(HiddenStringUtils.encodeString("{" + UUID.randomUUID() + ": 0}")));
        s.setItemMeta(meta);

        return s;
    }

    public static ItemStack createEggElementalByType(ElementalType type) {

        String displayName;
        int customModelData;

        if (type == ElementalType.AIR) {
            displayName = "&7Origen Elemental de Aire";
            customModelData = 1;
        } else if (type == ElementalType.EARTH) {
            displayName = "&2Origen Elemental de Tierra";
            customModelData = 2;
        } else if (type == ElementalType.ENERGY) {
            displayName = "&3Origen Elemental de Energía";
            customModelData = 3;
        } else if (type == ElementalType.FIRE) {
            displayName = "&6Origen Elemental de Fuego";
            customModelData = 4;
        } else {
            displayName = "&bOrigen Elemental de Agua";
            customModelData = 5;
        }

        ItemStack s = new ItemBuilder(Material.RED_DYE)
                .setCustomModelData(customModelData, !Main.optifineItemsEnabled())
                .setDisplayName(TextUtils.format(displayName)).build();

        ItemMeta meta = s.getItemMeta();
        meta.setUnbreakable(true);
        meta.setLore(Arrays.asList(HiddenStringUtils.encodeString("{" + UUID.randomUUID().toString() + ": 0}")));
        s.setItemMeta(meta);

        return s;
    }

    public static ElementalType extractElementalType(ItemStack egg) {
        ItemMeta meta = egg.getItemMeta();

        if (meta.isUnbreakable() && meta.hasLore() && meta.hasDisplayName()
                && meta.getDisplayName().contains("Origen Elemental de")) {
            int modelData = meta.getCustomModelData();
            if (modelData == 1) {
                return ElementalType.AIR;
            } else if (modelData == 2) {
                return ElementalType.EARTH;
            } else if (modelData == 3) {
                return ElementalType.ENERGY;
            } else if (modelData == 4) {
                return ElementalType.FIRE;
            } else if (modelData == 5) {
                return ElementalType.WATER;
            }
        }

        return null;
    }
}
