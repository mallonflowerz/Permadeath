package tech.sebazcrc.permadeath.util.mob;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import tech.sebazcrc.permadeath.Main;

public class ElementalSpider implements Listener {

    private final Main plugin;
    private final Random RANDOM = new Random();

    private final Map<Spider, Double> spiderHealth = new HashMap<>();
    private final Set<Spider> affectedSpiders = new HashSet<>();

    public ElementalSpider(Main instance) {
        plugin = instance;
    }

    @EventHandler
    public void onSpiderAttack(EntityDamageByEntityEvent event) {
        // Check if the cause of the damage is an entity attacking another entity
        Entity damager = event.getDamager();
        Entity target = event.getEntity();

        if (RANDOM.nextInt(100) < 30) {
            if (event.getCause() == DamageCause.ENTITY_ATTACK) {
                // Check if the damager is a spider and the target is a player
                if (damager instanceof Spider && target instanceof Player) {
                    Player player = (Player) target;

                    // Get the player's location
                    Location loc = player.getLocation();

                    // Encerrar al jugador en telarañas
                    encerrarEnTela(loc);
                }
            }
        }

        if (RANDOM.nextInt(100) < 50) {
            if (damager instanceof Arrow && target instanceof Spider) {
                Arrow arrow = (Arrow) event.getDamager();
                if (arrow.getShooter() instanceof Player) {
                    Player player = (Player) arrow.getShooter();
                    player.damage(event.getDamage());
                    player.setFireTicks(arrow.getFireTicks());
                }
            }
        }

        if (RANDOM.nextInt(100) < 25) {
            if (damager instanceof Player && target instanceof Spider) {
                Location spiderLocation = target.getLocation();
                spiderLocation.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, spiderLocation, 1);
                // Empujar al jugador lejos de la araña
                Vector direction = damager.getLocation().toVector().subtract(spiderLocation.toVector()).normalize();
                direction.multiply(2.0); // Aumentar la fuerza del empuje
                direction.setY(1.0); // Añadir un poco de elevación al empuje
                damager.setVelocity(direction);
            }
        }

        if (RANDOM.nextInt(100) < 40) {
            if (damager instanceof Player && target instanceof Spider) {
                Player player = (Player) damager;
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 10 * 20, 3));
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10 * 20, 1));
            }
        }
    }

    @EventHandler
    public void onSpiderDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Spider) {
            Spider spider = (Spider) event.getEntity();
            double currentHealth = spider.getHealth();
            double maxHealth = spider.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
            double healthThreshold = maxHealth * 0.75; // 75% of max health

            // Initialize spider's health if it's the first time
            if (!spiderHealth.containsKey(spider)) {
                spiderHealth.put(spider, maxHealth);
            }

            // Get previous health from the map
            double previousHealth = spiderHealth.get(spider);

            // Check if health has dropped below the threshold
            if (previousHealth > healthThreshold && currentHealth <= healthThreshold) {
                applyEffects(spider);
            }

            // Update the spider's health
            spiderHealth.put(spider, currentHealth);
        }
    }

    @EventHandler
    public void onSpiderDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Spider) {
            Spider spider = (Spider) event.getEntity();
            if (spiderHealth.containsKey(spider)) {
                spiderHealth.remove(spider);
            }
            if (affectedSpiders.contains(spider)) {
                affectedSpiders.remove(spider);
            }
        }
    }

    private void applyEffects(Spider spider) {
        // Make the spider invulnerable and immobile
        spider.setInvulnerable(true);
        spider.setAI(false);
        affectedSpiders.add(spider);

        BukkitTask particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                Location center = spider.getLocation();
                for (double angle = 0; angle < 360; angle += 10) {
                    double x = 5 * Math.cos(Math.toRadians(angle));
                    double z = 5 * Math.sin(Math.toRadians(angle));
                    center.getWorld().spawnParticle(Particle.DRAGON_BREATH, center.clone().add(x, 1, z), 5);
                }

                // Detect players in the area and apply damage
                for (Entity entity : center.getWorld().getNearbyEntities(center, 5, 5, 5)) {
                    if (entity instanceof Player) {
                        Player player = (Player) entity;
                        if (player.getLocation().distance(center) <= 5) {
                            player.damage(2.0); // Apply damage to players
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second (20 ticks)

        // Schedule a task to revert spider to normal state after 10 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                particleTask.cancel(); // Cancel the particle effect task
                spider.setInvulnerable(false);
                spider.setAI(true);
                affectedSpiders.remove(spider);
            }
        }.runTaskLater(plugin, 200L); // 200 ticks = 10 seconds
    }

    private void encerrarEnTela(Location loc) {
        // Define las posiciones relativas para colocar las telarañas alrededor del
        // jugador
        int[][] offsets = {
                { 0, 0, 0 }, // Posición del jugador
                { 1, 0, 0 }, { -1, 0, 0 }, { 0, 0, 1 }, { 0, 0, -1 }, // Alrededor del jugador en el mismo nivel
                { 1, 0, 1 }, { -1, 0, 1 }, { 1, 0, -1 }, { -1, 0, -1 }, // Diagonales
                { 0, 1, 0 }, // Justo encima del jugador
        };

        for (int[] offset : offsets) {
            // Obtener la posición específica
            Location webLoc = loc.clone().add(offset[0], offset[1], offset[2]);
            Block block = webLoc.getBlock();

            // Solo colocar telaraña si el bloque es aire
            if (block.getType() == Material.AIR) {
                block.setType(Material.COBWEB);
            }
        }
    }
}
