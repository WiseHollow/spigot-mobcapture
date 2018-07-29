package net.johnbrooks.mh.events;

import com.palmergames.bukkit.towny.object.*;
import net.johnbrooks.mh.*;
import net.johnbrooks.mh.items.CaptureEgg;

import java.util.Optional;
import java.util.Random;

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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class EventManager implements Listener {
    public void initialize() {
        register(this);
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
        if (!event.isCancelled() && event.getEntity().getShooter() != null && event.getEntity().getShooter() instanceof Player) {
            Player player = (Player) event.getEntity().getShooter();

            //2) Check which projectile is being launched.
            ItemStack projectileItemStack = null;
            if (player.getInventory().getItemInMainHand().getType() == Material.BOW) {
                for (ItemStack itemStack : player.getInventory().getContents()) {
                    if (itemStack != null && itemStack.getType() == Material.ARROW) {
                        projectileItemStack = itemStack;
                        break;
                    }
                }
            } else {
                projectileItemStack = player.getInventory().getItemInMainHand();
            }


            //3) If its the correct project type, attach required meta data to projectile.
            if (projectileItemStack != null &&
                    projectileItemStack.getType().name().equalsIgnoreCase(Settings.projectileCatcherMaterial.name()))
            {
                FixedMetadataValue state = new FixedMetadataValue(Main.plugin, projectileItemStack.getType().name());
                event.getEntity().setMetadata("type", state);
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
    public void captureEvent(EntityDamageByEntityEvent event)
    {
        //1) Check for all capture initial requirements.
        if (event.getDamager() instanceof Projectile && event.getDamager().hasMetadata("type") &&
                event.getDamager().getMetadata("type").get(0).asString().equalsIgnoreCase(Settings.projectileCatcherMaterial.name()) &&
                event.getEntity() instanceof LivingEntity &&
                !(event.getEntity() instanceof Player) &&
                ((Projectile) event.getDamager()).getShooter() instanceof Player) {

            //2) Check for whether the player has the correct payment to catch.
            Player player = (Player) ((Projectile) event.getDamager()).getShooter();
            LivingEntity livingEntity = (LivingEntity) event.getEntity();

            //3) Check if the player has permission to capture creatures.
            if (!Main.permissionManager.hasPermissionToCapture(player, livingEntity)) {
                player.sendMessage(Language.PREFIX + "You do not have permission to capture this creature.");
                return;
            }

            if (Settings.townyHook) {
                String worldName = event.getEntity().getWorld().getName();
                int x = event.getEntity().getLocation().getChunk().getX();
                int z = event.getEntity().getLocation().getChunk().getZ();
                Optional<Town> optionalTown = TownyUniverse.getDataSource().getTowns().stream()
                        .filter(town -> town.getWorld().getName().equalsIgnoreCase(worldName) &&
                                town.getTownBlocks().stream().anyMatch(mTownBlock -> mTownBlock.getX() == x && mTownBlock.getZ() == z)).findFirst();
                if (optionalTown.isPresent()) {
                    Town town = optionalTown.get();
                    if (!town.hasResident(player.getName())) {
                        player.sendMessage(Language.PREFIX + "You do not have permission to capture creatures here.");
                        return;
                    }
                }
            }

            //4) Check if they have enough money/items.
            if (!player.hasPermission(Main.permissionManager.NoCost)) {
                if (!EconomyManager.chargePlayer(player))
                {
                    player.sendMessage(Language.PREFIX + "You do not have enough " +
                            (Settings.costMode == Settings.CostMode.ITEM ?
                                    Settings.costMaterial.name() :
                                    "money (" + Settings.costVault + " required)."));
                    return;
                }
            }

            //5) Check if this is a disabled world.
            if (Settings.isDisabledWorld(player.getWorld().getName())) {
                player.sendMessage(Language.PREFIX + "You cannot capture a creature in this world!");
            } else {
                //6) Setup capture event and run it.
                CreatureCaptureEvent creatureCaptureEvent = new CreatureCaptureEvent(player, livingEntity);
                callEvent(creatureCaptureEvent);
                if (!creatureCaptureEvent.isCancelled()) {
                    //7) Remove the damage from the entity so we don't kill it.
                    event.setDamage(0.0d);

                    //8) Capture Logic
                    CaptureEgg.captureLivingEntity(creatureCaptureEvent.getTargetEntity());
                    livingEntity.remove();
                }
            }


        }
    }

    @EventHandler
    public void useSpawnEgg(PlayerInteractEvent event)
    {
        if (event.getPlayer().getInventory().getItemInMainHand() != null &&
                event.getHand() == EquipmentSlot.HAND) {

            String nameOfCreature = CaptureEgg.getCreatureType(event.getPlayer().getInventory().getItemInMainHand());
            if (nameOfCreature != null) {
                if (Settings.isDisabledWorld(event.getPlayer().getWorld().getName())) {
                    event.getPlayer().sendMessage(Language.PREFIX + "You cannot release a creature in this world!");
                    return;
                }

                if (!event.isCancelled() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    Location target = event.getClickedBlock().getLocation().clone();

                    if (event.getBlockFace() != BlockFace.UP) {
                        // Make a friendly location to spawn the entity.
                        Vector direction = event.getPlayer().getLocation().toVector().subtract(target.toVector());
                        direction = direction.normalize();
                        target = target.add(direction.multiply(2));
                    }

                    CreatureReleaseEvent creatureReleaseEvent = new CreatureReleaseEvent(event.getPlayer(), target);
                    callEvent(creatureReleaseEvent);
                    if (!creatureReleaseEvent.isCancelled()) {
                        // Release Logic

                        // 1) Spawn creature at target location and cancel event.
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(ChatColor.YELLOW + Main.plugin.getName() + ": " + ChatColor.BLUE + nameOfCreature + " successfully spawned!");
                        CaptureEgg.useSpawnItem(event.getPlayer().getInventory().getItemInMainHand(), target);

                        // 2) Remove itemstack from user, or reduce amount by 1.
                        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                            if (event.getPlayer().getInventory().getItemInMainHand().getAmount() <= 1) {
                                event.getPlayer().getInventory().remove(event.getPlayer().getInventory().getItemInMainHand());
                            }
                            else {
                                int nextAmount = event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1;
                                event.getPlayer().getInventory().getItemInMainHand().setAmount(nextAmount);
                            }
                        }
                    }
                }
                else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
                    //1) Let's prepare to throw the egg. Here we have the unit vector.
                    Vector direction = event.getPlayer().getLocation().getDirection().clone().normalize();

                    //2) Spawn item-drop.
                    final Item item = event.getPlayer().getWorld().dropItem(
                            event.getPlayer().getLocation().clone().add(0, 1, 0),
                            event.getPlayer().getInventory().getItemInMainHand());

                    //3) Prevent pickup, set direction, set velocity
                    item.setPickupDelay(Integer.MAX_VALUE);
                    item.getLocation().setDirection(direction);
                    item.setVelocity(direction.clone().multiply(1.5f));

                    Main.plugin.getServer().getScheduler().runTaskLater(Main.plugin, () -> {
                        Location fixedLocation = new Location(item.getLocation().getWorld(),
                                item.getLocation().getBlockX() + 0.5f,
                                item.getLocation().getBlockY() + 0.5f,
                                item.getLocation().getBlockZ() + 0.5f);
                        CaptureEgg.useSpawnItem(item.getItemStack(), fixedLocation);
                        item.remove();
                    }, 60);

                    // 4) Remove itemstack from user, or reduce amount by 1.
                    if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                        if (event.getPlayer().getInventory().getItemInMainHand().getAmount() <= 1) {
                            event.getPlayer().getInventory().remove(event.getPlayer().getInventory().getItemInMainHand());
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
