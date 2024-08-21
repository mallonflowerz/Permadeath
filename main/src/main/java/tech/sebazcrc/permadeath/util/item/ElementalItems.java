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

    public static ItemStack createFragmentElementalByType(ElementalType type) {

        String displayName;
        int customModelData;

        if (type == ElementalType.AIR) {
            displayName = "&7Fragmento Elemental de Aire";
            customModelData = 1;
        } else if (type == ElementalType.EARTH) {
            displayName = "&2Fragmento Elemental de Tierra";
            customModelData = 2;
        } else if (type == ElementalType.ENERGY) {
            displayName = "&3Fragmento Elemental de Energía";
            customModelData = 3;
        } else if (type == ElementalType.FIRE) {
            displayName = "&6Fragmento Elemental de Fuego";
            customModelData = 4;
        } else {
            displayName = "&bFragmento Elemental de Agua";
            customModelData = 5;
        }

        ItemStack s = new ItemBuilder(Material.PINK_DYE)
                .setCustomModelData(customModelData, !Main.optifineItemsEnabled())
                .setDisplayName(TextUtils.format(displayName)).build();

        ItemMeta meta = s.getItemMeta();
        meta.setUnbreakable(true);
        meta.setLore(Arrays.asList(HiddenStringUtils.encodeString("{" + UUID.randomUUID().toString() + ": 0}")));
        s.setItemMeta(meta);

        return s;
    }

    public static ItemStack createElementalizador() {
        ItemStack s = new ItemBuilder(Material.YELLOW_DYE)
                .setCustomModelData(1, !Main.optifineItemsEnabled())
                .setDisplayName(TextUtils.format("&7Ele&2men&3ta&6li&bzador")).build();

        ItemMeta meta = s.getItemMeta();
        meta.setUnbreakable(true);
        meta.setLore(Arrays.asList(
                TextUtils.format("&4¡¡Tienes en tu poder una serie de habilidades grandiosas!!"),
                TextUtils.format("&8Cooldown: 5 minutos"),
                TextUtils.format("&3Solo se puede activar una habilidad a la vez!")));
        s.setItemMeta(meta);

        return s;
    }

    public static boolean isElementalizador(ItemStack item) {
        ItemMeta meta = item.getItemMeta();

        if (meta != null && meta.isUnbreakable() && meta.hasLore() && meta.hasDisplayName()
                && item.isSimilar(createElementalizador()) && meta.hasCustomModelData()) {
            return true;
        }
        System.out.println("NO ES XD ");
        return false;
    }

    public static ElementalType extractFragmentElementalType(ItemStack fragment) {
        ItemMeta meta = fragment.getItemMeta();

        if (meta.isUnbreakable() && meta.hasLore() && meta.hasDisplayName()
                && meta.getDisplayName().contains("Fragmento Elemental de")) {
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
