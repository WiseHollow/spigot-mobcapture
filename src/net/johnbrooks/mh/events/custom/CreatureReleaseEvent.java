package net.johnbrooks.mh.events.custom;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CreatureReleaseEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private Player user;
    private Location target;

    public CreatureReleaseEvent(Player user, Location target) {
        this.cancelled = false;
        this.user = user;
        this.target = target;
    }

    public Player getUser() {
        return user;
    }

    public Location getTarget() {
        return target;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
