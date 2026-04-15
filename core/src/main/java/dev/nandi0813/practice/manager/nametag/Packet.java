package dev.nandi0813.practice.manager.nametag;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public final class Packet {

    private Packet() {
    }

    public static void sendSpawnTextDisplay(Player viewer, int entityId, UUID entityUuid, Location location) {
        Vector3d position = new Vector3d(location.getX(), location.getY(), location.getZ());
        WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(
                entityId,
                Optional.of(entityUuid),
                EntityTypes.TEXT_DISPLAY,
                position,
                location.getPitch(),
                location.getYaw(),
                location.getYaw(),
                0,
                Optional.empty()
        );

        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
    }

    public static void sendMetadataTextDisplay(Player viewer, ClientTextDisplay display) {
        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(display.getEntityId(), display.createMetadata());
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
    }

    public static void sendDestroyTextDisplay(Player viewer, int entityId) {
        WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(entityId);
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
    }

    public static void sendSetPassengers(Player viewer, int vehicleEntityId, int[] passengerEntityIds) {
        WrapperPlayServerSetPassengers packet = new WrapperPlayServerSetPassengers(vehicleEntityId, passengerEntityIds);
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
    }

}
