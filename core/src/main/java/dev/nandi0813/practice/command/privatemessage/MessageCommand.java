package dev.nandi0813.practice.command.privatemessage;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageCommand extends PrivateMessageCommand {

    @Getter
    public static HashMap<Player, Player> latestMessage = new HashMap<>();

    public MessageCommand() {
        super("message", new String[]{"m", "msg"});
    }

    @Override
    public void executeCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Common.sendConsoleMMMessage(LanguageManager.getString("CANT-USE-CONSOLE"));
            return;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (args.length < 2) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRIVATE-MESSAGE.MESSAGE.COMMAND-HELP").replaceAll("%label%", label));
            return;
        }

        if (!profile.isPrivateMessages()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRIVATE-MESSAGE.MESSAGE.CANT-SEND"));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRIVATE-MESSAGE.MESSAGE.TARGET-OFFLINE").replaceAll("%target%", args[0]));
            return;
        }

        Profile targetProfile = ProfileManager.getInstance().getProfile(target);
        if (player != target && targetProfile.isHideFromPlayers() && !player.hasPermission("zpp.staffmode.see")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRIVATE-MESSAGE.MESSAGE.TARGET-HIDE").replaceAll("%target%", target.getName()));
            return;
        }

        if (!targetProfile.isPrivateMessages() && !player.hasPermission("zpp.bypass.privatemessage")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRIVATE-MESSAGE.MESSAGE.CANT-SEND2").replaceAll("%target%", target.getName()));
            return;
        }

        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++)
            message.append(args[i]).append(" ");

        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRIVATE-MESSAGE.MESSAGE.SENDER-MESSAGE")
                .replaceAll("%sender%", player.getName())
                .replaceAll("%receiver%", target.getName())
                .replaceAll("%message%", message.toString())
        );
        Common.sendMMMessage(target, LanguageManager.getString("COMMAND.PRIVATE-MESSAGE.MESSAGE.RECEIVER-MESSAGE")
                .replaceAll("%sender%", player.getName())
                .replaceAll("%receiver%", target.getName())
                .replaceAll("%message%", message.toString())
        );

        latestMessage.put(target, player);
        latestMessage.put(player, target);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> results = new ArrayList<>();
        if (!(sender instanceof Player player)) return results;

        if (args.length == 1) {
            List<String> onlineNames = new ArrayList<>();

            for (Player online : Bukkit.getOnlinePlayers()) {
                Profile onlineProfile = ProfileManager.getInstance().getProfile(online);
                if (player != online && (!onlineProfile.isHideFromPlayers() || player.hasPermission("zpp.staffmode.see")) && (onlineProfile.isPrivateMessages() || player.hasPermission("zpp.bypass.privatemessage")))
                    onlineNames.add(online.getName());
            }

            return org.bukkit.util.StringUtil.copyPartialMatches(args[0], onlineNames, new ArrayList<>());
        }

        return results;
    }

}
