package tech.sebazcrc.permadeath.event.player;

import static org.bukkit.Bukkit.getServer;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import tech.sebazcrc.permadeath.Main;
import tech.sebazcrc.permadeath.util.item.ElementalItems;
import tech.sebazcrc.permadeath.util.lib.ElementalType;

public class AbilityListener implements Listener {

    private BukkitTask ability;
    private Main plugin;
    private ElementalType activeAbility;
    private boolean cooldownActive = false;
    private final int DURATION = 5 * 60 * 20;

    public AbilityListener(Main instance) {
        this.plugin = instance;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (this.ability != null || this.activeAbility != null || this.cooldownActive)
            return;

        Player p = e.getPlayer();
        PotionEffect effect = new PotionEffect(PotionEffectType.HEALTH_BOOST, DURATION, 0);

        if (e.getAction() == Action.RIGHT_CLICK_AIR) {
            ItemStack item = p.getInventory().getItemInMainHand();
            if (item != null && ElementalItems.isElementalizador(item)) {
                this.activeAbility = ElementalType.AIR;
                // SkillTimerRunnable.startSkillTimer(p, plugin, 300);
                p.setAllowFlight(true);
                p.addPotionEffect(effect);
                this.ability = new BukkitRunnable() {
                    @Override
                    public void run() {
                        getServer().getWorlds().forEach(w -> w.setStorm(false));
                    }
                }.runTaskTimer(plugin, 0, 20);
            }
        }

        if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
            ItemStack item = p.getInventory().getItemInMainHand();
            if (item != null && ElementalItems.isElementalizador(item)) {
                this.activeAbility = ElementalType.ENERGY;

                p.addPotionEffect(effect);
            }
        }

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = p.getInventory().getItemInMainHand();
            if (item != null && ElementalItems.isElementalizador(item)) {
                if (isTypeDirt(e.getClickedBlock().getType())) {
                    this.activeAbility = ElementalType.EARTH;

                    p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, DURATION, 1));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, DURATION, 2));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, DURATION, 5));
                    p.addPotionEffect(effect);
                } else if (e.getClickedBlock().getType() == Material.WATER) {
                    this.activeAbility = ElementalType.WATER;

                    p.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, DURATION, 1));
                    p.addPotionEffect(effect);
                    this.ability = new BukkitRunnable() {
                        @Override
                        public void run() {
                            p.setRemainingAir(300);
                        }
                    }.runTaskTimer(plugin, 0, 20);
                } else if (e.getClickedBlock().getType() == Material.LAVA) {
                    this.activeAbility = ElementalType.FIRE;

                    p.addPotionEffect(effect);
                }
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (activeAbility != null && activeAbility == ElementalType.AIR && p.getAllowFlight()) {
                    p.setAllowFlight(false);
                    p.setFlying(false);
                }
                if (ability != null) {
                    ability.cancel();
                    ability = null;
                }

                activeAbility = null;
                cooldownActive = true;

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        cooldownActive = false;
                    }
                }.runTaskLater(plugin, 10 * 60 * 20);
            }
        }.runTaskLater(plugin, DURATION);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (this.activeAbility == null)
            return;

        if (event.getEntity() instanceof Player) {
            Entity damager = event.getDamager();
            if (this.activeAbility == ElementalType.WATER && isAquaticMob(damager)) {
                event.setCancelled(true);
            }
        }

        if (this.activeAbility == ElementalType.ENERGY) {
            if (event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) event.getEntity();

                Location targetLocation = target.getLocation();

                target.getWorld().strikeLightning(targetLocation);
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (this.activeAbility == null)
            return;

        if (event.getEntity() instanceof Player) {
            if (this.activeAbility == ElementalType.FIRE) {
                if (event.getCause() == EntityDamageEvent.DamageCause.LAVA ||
                        event.getCause() == EntityDamageEvent.DamageCause.HOT_FLOOR) {
                    event.setCancelled(true);
                }
            }
            if (this.activeAbility == ElementalType.AIR) {
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (this.activeAbility == null)
            return;

        if (this.activeAbility == ElementalType.FIRE) {
            Block blockBelow = event.getPlayer().getLocation().subtract(0, 1, 0).getBlock();

            if (blockBelow.getType() == Material.LAVA) {
                blockBelow.setType(Material.OBSIDIAN);
            }
        }
    }

    @EventHandler
    public void onShootBow(EntityShootBowEvent event) {
        if (this.activeAbility == null)
            return;

        if (this.activeAbility == ElementalType.FIRE) {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();

                event.setCancelled(true);

                Location eyeLocation = player.getEyeLocation();
                Vector direction = eyeLocation.getDirection();

                Fireball fireball = player.getWorld().spawn(eyeLocation.add(direction.multiply(2)), Fireball.class);

                fireball.setDirection(direction);

                fireball.setYield(6);

                fireball.setIsIncendiary(false);
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (this.activeAbility == null)
            return;

        if (event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getEntity();
            if (this.activeAbility == ElementalType.EARTH) {
                if (arrow.getShooter() instanceof Player) {
                    Location hitLocation = arrow.getLocation();
                    placeWebsAround(hitLocation);
                }
            }

            if (this.activeAbility == ElementalType.ENERGY) {
                if (event.getHitEntity() != null) {
                    Entity hitEntity = event.getHitEntity();
                    if (hitEntity instanceof Mob) {
                        Location hitLocation = hitEntity.getLocation();
                        hitEntity.getWorld().strikeLightning(hitLocation);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (this.activeAbility == null)
            return;

        Player player = event.getPlayer();

        if (this.activeAbility == ElementalType.ENERGY) {
            if (player.isSneaking()) {
                List<Entity> nearbyEntities = player.getNearbyEntities(5, 5, 5);

                for (Entity entity : nearbyEntities) {
                    if (entity != player && entity.getType().isAlive()) {
                        player.getWorld().strikeLightning(entity.getLocation());
                    }
                }
            }
        }
    }

    private void placeWebsAround(Location location) {
        int radius = 1;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location blockLocation = location.clone().add(x, y, z);
                    if (blockLocation.getBlock().getType() == Material.AIR) {
                        blockLocation.getBlock().setType(Material.COBWEB);
                    }
                }
            }
        }
    }

    private boolean isAquaticMob(Entity entity) {
        // Comprueba si la entidad es un mob acuático
        EntityType type = entity.getType();
        return type == EntityType.DOLPHIN ||
                type == EntityType.GUARDIAN ||
                type == EntityType.ELDER_GUARDIAN ||
                type == EntityType.SQUID ||
                type == EntityType.COD ||
                type == EntityType.SALMON ||
                type == EntityType.DROWNED ||
                type == EntityType.TROPICAL_FISH ||
                type == EntityType.PUFFERFISH ||
                type == EntityType.TURTLE;
    }

    private boolean isTypeDirt(Material m) {
        if (m == Material.DIRT || m == Material.COARSE_DIRT || m == Material.PODZOL || m == Material.MYCELIUM
                || m == Material.FARMLAND)
            return true;
        return false;
    }

}
