package dev.nandi0813.practice.command.ladder.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.ladder.LadderSetupManager;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.util.Common;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public enum EffectArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.setup")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.LADDER.NO-PERMISSION"));
            return;
        }

        if (args.length != 3) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.LADDER.ARGUMENTS.EFFECT.COMMAND-HELP").replace("%label%", label));
            return;
        }

        Ladder ladder = LadderManager.getInstance().getLadder(args[2]);
        if (ladder == null) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.LADDER.ARGUMENTS.EFFECT.NOT-EXISTS").replace("%ladder%", args[2]));
            return;
        }

        if (ladder.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.LADDER.ARGUMENTS.EFFECT.LADDER-ENABLED").replace("%ladder%", ladder.getDisplayName()));
            return;
        }

        List<PotionEffect> effects = (List<PotionEffect>) player.getActivePotionEffects();
        if (effects.isEmpty()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.LADDER.ARGUMENTS.EFFECT.NO-EFFECTS"));
            return;
        }

        ladder.getKitData().setEffects(effects);
        LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_Inventory).update();

        Common.sendMMMessage(player, LanguageManager.getString("COMMAND.LADDER.ARGUMENTS.EFFECT.SET-EFFECTS").replace("%ladder%", ladder.getDisplayName()));
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
