package dev.nandi0813.practice.Module.Interfaces;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public abstract class EntityHider implements Listener {

    protected Table<Integer, Integer, Boolean> observerEntityMap = HashBasedTable.create();

    public enum Policy {
        /**
         * All entities are invisible by default. Only entities specifically made
         * visible may be seen.
         */
        WHITELIST,

        /**
         * All entities are visible by default. An entity can only be hidden explicitly.
         */
        BLACKLIST,
    }

    protected Plugin plugin;

    // Current policy
    protected final EntityHider.Policy policy;

    public EntityHider(Plugin plugin, EntityHider.Policy policy) {
        Preconditions.checkNotNull(plugin, "plugin cannot be NULL.");

        // Save policy
        this.policy = policy;
        this.plugin = plugin;

        PacketEvents.getAPI().getEventManager().registerListener(constructProtocol(), PacketListenerPriority.NORMAL);

        // Register events and packet listener
        plugin.getServer().getPluginManager().registerEvents(constructBukkit(), plugin);
    }

    /**
     * Set the visibility status of a given entity for a particular observer.
     *
     * @param observer - the observer player.
     * @param visible  - TRUE if the entity should be made visible, FALSE if not.
     * @return TRUE if the entity was visible before this method calls, FALSE
     * otherwise.
     */
    protected boolean setVisibility(Player observer, int entityID, boolean visible) {
        return switch (policy) {
            case BLACKLIST -> !setMembership(observer, entityID, !visible);
            case WHITELIST -> setMembership(observer, entityID, visible);
        };
    }

    /**
     * Add or remove the given entity and observer entry from the table.
     *
     * @param observer - the player observer.
     * @param entityID - ID of the entity.
     * @param member   - TRUE if they should be present in the table, FALSE
     *                 otherwise.
     * @return TRUE if they already were present, FALSE otherwise.
     */
    // Helper method
    protected boolean setMembership(Player observer, int entityID, boolean member) {
        if (member)
            return observerEntityMap.put(observer.getEntityId(), entityID, true) != null;
        else
            return observerEntityMap.remove(observer.getEntityId(), entityID) != null;
    }

    /**
     * Determine if the given entity and observer is present in the table.
     *
     * @param observer - the player observer.
     * @param entityID - ID of the entity.
     * @return TRUE if they are present, FALSE otherwise.
     */
    protected boolean getMembership(Player observer, int entityID) {
        return observerEntityMap.contains(observer.getEntityId(), entityID);
    }

    /**
     * Determine if a given entity is visible for a particular observer.
     *
     * @param observer - the observer player.
     * @param entityID - ID of the entity that we are testing for visibility.
     * @return TRUE if the entity is visible, FALSE otherwise.
     */
    protected boolean isVisible(Player observer, int entityID) {
        // If we are using a whitelist, presence means visibility - if not, the opposite
        // is the case
        boolean presence = getMembership(observer, entityID);

        return (policy == EntityHider.Policy.WHITELIST) == presence;
    }

    /**
     * Remove the given entity from the underlying map.
     *
     * @param entity - the entity to remove.
     */
    protected void removeEntity(Entity entity) {
        int entityID = entity.getEntityId();

        for (Map<Integer, Boolean> maps : observerEntityMap.rowMap().values()) {
            maps.remove(entityID);
        }
    }

    /**
     * Invoked when a player logs out.
     *
     * @param player - the player that jused logged out.
     */
    protected void removePlayer(Player player) {
        // Cleanup
        observerEntityMap.rowMap().remove(player.getEntityId());
    }

    /**
     * Construct the Bukkit event listener.
     *
     * @return Our listener.
     */
    private Listener constructBukkit() {
        return new Listener() {
            @EventHandler
            public void onEntityDeath(EntityDeathEvent e) {
                removeEntity(e.getEntity());
            }

            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent e) {
                removePlayer(e.getPlayer());
            }
        };
    }

    /**
     * Construct the packet listener that will be used to intercept every
     * entity-related packet.
     *
     * @return The packet listener.
     */
    private PacketListener constructProtocol() {
        return new PacketListener() {
            @Override
            public void onPacketSend(@NonNull PacketSendEvent event) {
                PacketListener.super.onPacketSend(event);

                int entityID = event.getPacketId();

                if (!isVisible(event.getPlayer(), entityID)) {
                    event.setCancelled(true);
                }
            }
        };
    }

    /**
     * Allow the observer to see an entity that was previously hidden.
     *
     * @param observer - the observer.
     * @param entity   - the entity to show.
     */
    public abstract void showEntity(Player observer, Entity entity);

    /**
     * Prevent the observer from seeing a given entity.
     *
     * @param observer - the player observer.
     * @param entity   - the entity to hide.
     */
    public abstract void hideEntity(Player observer, Entity entity);

    /**
     * Determine if the given entity has been hidden from an observer.
     * <p>
     * Note that the entity may very well be occluded or out of range from the
     * perspective of the observer. This method simply checks if an entity has been
     * completely hidden for that observer.
     *
     * @param observer - the observer.
     * @param entity   - the entity that may be hidden.
     * @return TRUE if the player may see the entity, FALSE if the entity has been
     * hidden.
     */
    public final boolean canSee(Player observer, Entity entity) {
        validate(observer, entity);

        return isVisible(observer, entity.getEntityId());
    }

    // For valdiating the input parameters
    protected void validate(Player observer, Entity entity) {
        Preconditions.checkNotNull(observer, "observer cannot be NULL.");
        Preconditions.checkNotNull(entity, "entity cannot be NULL.");
    }

}
