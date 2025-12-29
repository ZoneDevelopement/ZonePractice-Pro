package dev.nandi0813.practice.command.duel;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.duel.DuelManager;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.gui.guis.selectors.LadderSelectorGui;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DuelCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Common.sendConsoleMMMessage(LanguageManager.getString("CANT-USE-CONSOLE"));
            return false;
        }

        if (!player.hasPermission("zpp.duel")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.DUEL.NO-PERMISSION"));
            return false;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (args.length != 1) {
            Common.sendMMMessage(player, LanguageManager.getString("command.duel.command-help").replace("%label%", label));
            return false;
        }

        if (!profile.getStatus().equals(ProfileStatus.LOBBY) || profile.isParty()) {
            Common.sendMMMessage(player, LanguageManager.getString("command.duel.cant-duel"));
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        Profile targetProfile = ProfileManager.getInstance().getProfile(target);
        if (target == null || targetProfile == null) {
            Common.sendMMMessage(player, LanguageManager.getString("command.duel.player-offline").replace("%target%", args[0]));
            return false;
        }

        if (target.equals(player)) {
            Common.sendMMMessage(player, LanguageManager.getString("command.duel.self-duel"));
            return false;
        }

        if (DuelManager.getInstance().isRequested(player, target) && !player.hasPermission("zpp.duel.infiniteinvite")) {
            Common.sendMMMessage(player, LanguageManager.getString("command.duel.already-invited").replaceAll("%target%", target.getName()));
            return false;
        }

        if (targetProfile.isParty()) {
            Common.sendMMMessage(player, LanguageManager.getString("command.duel.player-cant-duel").replace("%target%", target.getName()));
            return false;
        }

        if (!targetProfile.getStatus().equals(ProfileStatus.LOBBY) && !targetProfile.getStatus().equals(ProfileStatus.EDITOR) && !targetProfile.getStatus().equals(ProfileStatus.SPECTATE)) {
            Common.sendMMMessage(player, LanguageManager.getString("command.duel.player-cant-duel").replace("%target%", target.getName()));
            return false;
        }

        if (!targetProfile.isDuelRequest()) {
            Common.sendMMMessage(player, LanguageManager.getString("command.duel.player-not-receive").replace("%target%", target.getName()));
            return false;
        }

        DuelManager.getInstance().getPendingRequestTarget().put(player, target);
        new LadderSelectorGui(profile, MatchType.DUEL).open(player);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> arguments = new ArrayList<>();
        List<String> completion = new ArrayList<>();
        if (!(sender instanceof Player player)) return arguments;

        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (!profile.getStatus().equals(ProfileStatus.LOBBY) || profile.isParty()) return arguments;

        if (args.length == 1) {
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (player.equals(target)) continue;

                arguments.add(target.getName());
            }

            StringUtil.copyPartialMatches(args[0], arguments, completion);
        }

        Collections.sort(completion);
        return completion;
    }

}
