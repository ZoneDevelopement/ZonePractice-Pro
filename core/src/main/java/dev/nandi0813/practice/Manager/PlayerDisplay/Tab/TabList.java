package dev.nandi0813.practice.Manager.PlayerDisplay.Tab;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter;
import dev.nandi0813.practice.Util.PAPIUtil;
import org.bukkit.entity.Player;

public class TabList {

    private final Player player;

    public TabList(Player player) {
        this.player = player;
        this.setTab();
    }

    public void setTab() {
        WrapperPlayServerPlayerListHeaderAndFooter packet
                = new WrapperPlayServerPlayerListHeaderAndFooter(
                PAPIUtil.runThroughFormat(player, TabListManager.HEADER_TEXT),
                PAPIUtil.runThroughFormat(player, TabListManager.FOOTER_TEXT)
        );

        User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        user.sendPacket(packet);
    }

}
