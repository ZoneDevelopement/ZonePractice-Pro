package dev.nandi0813.practice.command.ladder.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.ladder.LadderSetupManager;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum IconArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.setup")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.LADDER.NO-PERMISSION"));
            return;
        }

        if (args.length != 3) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.LADDER.ARGUMENTS.ICON.COMMAND-HELP").replace("%label%", label));
            return;
        }

        NormalLadder ladder = LadderManager.getInstance().getLadder(args[2]);
        if (ladder == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.LADDER.ARGUMENTS.ICON.NOT-EXISTS").replace("%ladder%", args[2]));
            return;
        }

        if (ladder.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.LADDER.ARGUMENTS.ICON.LADDER-ENABLED").replace("%ladder%", ladder.getDisplayName()));
            return;
        }

        ItemStack icon = ClassImport.getClasses().getPlayerUtil().getPlayerMainHand(player);
        if (icon == null || icon.getType().equals(Material.AIR)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.LADDER.ARGUMENTS.ICON.NO-ITEM").replace("%ladder%", ladder.getDisplayName()));
            return;
        }

        if (icon.getItemMeta() == null || !icon.getItemMeta().hasDisplayName()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.LADDER.ARGUMENTS.ICON.NO-DISPLAYNAME").replace("%ladder%", ladder.getDisplayName()));
            return;
        }

        ladder.setIcon(icon);

        LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_Main).update();
        GUIManager.getInstance().searchGUI(GUIType.Ladder_Summary).update();

        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.LADDER.ARGUMENTS.ICON.SAVED-ICON").replace("%ladder%", ladder.getDisplayName()));
    }

    public static List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();
        if (!player.hasPermission("zpp.setup")) return arguments;

        if (args.length == 3) {
            for (Ladder ladder : LadderManager.getInstance().getLadders())
                arguments.add(ladder.getName());

            return StringUtil.copyPartialMatches(args[2], arguments, new ArrayList<>());
        }

        return arguments;
    }

}
