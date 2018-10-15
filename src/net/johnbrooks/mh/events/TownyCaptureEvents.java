package net.johnbrooks.mh.events;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import net.johnbrooks.mh.Language;
import net.johnbrooks.mh.Main;
import net.johnbrooks.mh.Settings;

public class TownyCaptureEvents implements Listener {

	@EventHandler (priority = EventPriority.LOWEST)
    public void townyCheck(CreatureCaptureEvent event) {
        if (!event.isCancelled()) {
            if (Settings.townyHook) {
                String worldName = event.getTargetEntity().getWorld().getName();
                int x = event.getTargetEntity().getLocation().getChunk().getX();
                int z = event.getTargetEntity().getLocation().getChunk().getZ();
                
                Iterator<Town> town_itt = TownyUniverse.getDataSource().getTowns().iterator();
                ArrayList<Town> filtered = new ArrayList<Town>();
                while (town_itt.hasNext()) {
                	Town town = town_itt.next();
                	if (town == null || town.getWorld() == null)
                		continue;
                	Main.logger.log(Level.INFO, "TOWN: ", town.getName());
                	if (town.getWorld().getName().equalsIgnoreCase(worldName)) {
                		List<TownBlock> blocks = town.getTownBlocks();
                		Iterator<TownBlock> block_itt = blocks.iterator();
                		while (block_itt.hasNext()) {
                			TownBlock block = block_itt.next();
                			if (block.getX() == x && block.getZ() == z) {
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
        }
    }

}
