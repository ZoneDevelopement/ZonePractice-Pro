package dev.nandi0813.practice.manager.gui.setup.arena;

import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.nandi0813.practice.manager.arena.util.ArenaUtil;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.gui.GUIItem;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ArenaSetupUtil {
    ;

    @Getter
    private static final Map<ItemStack, DisplayArena> arenaMarkerList = new HashMap<>();

    // Name & Icon & Information item
    public static ItemStack getNameItem(Arena arena) {
        List<String> lore = new ArrayList<>();
        for (String line : GUIFile.getStringList("GUIS.SETUP.ARENA.ARENA-MAIN.ICONS.ARENA-NAME.LORE")) {
            lore.add(line
                    .replace("%arenaName%", arena.getName())
                    .replace("%arenaDisplayName%", arena.getDisplayName())
                    .replace("%arenaType%", arena.getType().getName())
            );
        }

        if (arena.getIcon() != null) {
            return ClassImport.getClasses().getItemCreateUtil().createItem(
                    arena.getIcon(),
                    GUIFile.getString("GUIS.SETUP.ARENA.ARENA-MAIN.ICONS.ARENA-NAME.NAME")
                            .replace("%arenaDisplayName%", arena.getDisplayName())
                            .replace("%arenaName%", arena.getName()),
                    lore);
        } else {
            return ClassImport.getClasses().getItemCreateUtil().createItem(
                    GUIFile.getString("GUIS.SETUP.ARENA.ARENA-MAIN.ICONS.ARENA-NAME.NAME")
                            .replace("%arenaDisplayName%", arena.getDisplayName())
                            .replace("%arenaName%", arena.getName()),
                    Material.valueOf(GUIFile.getString("GUIS.SETUP.ARENA.ARENA-MAIN.ICONS.ARENA-NAME.MATERIAL")),
                    lore);
        }
    }

    // Status item
    public static ItemStack getStatusItem(Arena arena) {
        if (arena.isEnabled())
            return GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-MAIN.ICONS.STATUS.ENABLED").get();
        else
            return GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-MAIN.ICONS.STATUS.DISABLED").get();
    }

    // Arena copies item
    public static ItemStack getArenaCopiesItem(Arena arena) {
        if (!arena.isBuild()) return null;

        int size = arena.getCopies().size();
        if (size < 1) size = 1;

        ItemStack item = GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-MAIN.ICONS.COPIES").get();
        item.setAmount(size);

        return item;
    }

    public static ItemStack getCopyArenaItem(Arena arena, int number) {
        GUIItem guiItem = GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-COPY.ICONS.COPY-ARENA");

        guiItem
                .replace("%arenaName%", arena.getName())
                .replace("%arenaDisplayName%", arena.getDisplayName())
                .replace("%copyNumber%", String.valueOf(number));

        ItemStack itemStack = guiItem.get();
        itemStack.setAmount(number);

        return itemStack;
    }

    // Locations item
    public static ItemStack getLocationItem(Arena arena) {
        GUIItem guiItem;
        if (arena.isBuild()) {
            guiItem = GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-MAIN.ICONS.LOCATION.BUILD")
                    .replace("%arenaName%", arena.getName())
                    .replace("%arenaDisplayName%", arena.getDisplayName())
                    .replace("%corner1%", Common.mmToNormal(ArenaUtil.convertLocation(arena.getCorner1())))
                    .replace("%corner2%", Common.mmToNormal(ArenaUtil.convertLocation(arena.getCorner2())))
                    .replace("%position1%", Common.mmToNormal(ArenaUtil.convertLocation(arena.getPosition1())))
                    .replace("%position2%", Common.mmToNormal(ArenaUtil.convertLocation(arena.getPosition2())))
                    .replace("%bed1%", Common.mmToNormal(ArenaUtil.convertLocation(arena.getBedLoc1())))
                    .replace("%bed2%", Common.mmToNormal(ArenaUtil.convertLocation(arena.getBedLoc2())))
                    .replace("%portal1%", Common.mmToNormal(ArenaUtil.convertLocation(arena.getPortalLoc1())))
                    .replace("%portal2%", Common.mmToNormal(ArenaUtil.convertLocation(arena.getPortalLoc2())))
                    .replace("%sideBuildLimit%", String.valueOf(arena.getSideBuildLimit()))
                    .replace("%buildMaxY%", arena.isBuildMax() ? String.valueOf(arena.getBuildMaxValue()) : "&cNot Set")
                    .replace("%deathZoneY%", arena.isDeadZone() ? String.valueOf(arena.getDeadZoneValue()) : "&cNot Set");
        } else {
            guiItem = GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-MAIN.ICONS.LOCATION.NOT-BUILD")
                    .replace("%arenaName%", arena.getName())
                    .replace("%arenaDisplayName%", arena.getDisplayName())
                    .replace("%corner1%", Common.mmToNormal(ArenaUtil.convertLocation(arena.getCorner1())))
                    .replace("%corner2%", Common.mmToNormal(ArenaUtil.convertLocation(arena.getCorner2())))
                    .replace("%position1%", Common.mmToNormal(ArenaUtil.convertLocation(arena.getPosition1())))
                    .replace("%position2%", Common.mmToNormal(ArenaUtil.convertLocation(arena.getPosition2())))
                    .replace("%deathZoneY%", arena.isDeadZone() ? String.valueOf(arena.getDeadZoneValue()) : "&cNot Set");
        }

        return guiItem.get();
    }

    public static ItemStack getFreezeItem(Arena arena) {
        if (arena.isFrozen())
            return GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-MAIN.ICONS.FREEZE.FROZEN").get();
        else
            return GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-MAIN.ICONS.FREEZE.NOT-FROZEN").get();
    }

    public static ItemStack getCopyGuiNavMainItem(Arena arena, int copies) {
        return GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-COPY.ICONS.NAV-MAIN")
                .replace("%arenaDisplayName%", arena.getDisplayName())
                .replace("%arenaName%", arena.getName())
                .replace("%copies%", String.valueOf(copies))
                .get();
    }

    public static ItemStack getAssignedLadderItem(Ladder ladder) {
        return GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-LADDERS-SINGLE.ICONS.LADDER-ICONS.ASSIGNED")
                .replace("%ladderDisplayName%", ladder.getDisplayName())
                .replace("%ladderName%", ladder.getName())
                .get();
    }

    public static ItemStack getNotAssignedLadderItem(Ladder ladder) {
        return GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-LADDERS-SINGLE.ICONS.LADDER-ICONS.NOT-ASSIGNED")
                .replace("%ladderDisplayName%", ladder.getDisplayName())
                .replace("%ladderName%", ladder.getName())
                .get();
    }

    public static ItemStack getDisabledLadderItem(Ladder ladder) {
        return GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-LADDERS-SINGLE.ICONS.LADDER-ICONS.DISABLED")
                .replace("%ladderDisplayName%", ladder.getDisplayName())
                .replace("%ladderName%", ladder.getName())
                .get();
    }

    public static ItemStack getNonCompatibleLadderItem(Ladder ladder) {
        return GUIFile.getGuiItem("GUIS.SETUP.ARENA.ARENA-LADDERS-SINGLE.ICONS.LADDER-ICONS.NOT-COMPATIBLE")
                .replace("%ladderDisplayName%", ladder.getDisplayName())
                .replace("%ladderName%", ladder.getName())
                .get();
    }

}
