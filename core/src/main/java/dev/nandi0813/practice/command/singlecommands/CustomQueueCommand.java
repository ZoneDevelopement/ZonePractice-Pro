package dev.nandi0813.practice.command.singlecommands;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.guis.queue.CustomKit.ChooseQueueTypeGui;
import dev.nandi0813.practice.manager.ladder.abstraction.playercustom.CustomLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.queue.CustomKitQueueManager;
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
import java.util.Locale;

public class CustomQueueCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("join", "host", "leave", "open");

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Common.sendConsoleMMMessage(LanguageManager.getString("CANT-USE-CONSOLE"));
            return true;
        }

        if (!player.hasPermission("zpp.playerkit.queue.use")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.QUEUES.CUSTOM.NO-PERMISSION"));
            return true;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null || profile.getStatus() != ProfileStatus.LOBBY || profile.isStaffMode() || profile.isParty()) {
            Common.sendMMMessage(player, LanguageManager.getString("CANT-USE-COMMAND"));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("open") || args[0].equalsIgnoreCase("gui")) {
            openQueueGui(player);
            return true;
        }

        String subcommand = args[0].toLowerCase(Locale.ROOT);
        CustomKitQueueManager queueManager = CustomKitQueueManager.getInstance();

        switch (subcommand) {
            case "join", "search" -> {
                if (queueManager.getJoinSearch(player) != null) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.QUEUES.CUSTOM.ALREADY-SEARCHING"));
                    return true;
                }

                if (queueManager.getHostedQueue(player) != null) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.QUEUES.CUSTOM.ALREADY-HOSTING"));
                    return true;
                }

                queueManager.startJoinSearch(player);
                return true;
            }
            case "host" -> {
                if (!player.hasPermission("zpp.playerkit.queue.host")) {
                    Common.sendMMMessage(player, LanguageManager.getString("QUEUES.CUSTOM.NO-PERMISSION"));
                    return true;
                }

                if (args.length < 2) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.QUEUES.CUSTOM.HOST-USAGE"));
                    return true;
                }

                if (queueManager.getHostedQueue(player) != null) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.QUEUES.CUSTOM.ALREADY-HOSTING"));
                    return true;
                }

                String kitName = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                CustomLadder customLadder = findHostableKit(profile, kitName);
                if (customLadder == null) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.QUEUES.CUSTOM.KIT-NOT-FOUND")
                            .replace("%kit%", kitName));
                    return true;
                }

                queueManager.hostQueue(player, customLadder);
                return true;
            }
            case "leave", "cancel" -> {
                boolean cancelledSearch = queueManager.cancelJoinSearch(player, true, true);
                boolean cancelledHost = queueManager.cancelHostedQueue(player, true, true);
                if (!cancelledSearch && !cancelledHost) {
                    Common.sendMMMessage(player, LanguageManager.getString("COMMAND.QUEUES.CUSTOM.NOT-IN-CUSTOM-QUEUE"));
                }
                return true;
            }
            default -> {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.QUEUES.CUSTOM.HELP"));
                return true;
            }
        }
    }

    private void openQueueGui(Player player) {
        GUI gui = GUIManager.getInstance().searchGUI(GUIType.Queue_CustomKitChooseType);
        if (gui instanceof ChooseQueueTypeGui chooseQueueTypeGui) {
            chooseQueueTypeGui.openFor(player, GUIType.Queue_Unranked);
        } else if (gui != null) {
            gui.open(player);
        }
    }

    private CustomLadder findHostableKit(Profile profile, String inputName) {
        String normalizedInput = normalizeKitName(inputName);
        for (CustomLadder customLadder : CustomKitQueueManager.getInstance().getHostableKits(profile)) {
            String displayName = normalizeKitName(customLadder.getDisplayName());
            String internalName = normalizeKitName(customLadder.getName());
            if (normalizedInput.equals(displayName) || normalizedInput.equals(internalName)) {
                return customLadder;
            }
        }
        return null;
    }

    private String normalizeKitName(String input) {
        if (input == null) {
            return "";
        }

        String stripped = Common.stripLegacyColor(input).replaceAll("<[^>]+>", "");
        return stripped.replace("_", "").replace(" ", "").toLowerCase(Locale.ROOT);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player) || !player.hasPermission("zpp.playerkit.queue.use")) {
            return Collections.emptyList();
        }

        List<String> completion = new ArrayList<>();
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, completion);
            Collections.sort(completion);
            return completion;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("host") && player.hasPermission("zpp.playerkit.queue.host")) {
            Profile profile = ProfileManager.getInstance().getProfile(player);
            if (profile == null) {
                return Collections.emptyList();
            }

            List<String> hostableKits = new ArrayList<>();
            for (CustomLadder customLadder : CustomKitQueueManager.getInstance().getHostableKits(profile)) {
                hostableKits.add(customLadder.getDisplayName().replace(' ', '_'));
            }

            StringUtil.copyPartialMatches(args[args.length - 1], hostableKits, completion);
            Collections.sort(completion);
            return completion;
        }

        return Collections.emptyList();
    }
}

