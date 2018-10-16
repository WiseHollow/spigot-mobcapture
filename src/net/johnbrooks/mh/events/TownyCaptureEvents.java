package net.johnbrooks.mh.events;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import net.johnbrooks.mh.Language;
import net.johnbrooks.mh.Settings;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Optional;

public class TownyCaptureEvents implements Listener {

    @EventHandler
    public void townyCheck(CreatureCaptureEvent event) {
        if (!event.isCancelled()) {
            if (Settings.townyHook) {
                String worldName = event.getTargetEntity().getWorld().getName();
                int x = event.getTargetEntity().getLocation().getChunk().getX();
                int z = event.getTargetEntity().getLocation().getChunk().getZ();
                Optional<Town> optionalTown = TownyUniverse.getDataSource().getTowns().stream()
                        .filter(town.getWorld() != null &&
                                town -> town.getWorld().getName().equalsIgnoreCase(worldName) &&
                                town.getTownBlocks().stream().anyMatch(mTownBlock -> mTownBlock.getX() == x && mTownBlock.getZ() == z)).findFirst();
                if (optionalTown.isPresent()) {
                    Town town = optionalTown.get();
                    if (!town.hasResident(event.getCaptor().getName())) {
                        event.getCaptor().sendMessage(Language.PREFIX + "You do not have permission to capture creatures here.");
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
}
