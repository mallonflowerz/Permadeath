package tech.sebazcrc.permadeath.util.mob.elementals;

import static tech.sebazcrc.permadeath.util.Utils.format;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import tech.sebazcrc.permadeath.Main;
import tech.sebazcrc.permadeath.util.BarBoss;
import tech.sebazcrc.permadeath.util.Utils;
import tech.sebazcrc.permadeath.util.interfaces.ElementalMob;

public class ElementalGhast implements Listener, ElementalMob {

    private final Main plugin;
    private boolean isDead;
    private boolean ghastActive;
    private String coorsGhast;
    private BarBoss bossBar;
    private final Random RANDOM = new Random();

    public final static String SECTION = "ElementalGhast";

    private final double MAX_HEALTH = 800.0;

    private NamespacedKey ghastKey;

    public ElementalGhast(Main instance) {
        this.plugin = instance;

        bossBar = new BarBoss();
        ghastKey = new NamespacedKey(instance, "elemental_ghast");

        loadConfig();
    }

    @Override
    public boolean spawnElemental(Location location) {
        if (this.ghastActive || this.isDead)
            return false;
        Ghast ghast = location.getWorld().spawn(location, Ghast.class);
        ghast.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(MAX_HEALTH);
        ghast.setHealth(MAX_HEALTH);
        ghast.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(1);
        ghast.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0.4);
        ghast.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(120.0);
        // ghast.getAttribute(Attribute.GENERIC_FLYING_SPEED).setBaseValue(0.8);
        ghast.setCustomName(format("&bElemental Ghast"));
        ghast.setCustomNameVisible(false);
        ghast.getPersistentDataContainer().set(ghastKey, PersistentDataType.BYTE,
                (byte) 1);
        ghast.setRemoveWhenFarAway(false);
        showBossBar(location);
        this.ghastActive = true;
        this.coorsGhast = String.format("%s %s %s", location.getX(), location.getY(),
                location.getZ());
        return true;
    }

    @EventHandler
    public void onGhastAttack(EntityDamageByEntityEvent event) {
        if (this.isDead)
            return;
        Entity damager = event.getDamager();
        Entity target = event.getEntity();

        if (damager instanceof Fireball && target instanceof Player) {
            Fireball fireball = (Fireball) damager;
            Player player = (Player) target;
            if (fireball.getShooter() instanceof Ghast) {
                Ghast ghast = (Ghast) fireball.getShooter();
                if (isElemental(ghast)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 2 * 20, 1));
                    if (RANDOM.nextInt(100) < 10) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 10 * 20, 9));
                    }

                    if (RANDOM.nextInt(100) < 11) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 4 * 20, 9));
                    }

                    if (RANDOM.nextInt(100) < 6) {
                        ghast.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 6 * 20, 2));
                        ghast.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 6 * 20, 0));
                        new BukkitRunnable() {
                            private int shotsFired = 0;

                            @Override
                            public void run() {
                                if (shotsFired >= 6 || ghast.isDead()) {
                                    cancel();
                                    return;
                                }

                                LivingEntity targetByGhast = ghast.getTarget();
                                if (targetByGhast != null && ghast.hasLineOfSight(targetByGhast)) {
                                    ghast.launchProjectile(Fireball.class);
                                    shotsFired++;
                                }
                            }
                        }.runTaskTimer(plugin, 0L, 6 * 20);
                    }

                    if (RANDOM.nextInt(100) < 8) {
                        Vector direction = player.getLocation().toVector().subtract(fireball.getLocation().toVector())
                                .normalize();

                        direction.multiply(4);

                        direction.setY(4);

                        player.setVelocity(direction);

                        player.getWorld().createExplosion(player.getLocation(), 0, false, false);
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
                    }
                }
            }
        }

        if (damager instanceof Arrow && isElemental(target)) {
            Arrow arrow = (Arrow) damager;
            if (arrow.getShooter() instanceof Player) {
                Player player = (Player) arrow.getShooter();
                if (RANDOM.nextInt(100) < 30) {
                    event.setCancelled(true);
                    player.damage(event.getDamage() * 2.0);
                }

                if (RANDOM.nextInt(100) < 6) {
                    player.setFireTicks(15 * 20);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 * 20, 4));
                }

                if (RANDOM.nextInt(100) < 13) {
                    target.teleport(player);
                    target.setInvulnerable(true);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            target.setInvulnerable(false);
                        }
                    }.runTaskLater(plugin, 5 * 20);
                }

                if (RANDOM.nextInt(100) < 20) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 5 * 20, 19));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 5 * 20, 4));
                }

                if (RANDOM.nextInt(100) < 4) {
                    target.addPassenger(player);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 7 * 20, 4));
                    target.setInvulnerable(true);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            target.setInvulnerable(false);
                            target.removePassenger(player);
                        }
                    }.runTaskLater(plugin, 7 * 20);
                }

                if (RANDOM.nextInt(100) < 11) {
                    ItemStack itemInHand = player.getInventory().getItemInMainHand();
                    if (itemInHand == null || itemInHand.getType() == Material.AIR) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 6 * 20, 4));
                        return;
                    }

                    ItemStack itemToReturn = itemInHand.clone();
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // Verificar si el jugador aún tiene la mano vacía
                            if (player.getInventory().getItemInMainHand() == null
                                    || player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                                // Devolver el ítem al jugador
                                player.getInventory().setItemInMainHand(itemToReturn);
                            } else {
                                // Si la mano no está vacía, colocar el ítem en el inventario del jugador
                                player.getInventory().addItem(itemToReturn);
                            }
                        }
                    }.runTaskLater(plugin, 6 * 20);
                }

                if (RANDOM.nextInt(100) < 14) {
                    ItemStack itemInOff = player.getInventory().getItemInOffHand();
                    if (itemInOff == null || itemInOff.getType() == Material.AIR) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 6 * 20, 4));
                        return;
                    }

                    ItemStack itemToReturn = itemInOff.clone();
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // Verificar si el jugador aún tiene la mano vacía
                            if (player.getInventory().getItemInOffHand() == null
                                    || player.getInventory().getItemInOffHand().getType() == Material.AIR) {
                                // Devolver el ítem al jugador
                                player.getInventory().setItemInOffHand(itemToReturn);
                            } else {
                                // Si la mano no está vacía, colocar el ítem en el inventario del jugador
                                player.getInventory().addItem(itemToReturn);
                            }
                        }
                    }.runTaskLater(plugin, 6 * 20);
                }
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (this.isDead)
            return;
        if (event.getEntity().getType() == EntityType.FIREBALL) {
            Fireball fireball = (Fireball) event.getEntity();

            if (fireball.getShooter() instanceof Ghast) {
                Ghast ghast = (Ghast) fireball.getShooter();
                if (isElemental(ghast)) {
                    fireball.setYield((float) RANDOM.nextInt(100));

                }
            }
        }
    }

    @EventHandler
    public void onSpiderDeath(EntityDeathEvent event) {
        if (this.isDead)
            return;
        if (isElemental(event.getEntity())) {
            Ghast spider = (Ghast) event.getEntity();
            markDeath(spider);
        }
    }

    private void markDeath(Ghast ghast) {
        confirmDeath();
        hideBossBar();
        Bukkit.getOnlinePlayers()
                .forEach(p -> p.sendMessage(format("&eLa Elemental de Tierra ha muerto en &3") + this.coorsGhast));
    }

    @Override
    public void showBossBar(Location location) {
        this.bossBar.createBar(format("&3Elemental de Aire"), BarColor.WHITE, BarStyle.SOLID);
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
        if (entity instanceof Ghast) {
            Ghast ghast = (Ghast) entity;
            PersistentDataContainer data = ghast.getPersistentDataContainer();
            if (data.has(ghastKey, PersistentDataType.BYTE) && ghast.getCustomName() != null
                    && ghast.getCustomName().contains("Elemental de Aire")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void loadConfig() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(SECTION);
        if (section == null) {
            section = plugin.getConfig().createSection(SECTION);
            this.isDead = false;
            this.ghastActive = false;
            this.coorsGhast = "No disponibles";
            return;
        }
        this.isDead = section.getBoolean("Death", false);
        this.ghastActive = section.getBoolean("Active", false);
        this.coorsGhast = section.getString("Coors", "No disponibles");
    }

    @Override
    public void confirmDeath() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(SECTION);
        if (section != null) {
            this.isDead = true;
            this.ghastActive = false;
            section.set("Death", true);
            section.set("Active", false);
            section.set("Coors", this.coorsGhast);
        }
    }

    @Override
    public void saveConfigElemental() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(SECTION);
        if (section != null) {
            section.set("Death", this.isDead);
            section.set("Active", this.ghastActive);
            section.set("Coors", this.coorsGhast);
        }
        plugin.saveConfig();
    }

    public boolean getIsDead() {
        return this.isDead;
    }

    public String getCoors() {
        return this.coorsGhast;
    }

}
