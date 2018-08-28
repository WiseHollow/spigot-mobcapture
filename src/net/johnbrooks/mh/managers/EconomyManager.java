package net.johnbrooks.mh.managers;

import net.johnbrooks.mh.Language;
import net.johnbrooks.mh.Main;
import net.johnbrooks.mh.Settings;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Optional;

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

    private static boolean chargeItem(Player player) {
        Optional<ItemStack> itemStackOptional = Arrays.stream(player.getInventory().getContents())
                .filter(itemStack -> itemStack != null && itemStack.getType().equals(Settings.costMaterial) && itemStack.getAmount() >= Settings.costAmount)
                .findAny();

        if (itemStackOptional.isPresent()) {
            ItemStack itemStack = itemStackOptional.get();
            itemStack.setAmount(itemStack.getAmount() - Settings.costAmount);

            String capitalizedMaterial = Settings.costMaterial.name().substring(0, 1) + Settings.costMaterial.name().substring(1).toLowerCase().replace("_", " ");
            if (Settings.costAmount > 0) {
                player.sendMessage(Language.PREFIX + "[" + capitalizedMaterial + " Charged: " + Settings.costAmount + "]");
            }
            return true;
        }

        return false;
    }
}
