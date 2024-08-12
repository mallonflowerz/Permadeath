package tech.sebazcrc.permadeath.util.item;

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import tech.sebazcrc.permadeath.Main;
import tech.sebazcrc.permadeath.util.TextUtils;
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
}
