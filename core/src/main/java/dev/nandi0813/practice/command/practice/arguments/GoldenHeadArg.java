package dev.nandi0813.practice.command.practice.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.GoldenHead;
import dev.nandi0813.practice.util.StringUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public enum GoldenHeadArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.setup")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.GOLDENHEAD.INVENTORY-FULL"));
            return;
        }

        GoldenHead goldenHead = ServerManager.getInstance().getGoldenHead();
        if (args.length == 1) {
            player.getInventory().addItem(goldenHead.getItem().clone());
        } else if (args.length == 2) {
            if (StringUtil.isNotInteger(args[1])) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.GOLDENHEAD.INVALID-AMOUNT"));
                return;
            }

            int amount = Integer.parseInt(args[1]);
            if (amount < 1 || amount > 64) {
                Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.GOLDENHEAD.INVALID-AMOUNT-2"));
                return;
            }

            ItemStack itemStack = goldenHead.getItem().clone();
            itemStack.setAmount(amount);

            player.getInventory().addItem(itemStack);
        } else
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.GOLDENHEAD.COMMAND-HELP").replaceAll("%label%", label));
    }

}
