package tech.sebazcrc.permadeath.util;

import java.util.*;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Utils {
    // public static final String RESOURCE_PACK_LINK =
    // "https://www.dropbox.com/s/h3v77ga72l9vhpg/PermaDeathCore%20RP%20v1.2.zip?dl=1";
    public static final String RESOURCE_PACK_LINK = "https://dl.dropbox.com/scl/fi/871cls207t40gpeodx1w5/PermaDeathCore-RP-v1.3.zip?rlkey=c49g4cflevxa1b9l6zsphvjbk&st=bafyfctt&dl=0";
    public static final String DISCORD_LINK = "https://discord.gg/w58wzrcJU8";
    public static final int RESOURCE_ID = 112343;
    public static final String SPIGOT_LINK = "https://www.spigotmc.org/resources/permadeath-%E2%98%A0%EF%B8%8F.112343/";
    public static NamespacedKey UNIQUE_VILLAGER_KEY;

    public static boolean isHostileMob(EntityType type) {
        if (type.toString().equals("WARDEN") || type == EntityType.ENDER_DRAGON || type == EntityType.WITHER
                || type == EntityType.BLAZE || type == EntityType.CREEPER || type == EntityType.GHAST
                || type == EntityType.MAGMA_CUBE || type == EntityType.SILVERFISH || type == EntityType.SKELETON
                || type == EntityType.SLIME || type == EntityType.ZOMBIE || type == EntityType.ZOMBIE_VILLAGER
                || type == EntityType.DROWNED || type == EntityType.WITHER_SKELETON || type == EntityType.WITCH
                || type == EntityType.PILLAGER || type == EntityType.EVOKER || type == EntityType.VINDICATOR
                || type == EntityType.RAVAGER || type == EntityType.VEX || type == EntityType.GUARDIAN
                || type == EntityType.ELDER_GUARDIAN || type == EntityType.SHULKER || type == EntityType.HUSK
                || type == EntityType.STRAY || type == EntityType.PHANTOM) {
            return true;
        } else {
            return false;
        }
    }

    public static void initializeKeys(JavaPlugin plugin) {
        UNIQUE_VILLAGER_KEY = new NamespacedKey(plugin, "villager_unique_id");
    }

    public static int getExperienceTotalFromLevels(int levels) {
        return 2 * levels * levels + 3 * levels;
    }

    public static List<Player> getNearbyPlayers(Location location, double radius) {
        List<Player> players = new ArrayList<>();
        for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
            if (entity instanceof Player) {
                players.add((Player) entity);
            }
        }
        return players;
    }
}
