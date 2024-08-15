package tech.sebazcrc.permadeath.util.interfaces;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface ElementalMob {
    boolean spawnElemental(Location location);

    void showBossBar(Location location);

    void hideBossBar();

    boolean isElemental(Entity entity);

    void loadConfig();

    void confirmDeath();

    void saveConfigElemental();
}
