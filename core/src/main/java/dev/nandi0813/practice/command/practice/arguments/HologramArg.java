package dev.nandi0813.practice.command.practice.arguments;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.hologram.HologramSetupManager;
import dev.nandi0813.practice.manager.leaderboard.hologram.Hologram;
import dev.nandi0813.practice.manager.leaderboard.hologram.HologramManager;
import dev.nandi0813.practice.manager.leaderboard.hologram.HologramType;
import dev.nandi0813.practice.manager.leaderboard.hologram.holograms.GlobalHologram;
import dev.nandi0813.practice.manager.leaderboard.hologram.holograms.LadderDynamicHologram;
import dev.nandi0813.practice.manager.leaderboard.hologram.holograms.LadderStaticHologram;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.manager.server.WorldEnum;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum HologramArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.setup")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
            return;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        switch (profile.getStatus()) {
            case MATCH:
            case FFA:
            case EVENT:
            case SPECTATE:
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.HOLOGRAM.CANT-USE"));
                return;
        }

        if (ServerManager.getLobby() == null) {
            Common.sendMMMessage(player, LanguageManager.getString("SET-SERVER-LOBBY"));
            return;
        }

        if (!ServerManager.getInstance().getInWorld().containsKey(player) || !ServerManager.getInstance().getInWorld().get(player).equals(WorldEnum.LOBBY)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.HOLOGRAM.ONLY-IN-LOBBY"));
            return;
        }

        if (args.length != 4 || !args[1].equalsIgnoreCase("create")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.HOLOGRAM.COMMAND-HELP").replace("%label%", label + " hologram"));
            return;
        }

        HologramType hologramType;
        switch (args[3].toLowerCase()) {
            case "global":
                hologramType = HologramType.GLOBAL;
                break;
            case "ladder_static":
                hologramType = HologramType.LADDER_STATIC;
                break;
            case "ladder_dynamic":
                hologramType = HologramType.LADDER_DYNAMIC;
                break;
            default:
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.HOLOGRAM.INVALID-TYPE"));
                return;
        }

        if (HologramManager.getInstance().getHolograms().size() >= 18) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.HOLOGRAM.REACHED-MAX"));
            return;
        }

        String name = args[2];
        if (HologramManager.getInstance().getHologram(name) != null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.HOLOGRAM.HOLO-EXISTS"));
            return;
        }

        if (name.length() > 9) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.HOLOGRAM.CREATE-ERROR"));
            return;
        }

        Hologram hologram = switch (hologramType) {
            case GLOBAL -> new GlobalHologram(name, player.getLocation());
            case LADDER_STATIC -> new LadderStaticHologram(name, player.getLocation());
            case LADDER_DYNAMIC -> new LadderDynamicHologram(name, player.getLocation());
        };

        HologramManager.getInstance().createHologram(hologram);

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
                HologramSetupManager.getInstance().getHologramSetupGUIs().get(hologram).get(GUIType.Hologram_Main).open(player), 3L);

        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.HOLOGRAM.CREATE-SUCCESS").replace("%hologram%", hologram.getName()));
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();

        if (!player.hasPermission("zpp.setup")) {
            return arguments;
        }

        if (args.length == 2) {
            arguments.add("create");
            return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
        }

        if (args.length == 4 && args[1].equalsIgnoreCase("create")) {
            arguments.add("global");
            arguments.add("ladder_static");
            arguments.add("ladder_dynamic");
            return StringUtil.copyPartialMatches(args[3], arguments, new ArrayList<>());
        }

        return arguments;
    }
}

