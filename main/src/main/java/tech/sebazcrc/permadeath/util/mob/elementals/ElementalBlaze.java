package tech.sebazcrc.permadeath.util.mob.elementals;

import static tech.sebazcrc.permadeath.util.TextUtils.format;

import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import tech.sebazcrc.permadeath.Main;
import tech.sebazcrc.permadeath.util.BarBoss;
import tech.sebazcrc.permadeath.util.Utils;
import tech.sebazcrc.permadeath.util.interfaces.ElementalMob;

public class ElementalBlaze implements Listener, ElementalMob {

    private final Main plugin;
    private boolean isDead;
    private boolean blazeActive;
    private String coorsBlaze;
    private BarBoss bossBar;
    private final Random RANDOM = new Random();

    public final static String SECTION = "ElementalBlaze";

    private final double MAX_HEALTH = 650.0;

    private NamespacedKey blazeKey;

    public ElementalBlaze(Main instance) {
        this.plugin = instance;

        bossBar = new BarBoss();
        blazeKey = new NamespacedKey(instance, "elemental_blaze");

        loadConfig();
    }

    @Override
    public boolean spawnElemental(Location location) {
        if (this.blazeActive || this.isDead)
            return false;
        Blaze blaze = location.getWorld().spawn(location, Blaze.class);
        blaze.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(MAX_HEALTH);
        blaze.setHealth(MAX_HEALTH);
        blaze.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.8);
        blaze.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(21.0);
        blaze.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(32.0);
        blaze.setCustomName(format("&bElemental Blaze"));
        blaze.setCustomNameVisible(false);
        blaze.getPersistentDataContainer().set(blazeKey, PersistentDataType.BYTE,
                (byte) 1);
        blaze.setRemoveWhenFarAway(false);
        this.blazeActive = true;
        this.coorsBlaze = String.format("%s %s %s", location.getX(), location.getY(),
                location.getZ());
        return true;
    }

    @Override
    public void showBossBar(Location location) {
        this.bossBar.createBar(format("&2Elemental de Fuego"), BarColor.YELLOW, BarStyle.SOLID);
        List<Player> nearby = Utils.getNearbyPlayers(location, 15.0);
        this.bossBar.addPlayers(nearby);
        nearby.forEach(p -> p.sendMessage(format("&cLa batalla ha comenzado...")));
    }

    @Override
    public void hideBossBar() {
        this.bossBar.setVisible(false);
    }

    @Override
    public boolean isElemental(Entity entity) {
        if (entity instanceof Blaze) {
            Blaze blaze = (Blaze) entity;
            PersistentDataContainer data = blaze.getPersistentDataContainer();
            if (data.has(blazeKey, PersistentDataType.BYTE) && blaze.getCustomName() != null
                    && blaze.getCustomName().contains("Elemental Blaze")) {
                return true;
            }
        }
        return false;
    }

    // Aqui van los eventos

    @Override
    public void loadConfig() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(SECTION);
        if (section == null) {
            section = plugin.getConfig().createSection(SECTION);
            this.isDead = false;
            this.blazeActive = false;
            this.coorsBlaze = "No disponibles";
            return;
        }
        this.isDead = section.getBoolean("Death", false);
        this.blazeActive = section.getBoolean("Active", false);
        this.coorsBlaze = section.getString("Coors", "No disponibles");
    }

    @Override
    public void confirmDeath() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(SECTION);
        if (section != null) {
            this.isDead = true;
            this.blazeActive = false;
            section.set("Death", true);
            section.set("Active", false);
            section.set("Coors", this.coorsBlaze);
        }
    }

    @Override
    public void saveConfigElemental() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(SECTION);
        if (section != null) {
            section.set("Death", this.isDead);
            section.set("Active", this.blazeActive);
            section.set("Coors", this.coorsBlaze);
        }
        plugin.saveConfig();
    }

}
