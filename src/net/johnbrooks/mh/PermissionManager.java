package net.johnbrooks.mh;

import org.bukkit.entity.*;

public class PermissionManager {
    public final String NoCost = Main.plugin.getName() + ".NoCost";
    public final String CatchPrefix = Main.plugin.getName() + ".Catch.";
    public final String CatchPeaceful = "MobCapture.Catch.Peaceful";
    public final String CatchHostile = "MobCapture.Catch.Hostile";

    public boolean hasPermissionToCapture(Player player, LivingEntity livingEntity) {
        if (livingEntity instanceof Animals && player.hasPermission(CatchPeaceful))
            return true;
        else if (livingEntity instanceof Monster && player.hasPermission(CatchHostile))
            return true;
        else if (livingEntity instanceof WaterMob && player.hasPermission(CatchPeaceful))
            return true;
        else
            return player.hasPermission(CatchPrefix + livingEntity.getType().name());
    }
}
