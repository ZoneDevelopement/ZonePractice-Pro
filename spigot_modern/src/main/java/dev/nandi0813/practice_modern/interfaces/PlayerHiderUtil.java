package dev.nandi0813.practice_modern.interfaces;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.module.interfaces.PlayerHiderInterface;
import org.bukkit.entity.Player;

public class PlayerHiderUtil implements PlayerHiderInterface {

    @Override
    public void hidePlayer(Player observer, Player target, boolean fullHide) {
        if (observer.canSee(target))
            observer.hidePlayer(ZonePractice.getInstance(), target);

        if (!fullHide) {
            WrapperPlayServerPlayerInfoUpdate playerInfoUpdate = new WrapperPlayServerPlayerInfoUpdate(
                    WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED,
                    new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(target.getUniqueId())
            );

            PacketEvents.getAPI().getPlayerManager().sendPacket(observer, playerInfoUpdate);
        }
    }

    @Override
    public void showPlayer(Player observer, Player target) {
        observer.showPlayer(ZonePractice.getInstance(), target);
    }

}

