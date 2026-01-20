package dev.nandi0813.practice.manager.ladder.util;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.backend.MysqlManager;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaGUISetupManager;
import dev.nandi0813.practice.manager.gui.setup.hologram.HologramSetupManager;
import dev.nandi0813.practice.manager.gui.setup.ladder.LadderSetupManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.ladder.type.SkyWars;
import dev.nandi0813.practice.manager.leaderboard.LeaderboardManager;
import dev.nandi0813.practice.manager.leaderboard.types.LbMainType;
import dev.nandi0813.practice.manager.leaderboard.types.LbSecondaryType;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public enum LadderUtil {
    ;

    public static void changeStatus(Player player, NormalLadder ladder) {
        if (!ladder.isEnabled()) {
            if (ladder.getIcon() == null) {
                Common.sendMMMessage(player, LanguageManager.getString("LADDER.STATUS-CHANGE.NO-ICON"));
                return;
            }
            if (!ladder.getKitData().isSet()) {
                Common.sendMMMessage(player, LanguageManager.getString("LADDER.STATUS-CHANGE.NO-CONTENT"));
                return;
            }
            if (ladder.getMatchTypes().isEmpty()) {
                Common.sendMMMessage(player, LanguageManager.getString("LADDER.STATUS-CHANGE.NO-MATCHTYPE-ASSIGNED"));
                return;
            }
            if (ladder instanceof SkyWars && ((SkyWars) ladder).getSkyWarsLoot() == null) {
                Common.sendMMMessage(player, LanguageManager.getString("LADDER.STATUS-CHANGE.SKYWARS.NO-LOOT"));
                return;
            }

            enableLadder(ladder);
        } else {
            if (!MatchManager.getInstance().getLiveMatchesByLadder(ladder).isEmpty()) {
                Common.sendMMMessage(player, LanguageManager.getString("LADDER.STATUS-CHANGE.CANT-DISABLE"));
                return;
            }

            disableLadder(ladder);
        }
    }

    public static void disableLadder(NormalLadder ladder) {
        ladder.setEnabled(false);
        ladder.setFrozen(false);

        // Remove the ladder from the arenas and holograms.
        ArenaManager.getInstance().removeLadder(ladder);
        LeaderboardManager.getInstance().removeLadder(ladder);

        /*
         * Update GUIs
         */
        LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_Main).update();
        GUIManager.getInstance().searchGUI(GUIType.Ladder_Summary).update();
        GUIManager.getInstance().searchGUI(GUIType.Arena_Summary).update();

        GUIManager.getInstance().searchGUI(GUIType.Queue_Unranked).update();
        GUIManager.getInstance().searchGUI(GUIType.Queue_Ranked).update();
        GUIManager.getInstance().searchGUI(GUIType.CustomLadder_Selector).update();

        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                profile.getFile().deleteCustomKit(ladder);
                profile.getUnrankedCustomKits().remove(ladder);
                profile.getRankedCustomKits().remove(ladder);
            }
        });

        /*
         * Delete the ladder statistics from the mysql table.
         */
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            if (!MysqlManager.isConnected(true)) return;

            try (PreparedStatement stmt = MysqlManager.getConnection().prepareStatement("DELETE FROM ladder_stats WHERE ladder=?;")) {
                stmt.setString(1, ladder.getName());
                stmt.executeUpdate();
            } catch (SQLException e) {
                Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
            }
        });
    }

    public static void enableLadder(NormalLadder ladder) {
        ladder.setEnabled(true);

        // Update GUIs
        ladder.getPreviewGui().update();
        GUIManager.getInstance().searchGUI(GUIType.Queue_Ranked).update();
        GUIManager.getInstance().searchGUI(GUIType.Queue_Unranked).update();
        GUIManager.getInstance().searchGUI(GUIType.CustomLadder_Selector).update();

        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            /*
            for (Arena arena : ArenaManager.getInstance().getNormalArenas()) {
                if (arena.getAssignedLadderTypes().contains(ladder.getType()))
                    arena.getAssignedLadders().add(ladder);
            }
             */

            // Update ladder setup GUIs
            for (Map<GUIType, GUI> map : HologramSetupManager.getInstance().getHologramSetupGUIs().values())
                map.get(GUIType.Hologram_Ladder).update();
            for (Map<GUIType, GUI> map : ArenaGUISetupManager.getInstance().getArenaSetupGUIs().values()) {
                if (map.containsKey(GUIType.Arena_Ladders_Type))
                    map.get(GUIType.Arena_Ladders_Type).update();

                map.get(GUIType.Arena_Ladders_Single).update();
            }

            LadderSetupManager.getInstance().getLadderSetupGUIs().get(ladder).get(GUIType.Ladder_Main).update();
            GUIManager.getInstance().searchGUI(GUIType.Ladder_Summary).update();

            for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                profile.getStats().createLadderStat(ladder);

                // Set the custom ladder kits
                if (ladder.isEditable()) {
                    profile.getUnrankedCustomKits().put(ladder, new HashMap<>());

                    if (ladder.isRanked()) {
                        profile.getRankedCustomKits().put(ladder, new HashMap<>());
                    }
                }
            }

            // Update ladder leaderboard
            for (LbMainType lbMainType : LbMainType.values()) {
                for (LbSecondaryType lbSecondaryType : LbSecondaryType.values()) {
                    if (lbMainType.equals(LbMainType.LADDER)) {
                        LeaderboardManager.getInstance().updateLB(lbMainType, lbSecondaryType, ladder);
                    }
                }
            }
        });
    }

    public static Arena getAvailableArena(Ladder ladder) {
        List<Arena> availableArenas = ladder.getAvailableArenas();

        if (!availableArenas.isEmpty())
            return availableArenas.get(new Random().nextInt(availableArenas.size()));
        else
            return null;
    }

    public static List<String> getLadderNames(List<NormalLadder> ladders) {
        List<String> names = new ArrayList<>();
        for (Ladder ladder : ladders)
            names.add(ladder.getName());
        return names;
    }

}
