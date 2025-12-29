package dev.nandi0813.practice_1_8_8.interfaces;

import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.util.StringUtil;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ActionBar extends dev.nandi0813.practice.module.interfaces.actionbar.ActionBar {

    public ActionBar(Profile profile) {
        super(profile);
    }

    @Override
    public void send() {
        Player player = profile.getPlayer().getPlayer();

        if (player != null) {
            sendActionText(player, StringUtil.CC(LegacyComponentSerializer.legacyAmpersand().serialize(this.message)));
        }
    }

    @Override
    public void clear() {
        Player player = profile.getPlayer().getPlayer();

        if (player != null) {
            sendActionText(player, "");
        }
    }

    public void sendActionText(Player player, String message) {
        PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(message), (byte) 2);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

}
