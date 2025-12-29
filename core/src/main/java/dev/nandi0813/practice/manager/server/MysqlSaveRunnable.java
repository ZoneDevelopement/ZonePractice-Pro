package dev.nandi0813.practice.manager.server;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.MysqlManager;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@Getter
public class MysqlSaveRunnable extends BukkitRunnable {

    private final int interval = ConfigManager.getInt("MYSQL-DATABASE.SAVE-PERIOD");

    public void begin() {
        this.runTaskTimerAsynchronously(ZonePractice.getInstance(), interval * 60 * 20L, interval * 60 * 20L);
    }

    @Override
    public void run() {
        save();
    }

    public void save() {
        if (!MysqlManager.isConnected(true)) return;

        for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
            loadGlobalMysqlData(profile);

            for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
                if (!ladder.isEnabled()) continue;

                loadLadderMysqlData(profile, ladder);
            }
        }
    }

    public void loadGlobalMysqlData(Profile profile) {
        try (PreparedStatement checkQuery = MysqlManager.getConnection().prepareStatement("SELECT * FROM global_stats WHERE uuid=?;")) {
            checkQuery.setString(1, profile.getUuid().toString());
            if (!checkQuery.executeQuery().next()) {
                try (PreparedStatement stmt = MysqlManager.getConnection().prepareStatement("INSERT INTO global_stats(username, uuid, firstJoin, lastJoin, unrankedWins, unrankedLosses, rankedWins, rankedLosses, globalElo, globalRank) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);")) {
                    stmt.setString(1, profile.getPlayer().getName());
                    stmt.setString(2, profile.getUuid().toString());
                    stmt.setLong(3, profile.getFirstJoin());
                    stmt.setLong(4, profile.getLastJoin());
                    stmt.setInt(5, profile.getStats().getWins(false));
                    stmt.setInt(6, profile.getStats().getLosses(false));
                    stmt.setInt(7, profile.getStats().getWins(true));
                    stmt.setInt(8, profile.getStats().getLosses(true));
                    stmt.setInt(9, profile.getStats().getGlobalElo());

                    String division;
                    if (profile.getStats().getDivision() == null)
                        division = "";
                    else
                        division = profile.getStats().getDivision().getFullName();

                    stmt.setString(10, ChatColor.stripColor(division));
                    stmt.execute();
                } catch (SQLException e) {
                    Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
                }
            } else {
                try (PreparedStatement stmt = MysqlManager.getConnection().prepareStatement("UPDATE global_stats SET username=?, lastJoin=?, unrankedWins=?, unrankedLosses=?, rankedWins=?, rankedLosses=?, globalElo=?, globalRank=? WHERE uuid=?;")) {
                    stmt.setString(1, profile.getPlayer().getName());
                    stmt.setLong(2, profile.getLastJoin());
                    stmt.setInt(3, profile.getStats().getWins(false));
                    stmt.setInt(4, profile.getStats().getLosses(false));
                    stmt.setInt(5, profile.getStats().getWins(true));
                    stmt.setInt(6, profile.getStats().getLosses(true));
                    stmt.setInt(7, profile.getStats().getGlobalElo());

                    String division;
                    if (profile.getStats().getDivision() == null)
                        division = "";
                    else
                        division = profile.getStats().getDivision().getFullName();

                    stmt.setString(10, ChatColor.stripColor(division));
                    stmt.setString(9, profile.getUuid().toString());
                    stmt.execute();
                } catch (SQLException e) {
                    Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
        }
    }

    public void loadLadderMysqlData(Profile profile, NormalLadder ladder) {
        try (PreparedStatement checkQuery = MysqlManager.getConnection().prepareStatement("SELECT * FROM ladder_stats WHERE uuid=? AND ladder=?;")) {
            checkQuery.setString(1, profile.getUuid().toString());
            checkQuery.setString(2, ladder.getName());
            if (!checkQuery.executeQuery().next()) {
                if (ladder.isRanked()) {
                    try (PreparedStatement stmt = MysqlManager.getConnection().prepareStatement("INSERT INTO ladder_stats(username, uuid, ladder, unrankedWins, unrankedLosses, rankedWins, rankedLosses, elo, rank) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);")) {
                        stmt.setString(1, profile.getPlayer().getName());
                        stmt.setString(2, profile.getUuid().toString());
                        stmt.setString(3, ladder.getName());
                        stmt.setInt(4, profile.getStats().getLadderStat(ladder).getUnRankedWins());
                        stmt.setInt(5, profile.getStats().getLadderStat(ladder).getUnRankedWins());
                        stmt.setInt(6, profile.getStats().getLadderStat(ladder).getRankedWins());
                        stmt.setInt(7, profile.getStats().getLadderStat(ladder).getRankedLosses());
                        stmt.setInt(8, profile.getStats().getLadderStat(ladder).getElo());
                        stmt.setString(9, "NULL");
                        stmt.execute();
                    } catch (SQLException e) {
                        Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
                    }
                } else {
                    try (PreparedStatement stmt = MysqlManager.getConnection().prepareStatement("INSERT INTO ladder_stats(username, uuid, ladder, unrankedWins, unrankedLosses) VALUES(?, ?, ?, ?, ?);")) {
                        stmt.setString(1, profile.getPlayer().getName());
                        stmt.setString(2, profile.getUuid().toString());
                        stmt.setString(3, ladder.getName());
                        stmt.setInt(4, profile.getStats().getLadderStat(ladder).getUnRankedWins());
                        stmt.setInt(5, profile.getStats().getLadderStat(ladder).getUnRankedWins());
                        stmt.execute();
                    } catch (SQLException e) {
                        Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
                    }
                }
            } else {
                if (ladder.isRanked()) {
                    try (PreparedStatement stmt = MysqlManager.getConnection().prepareStatement("UPDATE ladder_stats SET username=?, unrankedWins=?, unrankedLosses=?, rankedWins=?, rankedLosses=?, elo=?, rank=? WHERE uuid=? AND ladder=?;")) {
                        stmt.setString(1, profile.getPlayer().getName());
                        stmt.setInt(2, profile.getStats().getLadderStat(ladder).getUnRankedWins());
                        stmt.setInt(3, profile.getStats().getLadderStat(ladder).getUnRankedWins());
                        stmt.setInt(4, profile.getStats().getLadderStat(ladder).getRankedWins());
                        stmt.setInt(5, profile.getStats().getLadderStat(ladder).getRankedLosses());
                        stmt.setInt(6, profile.getStats().getLadderStat(ladder).getElo());
                        stmt.setString(7, "NULL");
                        stmt.setString(8, profile.getUuid().toString());
                        stmt.setString(9, ladder.getName());
                        stmt.execute();
                    } catch (SQLException e) {
                        Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
                    }
                } else {
                    try (PreparedStatement stmt = MysqlManager.getConnection().prepareStatement("UPDATE ladder_stats SET username=?, unrankedWins=?, unrankedLosses=? WHERE uuid=? AND ladder=?;")) {
                        stmt.setString(1, profile.getPlayer().getName());
                        stmt.setInt(2, profile.getStats().getLadderStat(ladder).getUnRankedWins());
                        stmt.setInt(3, profile.getStats().getLadderStat(ladder).getUnRankedWins());
                        stmt.setString(4, profile.getUuid().toString());
                        stmt.setString(5, ladder.getName());
                        stmt.execute();
                    } catch (SQLException e) {
                        Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
        }
    }

}
