package net.johnbrooks.mh.events.custom;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

public class CreatureCaptureEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private Player captor;
    private LivingEntity targetEntity;

    public CreatureCaptureEvent(Player captor, LivingEntity targetEntity) {
        this.cancelled = false;
        this.captor = captor;
        this.targetEntity = targetEntity;
    }

    public Player getCaptor() {
        return captor;
    }

    public LivingEntity getTargetEntity() {
        return targetEntity;
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
