package dev.nandi0813.practice.command.singlecommands;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.inventory.InventoryUtil;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.NameFormatUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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

public class NickCommand implements CommandExecutor, TabCompleter {

    private static final PlainTextComponentSerializer PLAIN_TEXT_SERIALIZER = PlainTextComponentSerializer.plainText();
    private static final String DIFFERENT_NAME_PERMISSION = "zpp.nick.different-name";

    private static String joinArgs(String[] args, int start) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (i != start) builder.append(" ");
            builder.append(args[i]);
        }
        return builder.toString();
    }

    private static void applyNameTemplate(Player target, String nameTemplate) {
        Profile targetProfile = ProfileManager.getInstance().getProfile(target);
        targetProfile.setNameTemplate(NameFormatUtil.normalizePlayerNameTemplate(nameTemplate));
        InventoryUtil.setLobbyNametag(target, targetProfile);
    }

    private static boolean isTemplateKeepingRealName(Player player, String rawTemplate) {
        String normalizedTemplate = NameFormatUtil.normalizePlayerNameTemplate(rawTemplate);
        String plainName = PLAIN_TEXT_SERIALIZER.serialize(
                NameFormatUtil.applyPlayerPlaceholders(
                        NameFormatUtil.parseConfiguredComponent(normalizedTemplate),
                        player.getName()
                )
        ).trim();

        return plainName.equals(player.getName());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Common.sendConsoleMMMessage(LanguageManager.getString("CANT-USE-CONSOLE"));
            return false;
        }

        if (!ConfigManager.getBoolean("PLAYER.LOBBY-NAMETAG.ENABLED")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.NAMETAG.NAMETAG-DISABLED"));
            return false;
        }

        if (!player.hasPermission("zpp.nick.use")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
            return false;
        }

        if (args.length == 0) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.NICK.HELP").replace("%label%", label));
            return false;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            if (args.length == 1) {
                Profile profile = ProfileManager.getInstance().getProfile(player);
                profile.setNameTemplate(null);
                InventoryUtil.setLobbyNametag(player, profile);
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.NICK.RESET-SELF"));
                return true;
            }

            if (!player.hasPermission("zpp.nick.reset.others")) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
                return false;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.NICK.TARGET-OFFLINE").replace("%target%", args[1]));
                return false;
            }

            Profile targetProfile = ProfileManager.getInstance().getProfile(target);
            targetProfile.setNameTemplate(null);
            InventoryUtil.setLobbyNametag(target, targetProfile);

            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.NICK.RESET-OTHER").replace("%target%", target.getName()));
            if (!target.equals(player)) {
                Common.sendMMMessage(target, LanguageManager.getString("COMMAND.NICK.RESETED-BY").replace("%player%", player.getName()));
            }
            return true;
        }

        Player target = player;
        int templateStartIndex = 0;

        if (args.length >= 2 && player.hasPermission("zpp.nick.others")) {
            Player selectedTarget = Bukkit.getPlayer(args[0]);
            if (selectedTarget != null) {
                target = selectedTarget;
                templateStartIndex = 1;
            }
        }

        String nameTemplate = joinArgs(args, templateStartIndex).trim();
        if (nameTemplate.isEmpty()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.NICK.HELP").replace("%label%", label));
            return false;
        }

        if (!player.hasPermission(DIFFERENT_NAME_PERMISSION) && !isTemplateKeepingRealName(target, nameTemplate)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.NICK.DIFFERENT-NAME-NO-PERM"));
            return false;
        }

        applyNameTemplate(target, nameTemplate);

        if (target.equals(player)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.NICK.SET-SELF")
                    .replace("%name%", Common.mmToNormal(nameTemplate)));
        } else {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.NICK.SET-OTHER")
                    .replace("%target%", target.getName())
                    .replace("%name%", Common.mmToNormal(nameTemplate)));
            Common.sendMMMessage(target, LanguageManager.getString("COMMAND.NICK.SET-BY")
                    .replace("%player%", player.getName())
                    .replace("%name%", Common.mmToNormal(nameTemplate)));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> arguments = new ArrayList<>();
        List<String> completion = new ArrayList<>();

        if (!(sender instanceof Player player)) return arguments;
        if (!player.hasPermission("zpp.nick.use")) return arguments;

        if (args.length == 1) {
            arguments.add("reset");

            if (player.hasPermission("zpp.nick.others") || player.hasPermission("zpp.nick.reset.others")) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    arguments.add(online.getName());
                }
            }
            StringUtil.copyPartialMatches(args[0], arguments, completion);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("reset") && player.hasPermission("zpp.nick.reset.others")) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                arguments.add(online.getName());
            }
            StringUtil.copyPartialMatches(args[1], arguments, completion);
        }

        Collections.sort(completion);
        return completion;
    }
}
