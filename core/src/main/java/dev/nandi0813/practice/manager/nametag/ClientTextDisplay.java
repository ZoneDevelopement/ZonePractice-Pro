package dev.nandi0813.practice.manager.nametag;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.util.Vector3f;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class ClientTextDisplay {

    private static final AtomicInteger NEXT_ENTITY_ID = new AtomicInteger(1_500_000_000);
    private static final Vector3f DEFAULT_TRANSLATION = new Vector3f(0.0F, 0.25F, 0.0F);
    private static final Vector3f DEFAULT_SCALE = new Vector3f(1.0F, 1.0F, 1.0F);

    private final UUID ownerUuid;
    private final UUID entityUuid;
    private final int entityId;
    private final Set<UUID> viewers = ConcurrentHashMap.newKeySet();

    private volatile Component text = Component.empty();
    private volatile int background = -1;
    private volatile int textFlags = 0;

    public ClientTextDisplay(Player owner) {
        this.ownerUuid = owner.getUniqueId();
        this.entityUuid = UUID.randomUUID();
        this.entityId = NEXT_ENTITY_ID.incrementAndGet();
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public UUID getEntityUuid() {
        return entityUuid;
    }

    public int getEntityId() {
        return entityId;
    }

    public Component getText() {
        return text;
    }

    public void setText(Component text) {
        this.text = text == null ? Component.empty() : text;
    }

    public void setTextShadow(boolean enabled) {
        setFlag(0x01, enabled);
    }

    public void setSeeThrough(boolean enabled) {
        setFlag(0x02, enabled);
    }

    public void setTextAlignmentCenter() {
        textFlags &= ~(0x08 | 0x10);
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public boolean isViewing(UUID viewerUuid) {
        return viewers.contains(viewerUuid);
    }

    public void addViewer(UUID viewerUuid) {
        viewers.add(viewerUuid);
    }

    public void removeViewer(UUID viewerUuid) {
        viewers.remove(viewerUuid);
    }

    public Set<UUID> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    public Location getSpawnLocation(Player owner) {
        Location location = owner.getLocation();
        location.setYaw(0.0F);
        location.setPitch(0.0F);
        return location;
    }

    public List<EntityData<?>> createMetadata() {
        List<EntityData<?>> data = new java.util.ArrayList<>();
        data.add(new EntityData<>(11, EntityDataTypes.VECTOR3F, DEFAULT_TRANSLATION));
        data.add(new EntityData<>(12, EntityDataTypes.VECTOR3F, DEFAULT_SCALE));
        data.add(new EntityData<>(15, EntityDataTypes.BYTE, (byte) 3));
        data.add(new EntityData<>(23, EntityDataTypes.ADV_COMPONENT, text));

        if (background != -1) {
            data.add(new EntityData<>(25, EntityDataTypes.INT, background));
        }

        if (textFlags != 0) {
            data.add(new EntityData<>(27, EntityDataTypes.BYTE, (byte) textFlags));
        }

        return data;
    }

    private void setFlag(int mask, boolean enabled) {
        if (enabled) {
            textFlags |= mask;
        } else {
            textFlags &= ~mask;
        }
    }
}

