package tech.sebazcrc.permadeath.event;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataType;

import tech.sebazcrc.permadeath.Main;
import tech.sebazcrc.permadeath.util.Utils;

public class HostileEntityListener implements Listener {
    private final Main instance;

    public HostileEntityListener(Main instance) {
        this.instance = instance;
        initialize();
    }

    public void initialize() {
        if (instance.getDay() >= 20) {
            EntityType type;
            for (World w : Bukkit.getWorlds()) {
                for (LivingEntity entity : w.getLivingEntities()) {
                    type = entity.getType();

                    if (!Utils.isHostileMob(type) && type != EntityType.ENDERMAN) {
                        injectHostileBehavior(entity);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent e) {
        if (e.isCancelled()) return;

        if (instance.getDay() >= 20 && !Utils.isHostileMob(e.getEntityType()) && e.getEntityType() != EntityType.ARMOR_STAND && e.getEntityType() != EntityType.ENDERMAN) {
            if (instance.getDay() >= 70 && e.getEntityType() == EntityType.VILLAGER 
                    && e.getEntity().getPersistentDataContainer().has(Utils.UNIQUE_VILLAGER_KEY, PersistentDataType.STRING)) {
                // No incluir al villager en el dia 70 como agresivo
            } else {
                injectHostileBehavior(e.getEntity());
            }
        }
    }

    private void injectHostileBehavior(LivingEntity entity) {
        instance.getNmsAccessor().injectHostilePathfinders(entity);
        if (entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) == null) {
            instance.getNmsAccessor().registerAttribute(Attribute.GENERIC_ATTACK_DAMAGE, 8.0D, entity);
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if (instance.getDay() < 20 || e.isNewChunk()) return;

        for (Entity entity : e.getChunk().getEntities()) {
            if (!entity.isValid() || entity.isDead()) continue;
            if (!(entity instanceof LivingEntity) || entity instanceof Player) continue;

            if (entity instanceof Villager && instance.getDay() >= 60) {
                int chance = new Random().nextInt(100);
                if (instance.getDay() >= 70 && chance < 10) {
                    Villager villager = entity.getWorld().spawn(entity.getLocation(), Villager.class);
                    villager.getPersistentDataContainer().set(Utils.UNIQUE_VILLAGER_KEY, 
                        PersistentDataType.STRING, "passive_villager");
                    entity.remove();
                } else {
                    entity.getWorld().spawn(entity.getLocation(), Vindicator.class);
                    entity.remove();
                }
                return;
            }

            injectHostileBehavior((LivingEntity) entity);
        }
    }
}
