package dev.nandi0813.practice_1_8_8.interfaces;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class EntityHider extends dev.nandi0813.practice.module.interfaces.EntityHider implements Listener {

    public EntityHider(Plugin plugin, Policy policy) {
        super(plugin, policy);
    }

    @Override
    public void showEntity(Player observer, Entity entity) {
        validate(observer, entity);
        boolean hiddenBefore = !setVisibility(observer, entity.getEntityId(), true);

        // Resend packets
        if (hiddenBefore) {
            WrapperPlayServerSpawnEntity spawnEntity = new WrapperPlayServerSpawnEntity(
                    entity.getEntityId(),
                    entity.getUniqueId(),
                    SpigotConversionUtil.fromBukkitEntityType(entity.getType()),
                    SpigotConversionUtil.fromBukkitLocation(entity.getLocation()),
                    entity.getLocation().getYaw(),
                    0,
                    new Vector3d(entity.getVelocity().getX(), entity.getVelocity().getY(), entity.getVelocity().getZ())
            );

            PacketEvents.getAPI().getPlayerManager().sendPacket(observer, spawnEntity);
        }
    }

    @Override
    public void hideEntity(Player observer, Entity entity) {
        validate(observer, entity);
        boolean visibleBefore = setVisibility(observer, entity.getEntityId(), false);

        if (visibleBefore) {
            WrapperPlayServerDestroyEntities destroyEntities = new WrapperPlayServerDestroyEntities(new int[]{entity.getEntityId()});
            PacketEvents.getAPI().getPlayerManager().sendPacket(observer, destroyEntities);
        }
    }

}
