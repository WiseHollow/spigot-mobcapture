package net.johnbrooks.mh.items;

import net.johnbrooks.mh.Settings;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class UniqueProjectileData {

    private static boolean enabled;
    private static String displayName;
    private static List<String> lore;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        UniqueProjectileData.enabled = enabled;
    }

    public static String getDisplayName() {
        return displayName;
    }

    public static void setDisplayName(String displayName) {
        UniqueProjectileData.displayName = ChatColor.translateAlternateColorCodes('&', displayName);
    }

    public static List<String> getLore() {
        return lore;
    }

    public static void setLore(List<String> lore) {
        UniqueProjectileData.lore = new ArrayList<>(lore.size());
        lore.forEach(line -> UniqueProjectileData.lore.add(ChatColor.translateAlternateColorCodes('&', line)));
    }

    public static ItemStack spawn() {
        ItemStack itemStack = new ItemStack(Settings.projectileCatcherMaterial, 1);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(displayName);
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public static boolean isProjectile(ItemStack projectile) {
        if (projectile != null && projectile.hasItemMeta()
                && projectile.getItemMeta().hasDisplayName()
                && projectile.getItemMeta().hasLore()) {
            ItemMeta meta = projectile.getItemMeta();
            return (meta.getDisplayName().equalsIgnoreCase(displayName));
        }

        return false;
    }
}
