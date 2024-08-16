package tech.sebazcrc.permadeath.event.player;

import java.util.Map;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;

import tech.sebazcrc.permadeath.Main;
import tech.sebazcrc.permadeath.util.item.ElementalItems;

public class CraftingListener implements Listener {
    private final Main plugin;

    private final String SECTION = "LimitCraft";
    private final int MAX_CRAFTS_PER_DAY = 15;
    private Map<String, Integer> countCrafts;

    public CraftingListener(Main instance) {
        this.plugin = instance;
        countCrafts = new HashMap<>();

        loadLimitCraft();
    }

    @EventHandler
    public void onCrafting(CraftItemEvent e) {
        if (plugin.getDay() >= 70) {
            String playerId = e.getWhoClicked().getUniqueId().toString();
            Integer numCraft = countCrafts.get(playerId);
            if (numCraft != null && numCraft >= MAX_CRAFTS_PER_DAY) {
                e.setCancelled(true);
                e.getWhoClicked().sendMessage("Has superado el limite de crafteos por dia.");
            } else {
                countCrafts.put(playerId, (numCraft == null ? 0 : numCraft) + 1);
            }
        }
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();
        Recipe recipe = event.getRecipe();

        if (recipe != null && recipe.getResult().isSimilar(ElementalItems.createElementalizador())) {
            ItemStack[] matrix = inventory.getMatrix(); // Items en la cuadrícula de crafteo

            boolean[] validElemental = {
                    false, // AIR
                    false, // WATER
                    false, // FIRE
                    false, // ENERGY
                    false, // EARTH
            };

            boolean validAll = true;

            for (ItemStack item : matrix) {
                if (item != null && item.getType() == Material.PINK_DYE) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null && meta.hasCustomModelData()) {
                        int customModelData = meta.getCustomModelData();
                        if (customModelData == 1) {
                            validElemental[0] = true;
                        } else if (customModelData == 2) {
                            validElemental[4] = true;
                        } else if (customModelData == 3) {
                            validElemental[3] = true;
                        } else if (customModelData == 4) {
                            validElemental[2] = true;
                        } else if (customModelData == 5) {
                            validElemental[1] = true;
                        }
                    }
                }
            }

            for (boolean value : validElemental) {
                if (!value) {
                    validAll = false;
                    break;
                }
            }

            // Si alguno de los items no es válido, cancela el crafteo
            if (!validAll) {
                inventory.setResult(null);
            }
        }
    }

    private void loadLimitCraft() {
        ConfigurationSection limitCraftSection = plugin.getConfig().getConfigurationSection(SECTION);
        if (limitCraftSection == null) {
            limitCraftSection = plugin.getConfig().createSection(SECTION);
        }
        for (String uuid : limitCraftSection.getKeys(false)) {
            countCrafts.put(uuid, limitCraftSection.getInt(uuid));
        }
        plugin.saveConfig();
    }

    public void saveLimitCraft() {
        ConfigurationSection limitCraftSection = plugin.getConfig().getConfigurationSection(SECTION);
        if (limitCraftSection == null) {
            limitCraftSection = plugin.getConfig().createSection(SECTION);
        }
        countCrafts.forEach(limitCraftSection::set);
        plugin.saveConfig();
    }

    public void resetLimitCraft() {
        ConfigurationSection limitCraftSection = plugin.getConfig().getConfigurationSection(SECTION);
        if (limitCraftSection == null) {
            limitCraftSection = plugin.getConfig().createSection(SECTION);
        }
        countCrafts.replaceAll((key, value) -> 0);
    }

}
