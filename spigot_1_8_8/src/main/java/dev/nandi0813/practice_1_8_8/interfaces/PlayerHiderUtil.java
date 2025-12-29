package dev.nandi0813.practice_1_8_8.interfaces;

import dev.nandi0813.practice.module.interfaces.PlayerHiderInterface;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PlayerHiderUtil implements PlayerHiderInterface {

    @Override
    public void hidePlayer(Player observer, Player target, boolean fullHide) {
        if (observer.canSee(target))
            observer.hidePlayer(target);

        EntityPlayer entityTarget = ((CraftPlayer) target).getHandle();

        PacketPlayOutPlayerInfo packet;
        if (!fullHide) {
            packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityTarget);
        } else {
            packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityTarget);
        }
        ((CraftPlayer) observer).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public void showPlayer(Player observer, Player target) {
        observer.showPlayer(target);
    }

}
