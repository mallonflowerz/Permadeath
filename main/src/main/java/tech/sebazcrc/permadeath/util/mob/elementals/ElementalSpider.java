package tech.sebazcrc.permadeath.util.mob.elementals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import tech.sebazcrc.permadeath.Main;

public class ElementalSpider implements Listener {

    private final Main plugin;
    private final Random RANDOM = new Random();
    private boolean isDead;
    private boolean spiderActive;
    private String coorsSpider;
    private BossBar spiderBossBar;
    private Audience audience;
    public final static String SECTION = "ElementalSpider";

    private final Map<Spider, Double> spiderHealth = new HashMap<>();
    private final Set<Spider> affectedSpiders = new HashSet<>();
    private final double MAX_HEALTH = 40.0;

    private NamespacedKey spiderKey;

    public ElementalSpider(Main instance) {
        plugin = instance;
        spiderKey = new NamespacedKey(instance, "elemental_spider");

        loadConfig();
        audience = this.plugin.adventure().players();
        spiderBossBar = BossBar.bossBar(Component.text("Elemental de Tierra").color(NamedTextColor.DARK_GREEN),
                (float) MAX_HEALTH, Color.GREEN, Overlay.NOTCHED_6);
    }

    public boolean spawnElementalSpider(Location location) {
        if (this.spiderActive || this.isDead || !this.coorsSpider.isEmpty())
            return false;
        Spider spider = location.getWorld().spawn(location.clone(), Spider.class);
        spider.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(MAX_HEALTH);
        spider.setHealth(MAX_HEALTH);
        spider.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(14.0);
        spider.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(32.0);
        spider.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.7);
        spider.setCustomName("Elemental Spider");
        spider.setCustomNameVisible(false);
        spider.getPersistentDataContainer().set(spiderKey, PersistentDataType.BYTE,
                (byte) 1);
        spider.setRemoveWhenFarAway(false);
        showBossBar();
        this.spiderActive = true;
        this.coorsSpider = String.format("%s %s %s", location.clone().getX(), location.clone().getY(),
                location.clone().getZ());
        return true;
    }

    private void showBossBar() {
        audience.showBossBar(spiderBossBar);
        Bukkit.getConsoleSender().sendMessage("&cLa batalla ha comenzado...");
    }

    private void hideBossBar() {
        audience.hideBossBar(spiderBossBar);
    }

    private boolean isElementalSpider(Entity entity) {
        if (entity instanceof Spider) {
            Spider spider = (Spider) entity;
            PersistentDataContainer data = spider.getPersistentDataContainer();
            if (data.has(spiderKey, PersistentDataType.BYTE) && spider.getCustomName() != null
                    && spider.getCustomName().equalsIgnoreCase("Elemental Spider")) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onSpiderAttack(EntityDamageByEntityEvent event) {
        if (this.isDead)
            return;
        // Check if the cause of the damage is an entity attacking another entity
        Entity damager = event.getDamager();
        Entity target = event.getEntity();

        if (RANDOM.nextInt(100) < 30) {
            if (event.getCause() == DamageCause.ENTITY_ATTACK) {
                // Check if the damager is a spider and the target is a player
                if (isElementalSpider(damager) && target instanceof Player) {
                    Player player = (Player) target;

                    // Get the player's location
                    Location loc = player.getLocation();

                    // Encerrar al jugador en telarañas
                    encerrarEnTela(loc);
                }
            }
        }

        if (RANDOM.nextInt(100) < 50) {
            if (damager instanceof Arrow && isElementalSpider(target)) {
                Arrow arrow = (Arrow) event.getDamager();
                if (arrow.getShooter() instanceof Player) {
                    Player player = (Player) arrow.getShooter();
                    player.damage(event.getDamage());
                    player.setFireTicks(arrow.getFireTicks());
                }
            }
        }

        if (RANDOM.nextInt(100) < 25) {
            if (damager instanceof Player && isElementalSpider(target)) {
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
            if (damager instanceof Player && isElementalSpider(target)) {
                Player player = (Player) damager;
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 10 * 20, 3));
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10 * 20, 1));
            }
        }

        if (RANDOM.nextInt(100) < 8) {
            if (isElementalSpider(target)) {
                createDisappearEffect(target);
                this.spiderActive = false;
            }
        }
    }

    @EventHandler
    public void onSpiderDamage(EntityDamageEvent event) {
        if (this.isDead)
            return;
        if (isElementalSpider(event.getEntity())) {
            Spider spider = (Spider) event.getEntity();
            double currentHealth = spider.getHealth();
            double maxHealth = spider.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            double healthThreshold = maxHealth * 0.75; // 75% of max health

            // Initialize spider's health if it's the first time
            if (!spiderHealth.containsKey(spider)) {
                spiderHealth.put(spider, maxHealth);
            }

            // Get previous health from the map
            double previousHealth = spiderHealth.get(spider);

            // Actualizar la barra de jefe según la salud restante
            float healthPercentage = (float) (currentHealth / maxHealth);
            spiderBossBar.progress(healthPercentage); // Actualizar la barra de jefe

            // Check if health has dropped below the threshold
            if (previousHealth > healthThreshold && currentHealth <= healthThreshold) {
                applyEffects(spider);
            }

            // Update the spider's health
            spiderHealth.put(spider, currentHealth);

            if (RANDOM.nextInt(100) < 18) {
                spider.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10 * 20, 1));
                spider.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10 * 20, 1));
                spider.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 10 * 20, 0));
            } else if (RANDOM.nextInt(100) < 8) {
                spider.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10 * 20, 3));
                spider.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 10 * 20, 4));
                spider.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10 * 20, 0));
                spider.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 10 * 20, 0));
            }

            if (RANDOM.nextInt(100) < 15) {
                Location spiderLocation = spider.getLocation();
                spawnMobsAroundSpider(spiderLocation, RANDOM.nextInt(10) + 1);
            }

        }
    }

    @EventHandler
    public void onSpiderDeath(EntityDeathEvent event) {
        if (this.isDead)
            return;
        if (isElementalSpider(event.getEntity())) {
            Spider spider = (Spider) event.getEntity();
            markDeath(spider);
        }
    }

    private void markDeath(Spider spider) {
        if (spiderHealth.containsKey(spider)) {
            spiderHealth.remove(spider);
        }
        if (affectedSpiders.contains(spider)) {
            affectedSpiders.remove(spider);
        }
        confirmDeath();
        hideBossBar();
        Bukkit.getConsoleSender().sendMessage("&eLa Elemental de Tierra ha muerto en &3" + this.coorsSpider);
    }

    private void createDisappearEffect(Entity spider) {
        Location location = spider.getLocation();

        // Create a particle effect (simulating the spider "esfumando")
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 20) { // Effect lasts for 1 second (20 ticks)
                    spider.remove(); // Remove the spider after the effect
                    cancel();
                    return;
                }

                // Create particle effect (e.g., a "poof" of smoke)
                location.getWorld().spawnParticle(Particle.SMOKE_LARGE, location, 10, 0.5, 0.5, 0.5, 0.01);
                location.getWorld().spawnParticle(Particle.CLOUD, location, 10, 0.5, 0.5, 0.5, 0.01);

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 5L); // Run every 5 ticks (4 times per second)
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

    private void spawnMobsAroundSpider(Location location, int numberOfMobs) {
        World world = location.getWorld();
        EntityType[] types = {
                EntityType.VINDICATOR,
                EntityType.CREEPER,
                EntityType.EVOKER,
                EntityType.VEX,
                EntityType.PILLAGER,
                EntityType.SKELETON,
        };
        for (int i = 0; i < numberOfMobs; i++) {
            Location spawnLocation = location.clone().add(RANDOM.nextDouble() * 5 - 2.5, 0,
                    RANDOM.nextDouble() * 5 - 2.5);
            world.spawnEntity(spawnLocation, types[RANDOM.nextInt(types.length)]); // Puedes cambiar el tipo de mob aquí
        }
    }

    private void loadConfig() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(SECTION);
        if (section == null) {
            section = plugin.getConfig().createSection(SECTION);
            this.isDead = false;
            this.spiderActive = false;
            this.coorsSpider = "No disponibles";
            return;
        }
        this.isDead = section.getBoolean("Death", false);
        this.spiderActive = section.getBoolean("Active", false);
        this.coorsSpider = section.getString("Coors", "No disponibles");
    }

    private void confirmDeath() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(SECTION);
        if (section != null) {
            this.isDead = true;
            this.spiderActive = false;
            section.set("Death", true);
            section.set("Active", false);
        }
    }

    public void saveConfigElemental() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(SECTION);
        if (section != null) {
            section.set("Death", this.isDead);
            section.set("Active", this.spiderActive);
            section.set("Coors", this.coorsSpider);
        }
        plugin.saveConfig();
    }

    public boolean getIsDead() {
        return this.isDead;
    }

    public String getCoors() {
        return this.coorsSpider;
    }
}
