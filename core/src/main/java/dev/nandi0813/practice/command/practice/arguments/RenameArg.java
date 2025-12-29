package dev.nandi0813.practice.command.practice.arguments;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public enum RenameArg {
    ;

    public static void run(Player player, String label, String[] args) {
        if (!player.hasPermission("zpp.practice.rename")) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.NO-PERMISSION"));
            return;
        }

        if (args.length <= 1) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RENAME.COMMAND-HELP").replaceAll("%label%", label));
            return;
        }

        if (player.getItemInHand() == null || player.getItemInHand().getType().equals(Material.AIR)) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.PRACTICE.ARGUMENTS.RENAME.ITEM-IN-HAND"));
            return;
        }

        List<String> name = Arrays.asList(args);
        StringBuilder builder = new StringBuilder();

        for (int i = 1; i < name.size(); i++) {
            builder.append(name.get(i));
            if (name.size() - 1 != i)
                builder.append(" ");
        }

        ItemStack item = player.getItemInHand();
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(StringUtil.CC(builder.toString()));
        item.setItemMeta(itemMeta);
        player.setItemInHand(item);
    }

}
