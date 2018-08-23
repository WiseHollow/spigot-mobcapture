package net.johnbrooks.mh;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EconomyManager {

    public static boolean chargePlayer(Player player) {
        switch (Settings.costMode) {
            case ITEM:
                return chargeItem(player);
            case VAULT:
                return chargeVault(player);
            default:
                return false;
        }
    }

    private static boolean chargeVault(Player player) {
        if (Main.economy.getBalance(player) >= Settings.costVault) {
            if (Settings.costVault > 0) {
                player.sendMessage(Language.PREFIX + "[Wallet Charged: $" + Settings.costVault + "]");
                Main.economy.withdrawPlayer(player, Settings.costVault);
            }
            return true;
        }
        return false;
    }

    private static boolean chargeItem(Player player)
    {
        for (int i = 0; i < player.getInventory().getContents().length; i++) {
            ItemStack itemStack = player.getInventory().getContents()[i];
            if (itemStack != null && itemStack.getType().name().equalsIgnoreCase(Settings.costMaterial.name()) && itemStack.getAmount() >= Settings.costAmount) {
                if (itemStack.getAmount() == Settings.costAmount)
                    player.getInventory().setItem(i, new ItemStack(Material.AIR));
                else
                    itemStack.setAmount(itemStack.getAmount() - Settings.costAmount);
                if (Settings.costAmount > 0) {
                    String capitalizedMaterial = Settings.costMaterial.name().substring(0, 1) + Settings.costMaterial.name().substring(1).toLowerCase().replace("_", " ");
                    player.sendMessage(Language.PREFIX + "[" + capitalizedMaterial + " Charged: " + Settings.costAmount + "]");
                }
                return true;
            }
        }
        return false;
    }
}
