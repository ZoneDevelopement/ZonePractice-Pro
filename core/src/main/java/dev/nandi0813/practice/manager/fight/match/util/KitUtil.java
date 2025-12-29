package dev.nandi0813.practice.manager.fight.match.util;

import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.module.interfaces.KitData;
import dev.nandi0813.practice.module.util.ClassImport;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum KitUtil {
    ;

    public static void loadDefaultLadderKit(Player player, TeamEnum team, Ladder ladder) {
        KitData kitData = ladder.getKitData();
        loadKit(player, team, kitData.getArmor(), kitData.getStorage(), kitData.getExtra());
    }

    public static void loadKit(Player player, TeamEnum team, ItemStack[] armor, ItemStack[] inventory, ItemStack[] extra) {
        ClassImport.getClasses().getPlayerUtil().clearInventory(player);

        if (team == null) {
            ClassImport.getClasses().getLadderUtil().loadInventory(player, armor, inventory, extra);
        } else {
            List<ItemStack> inventoryList = new ArrayList<>();
            for (ItemStack item : new ArrayList<>(Arrays.asList(inventory.clone()))) {
                if (item != null) {
                    item = ClassImport.getClasses().getLadderUtil().changeItemColor(item, team.getColor());
                    inventoryList.add(item);
                } else {
                    inventoryList.add(null);
                }
            }

            List<ItemStack> armorList = new ArrayList<>();
            for (ItemStack item : new ArrayList<>(Arrays.asList(armor.clone()))) {
                if (item != null) {
                    item = ClassImport.getClasses().getLadderUtil().changeItemColor(item, team.getColor());
                    armorList.add(item);
                } else {
                    armorList.add(null);
                }
            }

            List<ItemStack> extraList = new ArrayList<>();
            if (extra != null) {
                for (ItemStack item : new ArrayList<>(Arrays.asList(extra.clone()))) {
                    if (item != null) {
                        item = ClassImport.getClasses().getLadderUtil().changeItemColor(item, team.getColor());
                        extraList.add(item);
                    } else {
                        extraList.add(null);
                    }
                }
            }

            ClassImport.getClasses().getLadderUtil().loadInventory(player,
                    armorList.toArray(new ItemStack[0]),
                    inventoryList.toArray(new ItemStack[0]),
                    extra != null ? extraList.toArray(new ItemStack[0]) : null);
        }

        player.updateInventory();
    }

}
