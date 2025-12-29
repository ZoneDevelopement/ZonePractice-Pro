package dev.nandi0813.practice.command.privatemessage;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ReplyCommand extends PrivateMessageCommand {

    public ReplyCommand() {
        super("reply", new String[]{"r", "re"});
    }

    @Override
    public void executeCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Common.sendConsoleMMMessage(LanguageManager.getString("CANT-USE-CONSOLE"));
            return;
        }

        if (args.length < 1) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRIVATE-MESSAGE.REPLY.COMMAND-HELP").replaceAll("%label%", label));
            return;
        }

        if (!MessageCommand.getLatestMessage().containsKey(player)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRIVATE-MESSAGE.REPLY.NO-MESSAGE"));
            return;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (!profile.isPrivateMessages()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRIVATE-MESSAGE.REPLY.CANT-REPLY"));
            return;
        }

        Player target = MessageCommand.getLatestMessage().get(player);
        if (!target.isOnline()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRIVATE-MESSAGE.REPLY.PLAYER-LEFT").replaceAll("%target%", target.getName()));
            return;
        }

        Profile targetProfile = ProfileManager.getInstance().getProfile(target);
        if (!targetProfile.isPrivateMessages() && !player.hasPermission("zpp.bypass.privatemessage")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRIVATE-MESSAGE.REPLY.CANT-REPLY2").replaceAll("%target%", target.getName()));
            return;
        }

        StringBuilder message = new StringBuilder();
        for (String arg : args)
            message.append(arg).append(" ");

        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRIVATE-MESSAGE.REPLY.SENDER-MESSAGE")
                .replaceAll("%sender%", player.getName())
                .replaceAll("%receiver%", target.getName())
                .replaceAll("%message%", message.toString())
        );
        Common.sendMMMessage(target, LanguageManager.getString("COMMAND.PRIVATE-MESSAGE.REPLY.RECEIVER-MESSAGE")
                .replaceAll("%sender%", player.getName())
                .replaceAll("%receiver%", target.getName())
                .replaceAll("%message%", message.toString())
        );

        MessageCommand.getLatestMessage().put(target, player);
        MessageCommand.getLatestMessage().put(player, target);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

}
