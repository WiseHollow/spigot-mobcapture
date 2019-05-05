package net.johnbrooks.mh.events;

import net.johnbrooks.mh.*;
import net.johnbrooks.mh.events.custom.CreatureCaptureEvent;
import net.johnbrooks.mh.events.custom.CreatureReleaseEvent;
import net.johnbrooks.mh.items.CaptureEgg;

import java.util.Random;

import net.johnbrooks.mh.items.UniqueProjectileData;
import net.johnbrooks.mh.managers.EconomyManager;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class EventManager implements Listener {
    public void initialize() {
        register(this);
        if (Settings.townyHook) {
            register(new TownyCaptureEvents());
        }
    }

    private void register(Listener listener) {
        Main.plugin.getServer().getPluginManager().registerEvents(listener, Main.plugin);
    }

    private void callEvent(Event event) {
        Main.plugin.getServer().getPluginManager().callEvent(event);
    }

    @EventHandler
    public void assignMetaDataOnLaunch(ProjectileLaunchEvent event) {

        //
        // Give the projectile meta data concerning the material type that is being launched!
        //

        //1) If a player is shooting a projectile.
        if (!event.isCancelled() && event.getEntity().getShooter() != null &&
                event.getEntity().getShooter() instanceof Player) {
            Projectile projectile = event.getEntity();

            //3) If its the correct project type, attach required meta data to projectile.
            if (projectile != null && projectile.getType().name().equalsIgnoreCase(Settings.projectileCatcherMaterial.name())) {
                if (UniqueProjectileData.isEnabled()) {
                    int indexOfProjectile = ((Player) projectile.getShooter()).getInventory().first(Settings.projectileCatcherMaterial);
                    ItemStack shot = ((Player) projectile.getShooter()).getInventory().getItem(indexOfProjectile);
                    if (UniqueProjectileData.isProjectile(shot)) {
                        FixedMetadataValue state = new FixedMetadataValue(Main.plugin, projectile.getType().name());
                        event.getEntity().setMetadata("type", state);
                    }
                } else {
                    FixedMetadataValue state = new FixedMetadataValue(Main.plugin, projectile.getType().name());
                    event.getEntity().setMetadata("type", state);
                }
            }
        }
    }

    @EventHandler
    public void onChickenSpawn(ProjectileHitEvent event) {
        //1) Check whether the projectile is an egg
        if (event.getEntity() instanceof Egg) {
            //2) If it meets the criteria to capture
            if (event.getEntity().hasMetadata("type") &&
                    event.getEntity().getMetadata("type").get(0).asString().equalsIgnoreCase(Settings.projectileCatcherMaterial.name()) &&
                    event.getHitEntity() != null) {
                //3) Do nothing
                return;
            } else {
                //4) Manually spawn the chick
                Random random = new Random(System.currentTimeMillis());
                if (random.nextInt(8) == 0) {
                    //5) Spawn a chicken
                    Chicken chicken = (Chicken) event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.CHICKEN);
                    chicken.setBaby();
                    if (random.nextInt(32) == 0) {
                        //6) Make it 4
                        Chicken chicken2 = (Chicken) event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.CHICKEN);
                        chicken2.setBaby();
                        Chicken chicken3 = (Chicken) event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.CHICKEN);
                        chicken3.setBaby();
                        Chicken chicken4 = (Chicken) event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.CHICKEN);
                        chicken4.setBaby();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onVanillaChickenSpawn(CreatureSpawnEvent event) {
        //1) Check whether we are spawning a chicken from an egg
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.EGG && event.getEntityType() == EntityType.CHICKEN) {
            //2) Automatically cancel the event so we can manually trigger it above
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void captureEvent(EntityDamageByEntityEvent event) {
        Player player = null;
        LivingEntity livingEntity = null;

        //1) Check for all capture initial requirements.
        if (event.getEntity() instanceof LivingEntity && !(event.getEntity() instanceof Player)) {
            livingEntity = (LivingEntity) event.getEntity();
            // Check item used requirements (projectile or melee).
            if (event.getDamager() instanceof Projectile && event.getDamager().hasMetadata("type") &&
                    event.getDamager().getMetadata("type").get(0).asString().equalsIgnoreCase(Settings.projectileCatcherMaterial.name()) &&
                    ((Projectile) event.getDamager()).getShooter() instanceof Player) {
                player = (Player) ((Projectile) event.getDamager()).getShooter();
            } else if (Settings.meleeCapture && event.getDamager() instanceof Player
                    && ((Player) event.getDamager()).getInventory().getItemInMainHand().getType() != Material.AIR
                    && ((Player) event.getDamager()).getInventory().getItemInMainHand().getType() == Settings.projectileCatcherMaterial) {
                player = ((Player) event.getDamager());
            }
        }

        if (player != null) {
            boolean success = attemptCapture(player, livingEntity);
            if (success) {
                event.setDamage(0);
            }
        }
    }

    private boolean attemptCapture(Player catcher, LivingEntity target) {
        if (!Main.permissionManager.hasPermissionToCapture(catcher, target)) {
            catcher.sendMessage(Language.PREFIX + "You do not have permission to capture this creature.");
            return false;
        }

        if (Settings.griefPreventionHook &&
                Main.griefPrevention.claimsEnabledForWorld(target.getWorld()) &&
                Main.griefPrevention.allowBuild(catcher, target.getLocation()) != null) {
            catcher.sendMessage(Language.PREFIX + "You do not have permission to capture creatures here.");
            return false;
        }

        //4) Check if this is a disabled world.
        if (Settings.isDisabledWorld(catcher.getWorld().getName())) {
            catcher.sendMessage(Language.PREFIX + "You cannot capture a creature in this world!");
            return false;
        }

        //5) Check if they have enough money/items.
        if (!catcher.hasPermission(Main.permissionManager.NoCost) && Settings.costMode != Settings.CostMode.NONE) {
            if (!EconomyManager.chargePlayer(catcher)) {
                catcher.sendMessage(Language.PREFIX + "You do not have enough " +
                        (Settings.costMode == Settings.CostMode.ITEM ?
                                Settings.costMaterial.name() :
                                "money (" + Settings.costVault + " required)."));
                return false;
            }
        }

        //6) Setup capture event and run it.
        CreatureCaptureEvent creatureCaptureEvent = new CreatureCaptureEvent(catcher, target);
        callEvent(creatureCaptureEvent);
        if (!creatureCaptureEvent.isCancelled()) {
            //8) Capture Logic
            CaptureEgg.captureLivingEntity(creatureCaptureEvent.getTargetEntity());
            target.remove();
            return true;
        }

        return false;
    }

    @EventHandler
    public void preventUseEggOnOtherEntity(PlayerInteractEntityEvent event) {
        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR && event.getHand() == EquipmentSlot.HAND
                && NBTManager.isSpawnEgg(event.getPlayer().getInventory().getItemInMainHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void useSpawnEgg(PlayerInteractEvent event) {
        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR &&
                event.getHand() == EquipmentSlot.HAND) {

            if (NBTManager.isSpawnEgg(event.getPlayer().getInventory().getItemInMainHand())) {
                if (Settings.isDisabledWorld(event.getPlayer().getWorld().getName()) || event.useItemInHand() == Event.Result.DENY) {
                    event.getPlayer().sendMessage(Language.PREFIX + "You cannot release a creature in this world!");
                    return;
                }

                if (event.getClickedBlock() != null && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getClickedBlock().getType() == Material.WATER)) {
                    Location target = event.getClickedBlock().getLocation().clone().add(0.5, 0, 0.5);

                    if (event.getBlockFace() != BlockFace.UP) {
                        // Make a friendly location to spawn the entity.
                        final Vector direction = event.getPlayer().getLocation().toVector().subtract(target.toVector()).normalize();
                        target = target.add(direction.multiply(2));
                    }

                    CreatureReleaseEvent creatureReleaseEvent = new CreatureReleaseEvent(event.getPlayer(), target);
                    callEvent(creatureReleaseEvent);
                    if (!creatureReleaseEvent.isCancelled()) {
                        // Release Logic

                        // 1) Spawn creature at target location and cancel event.
                        LivingEntity spawnedEntity = CaptureEgg.useSpawnItem(event.getPlayer().getInventory().getItemInMainHand(), target);
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(ChatColor.YELLOW + Main.plugin.getName() + ": " + ChatColor.BLUE + spawnedEntity.getType().name() + " successfully spawned!");

                        // 2) Remove itemstack from user, or reduce amount by 1.
                        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                            if (event.getPlayer().getInventory().getItemInMainHand().getAmount() <= 1) {
                                event.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                            } else {
                                int nextAmount = event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1;
                                event.getPlayer().getInventory().getItemInMainHand().setAmount(nextAmount);
                            }
                        }
                    }
                } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
                    //1) Let's prepare to throw the egg. Here we have the unit vector.
                    Vector direction = event.getPlayer().getLocation().getDirection().clone().normalize();

                    //2) Spawn item-drop.
                    ItemStack toThrow = event.getPlayer().getInventory().getItemInMainHand().clone();
                    toThrow.setAmount(1);
                    final Item item = event.getPlayer().getWorld().dropItem(
                            event.getPlayer().getLocation().clone().add(0, 1, 0),
                            toThrow);

                    //3) Prevent pickup, set direction, set velocity
                    item.setPickupDelay(Integer.MAX_VALUE);
                    item.getLocation().setDirection(direction);
                    item.setVelocity(direction.clone().multiply(1.5f));

                    Main.plugin.getServer().getScheduler().runTaskLater(Main.plugin, () -> {
                        if (!item.isDead()) {
                            Location fixedLocation = new Location(item.getLocation().getWorld(),
                                    item.getLocation().getBlockX() + 0.5f,
                                    item.getLocation().getBlockY() + 0.5f,
                                    item.getLocation().getBlockZ() + 0.5f);
                            CaptureEgg.useSpawnItem(item.getItemStack(), fixedLocation);
                            item.remove();
                        }
                    }, 60);

                    // 4) Remove itemstack from user, or reduce amount by 1.
                    if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                        if (event.getPlayer().getInventory().getItemInMainHand().getAmount() <= 1) {
                            event.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                        } else {
                            int nextAmount = event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1;
                            event.getPlayer().getInventory().getItemInMainHand().setAmount(nextAmount);
                        }
                    }
                }
            }
        }
    }
}
