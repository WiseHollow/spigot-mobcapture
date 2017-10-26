package net.johnbrooks.mh.items;

import net.johnbrooks.mh.Main;
import net.johnbrooks.mh.NBTManager;
import net.johnbrooks.mh.Settings;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class CaptureEgg
{
    public static final String TITLE_PREFIX = ChatColor.GOLD + "Captured ";
    public static final Material SPAWNER_TYPE = Material.MONSTER_EGG;

    public static void captureLivingEntity(LivingEntity livingEntity)
    {
        ItemStack eggItemStack = CaptureEgg.get(livingEntity);
        Item drop = livingEntity.getLocation().getWorld().dropItem(livingEntity.getLocation(), eggItemStack);
        drop.setItemStack(eggItemStack);
        drop.setVelocity(new Vector(0, 0.3f, 0));
    }

    public static void useSpawnItem(ItemStack spawnItem, Location target)
    {
        NBTManager.spawnEntityFromNBTData(spawnItem, target);
    }

    public static String getCreatureType(ItemStack spawnItem)
    {
        if (isSpawnEgg(spawnItem))
            return spawnItem.getItemMeta().getDisplayName().substring(TITLE_PREFIX.length(), spawnItem.getItemMeta().getDisplayName().length());
        else
            return null;
    }

    public static boolean isSpawnEgg(ItemStack itemStack)
    {
        if (itemStack != null && itemStack.getType() == SPAWNER_TYPE && itemStack.hasItemMeta())
        {
            ItemMeta meta = itemStack.getItemMeta();
            return meta.hasDisplayName() && meta.getDisplayName().startsWith(TITLE_PREFIX);
        }
        return false;
    }

    private static ItemStack get(LivingEntity livingEntity)
    {
        ItemStack spawnItem = new ItemStack(Material.MONSTER_EGG, 1);
        return NBTManager.castEntityDataToItemStackNBT(spawnItem, livingEntity);
    }
}
