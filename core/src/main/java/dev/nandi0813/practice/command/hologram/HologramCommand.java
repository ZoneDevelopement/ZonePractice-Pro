package dev.nandi0813.practice.command.hologram;

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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HologramCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Common.sendConsoleMMMessage(LanguageManager.getString("CANT-USE-CONSOLE"));
            return false;
        }

        if (!player.hasPermission("zpp.setup")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.HOLOGRAM.NO-PERMISSION"));
            return false;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        switch (profile.getStatus()) {
            case MATCH:
            case FFA:
            case EVENT:
            case SPECTATE:
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.HOLOGRAM.CANT-USE"));
                return false;
        }

        if (ServerManager.getLobby() == null) {
            Common.sendMMMessage(player, LanguageManager.getString("SET-SERVER-LOBBY"));
            return false;
        }

        if (!ServerManager.getInstance().getInWorld().containsKey(player) || !ServerManager.getInstance().getInWorld().get(player).equals(WorldEnum.LOBBY)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.HOLOGRAM.ONLY-IN-LOBBY"));
            return false;
        }

        if (args.length != 3 || !args[0].equalsIgnoreCase("create")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.HOLOGRAM.COMMAND-HELP").replace("%label%", label));
            return false;
        }

        HologramType hologramType;
        switch (args[2].toLowerCase()) {
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
                return false;
        }

        if (HologramManager.getInstance().getHolograms().size() >= 18) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.HOLOGRAM.REACHED-MAX"));
            return false;
        }

        String name = args[1];
        if (HologramManager.getInstance().getHologram(name) != null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.HOLOGRAM.HOLO-EXISTS"));
            return false;
        }

        if (name.length() > 9) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.HOLOGRAM.CREATE-ERROR"));
            return false;
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

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> arguments = new ArrayList<>();
        if (!(sender instanceof Player player)) return arguments;

        if (player.hasPermission("zpp.setup")) {
            if (args.length == 1) {
                arguments.add("create");
            } else if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
                arguments.add("global");
                arguments.add("ladder_static");
                arguments.add("ladder_dynamic");
            }
        }

        return arguments;
    }

}
