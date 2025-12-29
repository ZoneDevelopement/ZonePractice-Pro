package dev.nandi0813.practice.command.singlecommands;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.type.duel.Duel;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class LeaveCommand extends BukkitCommand {

    public LeaveCommand() {
        super("leave");

        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap map = (CommandMap) field.get(Bukkit.getServer());
            map.register(this.getName(), this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
        }
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (!(commandSender instanceof Player player)) return false;

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) return false;

        if (!profile.getStatus().equals(ProfileStatus.MATCH)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.LEAVE.NOT-IN-MATCH"));
            return false;
        }

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return false;

        if (match instanceof Duel duel) {
            if (!duel.isRanked()) {
                if (!ConfigManager.getBoolean("MATCH-SETTINGS.LEAVE-COMMAND.WEIGHT-CLASS.UNRANKED")) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.LEAVE.CANT-LEAVE-DUEL-UNRANKED"));
                    return false;
                }
            } else {
                if (!ConfigManager.getBoolean("MATCH-SETTINGS.LEAVE-COMMAND.WEIGHT-CLASS.RANKED")) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.LEAVE.CANT-LEAVE-DUEL-RANKED"));
                    return false;
                }
            }
        }

        match.removePlayer(player, true);

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return Collections.emptyList();
    }

}
