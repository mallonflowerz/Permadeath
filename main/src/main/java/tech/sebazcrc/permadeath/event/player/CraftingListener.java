package tech.sebazcrc.permadeath.event.player;

import java.util.Map;
import java.util.HashMap;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;

import tech.sebazcrc.permadeath.Main;

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
