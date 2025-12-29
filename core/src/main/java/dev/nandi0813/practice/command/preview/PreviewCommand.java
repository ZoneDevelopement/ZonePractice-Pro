package dev.nandi0813.practice.command.preview;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.util.Common;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PreviewCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Common.sendConsoleMMMessage(LanguageManager.getString("CANT-USE-CONSOLE"));
            return false;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);

        switch (profile.getStatus()) {
            case MATCH:
            case FFA:
            case EVENT:
            case SPECTATE:
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PREVIEW.CANT-USE"));
                return false;
        }

        if (args.length != 1) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PREVIEW.COMMAND-HELP").replaceAll("%label%", label));
            return false;
        }

        NormalLadder ladder = LadderManager.getInstance().getLadder(args[0]);
        if (ladder == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PREVIEW.LADDER-NOT-EXISTS").replaceAll("%ladder%", args[0]));
            return false;
        }

        if (!ladder.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PREVIEW.LADDER-DISABLED").replaceAll("%ladder%", ladder.getDisplayName()));
            return false;
        }

        ladder.getPreviewGui().open(player);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> arguments = new ArrayList<>();
        List<String> completion = new ArrayList<>();
        if (!(sender instanceof Player player)) return arguments;

        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile.getStatus().equals(ProfileStatus.MATCH) || profile.getStatus().equals(ProfileStatus.SPECTATE))
            return arguments;

        if (args.length == 1) {
            for (Ladder ladder : LadderManager.getInstance().getLadders()) {
                if (ladder.isEnabled())
                    arguments.add(ladder.getName());
            }

            StringUtil.copyPartialMatches(args[0], arguments, completion);
        }

        Collections.sort(completion);
        return completion;
    }

}
