package dev.nandi0813.practice.manager.nametag;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

public class Packet {

    private final WrapperPlayServerTeams packet;

    public Packet(String name, Component prefix, NamedTextColor namedTextColor, Component suffix, Collection<String> players) {
        WrapperPlayServerTeams.ScoreBoardTeamInfo scoreBoardTeamInfo = new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                serialize(name),
                prefix,
                suffix,
                WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
                WrapperPlayServerTeams.CollisionRule.ALWAYS,
                namedTextColor,
                WrapperPlayServerTeams.OptionData.ALL
        );

        this.packet = new WrapperPlayServerTeams(
                name,
                WrapperPlayServerTeams.TeamMode.CREATE,
                scoreBoardTeamInfo,
                players);
    }

    public Packet(String name) {
        this.packet = new WrapperPlayServerTeams(
                name,
                WrapperPlayServerTeams.TeamMode.REMOVE,
                (WrapperPlayServerTeams.ScoreBoardTeamInfo) null
        );
    }

    public Packet(String name, Collection<String> players, WrapperPlayServerTeams.TeamMode teamMode) {
        this.packet = new WrapperPlayServerTeams(
                name,
                teamMode,
                (WrapperPlayServerTeams.ScoreBoardTeamInfo) null,
                players
        );
    }

    public void send(Player player) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    public void send() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            send(player);
        }
    }

    private static Component serialize(String normalString) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(normalString);
    }

}
