package net.johnbrooks.mh;

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
            Main.economy.withdrawPlayer(player, Settings.costVault);
            return true;
        }
        return false;
    }

    private static boolean chargeItem(Player player)
    {
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack != null && itemStack.getType().name().equalsIgnoreCase(Settings.costMaterial.name()) && itemStack.getAmount() >= Settings.costAmount) {
                if (itemStack.getAmount() == Settings.costAmount)
                    player.getInventory().remove(itemStack);
                else
                    itemStack.setAmount(itemStack.getAmount() - Settings.costAmount);
                return true;
            }
        }
        return false;
    }
}
