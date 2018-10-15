package net.johnbrooks.mh;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EconomyManager {

    public static void chargePlayer(Player player) {
        switch (Settings.costMode) {
            case ITEM:
                chargeItem(player);
                String capitalizedMaterial = Settings.costMaterial.name().substring(0, 1) + Settings.costMaterial.name().substring(1).toLowerCase().replace("_", " ");
                player.sendMessage(Language.PREFIX + "[" + capitalizedMaterial + " Charged: " + Settings.costAmount + "]");
                break;
            case VAULT:
                chargeVault(player);
                break;
            case BOTH:
                chargeItem(player);
                chargeVault(player);
                break;
            default:
            	break;
        }
        
    }
    public static boolean canChargePlayer(Player player) {
        switch (Settings.costMode) {
            case ITEM:
                return canChargeItem(player);
            case VAULT:
                return canChargeVault(player);
            case BOTH:
                return canChargeItem(player) && canChargeVault(player);
            default:
                return false;
        }
    }

    private static void chargeVault(Player player) {
    	player.sendMessage(Language.PREFIX + "[Wallet Charged: $" + Settings.costVault + "]");
    	Main.economy.withdrawPlayer(player, Settings.costVault);
    }
    private static boolean canChargeVault(Player player) {
        return (Main.economy.getBalance(player) >= Settings.costVault && Settings.costVault > 0);
    }

    private static void chargeItem(Player player) {
    	int req = Settings.costAmount;
        for (int i = 0; i < player.getInventory().getContents().length; i++) {
            ItemStack itemStack = player.getInventory().getContents()[i];
            if (itemStack != null && itemStack.getType().name().equalsIgnoreCase(Settings.costMaterial.name())) {
            	int current = itemStack.getAmount();
                if (current == req) {
                	player.getInventory().setItem(i, new ItemStack(Material.AIR));
                	return;
                }
                if (current > req) {
                	itemStack.setAmount(current - req);
                	return;
                }
                if (current < req) {
                	player.getInventory().setItem(i, new ItemStack(Material.AIR));
                	req -= current;
                }
            }
        }
    }
    private static boolean canChargeItem(Player player) {
    	int count = 0;
        for (int i = 0; i < player.getInventory().getContents().length; i++) {
            ItemStack itemStack = player.getInventory().getContents()[i];
            if (itemStack != null && itemStack.getType().name().equalsIgnoreCase(Settings.costMaterial.name())) {
                count +=itemStack.getAmount();
                if (count >= Settings.costAmount)
                	return true;
            }
        }
        return false;
    }
}
