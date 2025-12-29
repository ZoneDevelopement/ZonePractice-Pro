package dev.nandi0813.practice.command.spectate;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.fight.ffa.FFAManager;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
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

public class SpectateCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Common.sendConsoleMMMessage(LanguageManager.getString("CANT-USE-CONSOLE"));
            return false;
        }

        if (!player.hasPermission("zpp.spectate")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SPECTATE.NO-SPECTATE"));
            return false;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if ((!profile.getStatus().equals(ProfileStatus.LOBBY) && !profile.getStatus().equals(ProfileStatus.SPECTATE)) || profile.isParty() || profile.isStaffMode()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SPECTATE.CANT-SPECTATE"));
            return false;
        }

        if (args.length != 1) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SPECTATE.COMMAND-HELP").replaceAll("%label%", label));
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SPECTATE.TARGET-OFFLINE").replaceAll("%target%", args[0]));
            return false;
        }

        if (target == player) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SPECTATE.CANT-SPECTATE2"));
            return false;
        }

        Profile targetProfile = ProfileManager.getInstance().getProfile(target);
        if (targetProfile.getStatus().equals(ProfileStatus.MATCH)) {
            Match match = MatchManager.getInstance().getLiveMatchByPlayer(target);

            if (match != null)
                match.addSpectator(player, target, true, true);
        } else if (targetProfile.getStatus().equals(ProfileStatus.EVENT)) {
            Event event = EventManager.getInstance().getEventByPlayer(target);

            if (event != null)
                event.addSpectator(player, target, true, true);
        } else if (targetProfile.getStatus().equals(ProfileStatus.FFA)) {
            FFA ffa = FFAManager.getInstance().getFFAByPlayer(target);

            if (ffa != null)
                ffa.addSpectator(player, target, true, true);
        } else
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SPECTATE.PLAYER-INACTIVE"));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> arguments = new ArrayList<>();
        List<String> completion = new ArrayList<>();
        if (!(sender instanceof Player player)) return arguments;
        if (!player.hasPermission("zpp.spectate")) return arguments;

        if (args.length == 1) {
            for (Player online : Bukkit.getOnlinePlayers())
                if (player != online) arguments.add(online.getName());

            StringUtil.copyPartialMatches(args[0], arguments, completion);
        }

        Collections.sort(completion);
        return completion;
    }

}
