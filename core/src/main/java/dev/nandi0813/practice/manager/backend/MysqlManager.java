package dev.nandi0813.practice.manager.backend;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.statistics.LadderStats;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public enum MysqlManager {
    ;

    @Getter
    private static HikariDataSource dataSource;
    private static ExecutorService executor;

    private static final String GLOBAL_STATS_UPSERT = "INSERT INTO global_stats(" +
            "username, uuid, firstJoin, lastJoin, unrankedWins, unrankedLosses, rankedWins, rankedLosses, globalElo, globalRank, experience, winStreak, bestWinStreak, loseStreak, bestLoseStreak" +
            ") VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE " +
            "username=VALUES(username), firstJoin=VALUES(firstJoin), lastJoin=VALUES(lastJoin), " +
            "unrankedWins=VALUES(unrankedWins), unrankedLosses=VALUES(unrankedLosses), rankedWins=VALUES(rankedWins), rankedLosses=VALUES(rankedLosses), " +
            "globalElo=VALUES(globalElo), globalRank=VALUES(globalRank), experience=VALUES(experience), " +
            "winStreak=VALUES(winStreak), bestWinStreak=VALUES(bestWinStreak), loseStreak=VALUES(loseStreak), bestLoseStreak=VALUES(bestLoseStreak);";

    private static final String LADDER_STATS_UPSERT = "INSERT INTO ladder_stats(" +
            "username, uuid, ladder, unrankedWins, unrankedLosses, rankedWins, rankedLosses, elo, rank, kills, deaths" +
            ") VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE " +
            "username=VALUES(username), unrankedWins=VALUES(unrankedWins), unrankedLosses=VALUES(unrankedLosses), " +
            "rankedWins=VALUES(rankedWins), rankedLosses=VALUES(rankedLosses), elo=VALUES(elo), rank=VALUES(rank), kills=VALUES(kills), deaths=VALUES(deaths);";

    public static void openConnection() {
        if (!ConfigManager.getBoolean("MYSQL-DATABASE.ENABLED")) return;
        if (isConnected(false)) return;

        final String host = ConfigManager.getString("MYSQL-DATABASE.CONNECTION.HOST");
        final int port = ConfigManager.getInt("MYSQL-DATABASE.CONNECTION.PORT");
        final String database = ConfigManager.getString("MYSQL-DATABASE.CONNECTION.DATABASE");
        final String username = ConfigManager.getString("MYSQL-DATABASE.CONNECTION.USER");
        final String password = ConfigManager.getString("MYSQL-DATABASE.CONNECTION.PASSWORD");

        final String url = "jdbc:mariadb://" + host + ":" + port + "/" + database + "?useSsl=false&characterEncoding=utf8";

        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            config.setPoolName("ZonePractice-Hikari");
            config.setMaximumPoolSize(Math.max(2, ConfigManager.getInt("MYSQL-DATABASE.CONNECTION.POOL-SIZE")));
            config.setMinimumIdle(1);
            config.setConnectionTimeout(10000L);
            config.setValidationTimeout(5000L);
            config.setLeakDetectionThreshold(0L);

            dataSource = new HikariDataSource(config);
            executor = Executors.newFixedThreadPool(Math.max(2, config.getMaximumPoolSize() / 2), createThreadFactory());
        } catch (Exception e) {
            Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
            return;
        }

        try {
            if (isConnected(false))
                initDB();
        } catch (SQLException | IOException e) {
            Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
        }
    }

    public static void closeConnection() {
        try {
            if (dataSource != null && !dataSource.isClosed())
                dataSource.close();

            if (executor != null && !executor.isShutdown()) {
                executor.shutdown();
                executor.awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
        } finally {
            dataSource = null;
            executor = null;
        }
    }

    public static boolean isConnected(boolean reconnect) {
        if (ConfigManager.getBoolean("MYSQL-DATABASE.ENABLED")) {
            if (dataSource != null && !dataSource.isClosed()) {
                try (Connection connection = dataSource.getConnection()) {
                    if (connection.isValid(5)) {
                        return true;
                    }
                } catch (SQLException e) {
                    Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
                }
            }

            if (reconnect) {
                openConnection();
                return isConnected(false);
            }
            return false;
        }
        return false;
    }

    public static Connection getConnection() throws SQLException {
        if (!isConnected(true)) {
            throw new SQLException("MySQL is not connected.");
        }
        return dataSource.getConnection();
    }

    public static CompletableFuture<Void> saveProfileAsync(Profile profile) {
        if (!isConnected(true)) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection()) {
                saveGlobalStats(connection, profile);
                saveLadderStats(connection, profile);
            } catch (SQLException e) {
                Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
            }
        }, getExecutor());
    }

    public static CompletableFuture<Void> saveProfilesAsync(Collection<Profile> profiles) {
        if (!isConnected(true)) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<?>[] futures = profiles.stream()
                .map(MysqlManager::saveProfileAsync)
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    public static CompletableFuture<Void> loadProfileAsync(Profile profile) {
        if (!isConnected(false)) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection()) {
                loadGlobalStats(connection, profile);
                loadLadderStats(connection, profile);
            } catch (SQLException e) {
                Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
            }
        }, getExecutor());
    }

    public static CompletableFuture<Void> loadProfilesAsync(Collection<Profile> profiles) {
        if (!isConnected(false)) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<?>[] futures = profiles.stream()
                .map(MysqlManager::loadProfileAsync)
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    public static CompletableFuture<Void> deleteProfileStatsAsync(UUID uuid) {
        if (!isConnected(false)) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection()) {
                try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM global_stats WHERE uuid=?;")) {
                    stmt.setString(1, uuid.toString());
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM ladder_stats WHERE uuid=?;")) {
                    stmt.setString(1, uuid.toString());
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
            }
        }, getExecutor());
    }

    public static CompletableFuture<Void> deleteLadderStatsAsync(String ladderName) {
        if (!isConnected(false)) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement stmt = connection.prepareStatement("DELETE FROM ladder_stats WHERE ladder=?;")) {
                stmt.setString(1, ladderName);
                stmt.executeUpdate();
            } catch (SQLException e) {
                Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
            }
        }, getExecutor());
    }

    private static void initDB() throws IOException, SQLException {
        String setup = null;

        try (InputStream in = ZonePractice.getInstance().getClass().getClassLoader().getResourceAsStream("dbsetup.sql")) {
            if (in != null)
                setup = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            Common.sendConsoleMMMessage(LanguageManager.getString("CANT-READ-DATABASE-FILE"));
            throw e;
        }

        if (setup != null && !setup.isBlank()) {
            String[] queries = setup.split(";");
            try (Connection connection = getConnection()) {
                for (String query : queries) {
                    String normalizedQuery = query.trim();
                    if (normalizedQuery.isEmpty()) {
                        continue;
                    }

                    try (PreparedStatement stmt = connection.prepareStatement(normalizedQuery)) {
                        stmt.execute();
                    } catch (SQLException e) {
                        Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
                    }
                }
            }
        }
    }

    private static void saveGlobalStats(Connection connection, Profile profile) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(GLOBAL_STATS_UPSERT)) {
            String username = profile.getPlayer().getName() != null ? profile.getPlayer().getName() : profile.getUuid().toString();
            stmt.setString(1, username);
            stmt.setString(2, profile.getUuid().toString());
            stmt.setLong(3, profile.getFirstJoin());
            stmt.setLong(4, profile.getLastJoin());
            stmt.setInt(5, profile.getStats().getWins(false));
            stmt.setInt(6, profile.getStats().getLosses(false));
            stmt.setInt(7, profile.getStats().getWins(true));
            stmt.setInt(8, profile.getStats().getLosses(true));
            stmt.setInt(9, profile.getStats().getGlobalElo());

            String division = profile.getStats().getDivision() == null ? "" : profile.getStats().getDivision().getFullName();
            stmt.setString(10, Common.stripLegacyColor(division));

            stmt.setInt(11, profile.getStats().getExperience());
            stmt.setInt(12, profile.getStats().getWinStreak());
            stmt.setInt(13, profile.getStats().getBestWinStreak());
            stmt.setInt(14, profile.getStats().getLoseStreak());
            stmt.setInt(15, profile.getStats().getBestLoseStreak());
            stmt.executeUpdate();
        }
    }

    private static void saveLadderStats(Connection connection, Profile profile) throws SQLException {
        if (profile.getStats().getLadderStats().isEmpty()) {
            return;
        }

        try (PreparedStatement stmt = connection.prepareStatement(LADDER_STATS_UPSERT)) {
            String username = profile.getPlayer().getName() != null ? profile.getPlayer().getName() : profile.getUuid().toString();

            for (Map.Entry<NormalLadder, LadderStats> entry : profile.getStats().getLadderStats().entrySet()) {
                NormalLadder ladder = entry.getKey();
                LadderStats ladderStats = entry.getValue();

                stmt.setString(1, username);
                stmt.setString(2, profile.getUuid().toString());
                stmt.setString(3, ladder.getName());
                stmt.setInt(4, ladderStats.getUnRankedWins());
                stmt.setInt(5, ladderStats.getUnRankedLosses());
                stmt.setInt(6, ladderStats.getRankedWins());
                stmt.setInt(7, ladderStats.getRankedLosses());
                stmt.setInt(8, ladderStats.getElo());
                stmt.setNull(9, Types.VARCHAR);
                stmt.setInt(10, ladderStats.getKills());
                stmt.setInt(11, ladderStats.getDeaths());
                stmt.addBatch();
            }

            stmt.executeBatch();
        }
    }

    private static void loadGlobalStats(Connection connection, Profile profile) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM global_stats WHERE uuid=? LIMIT 1;")) {
            stmt.setString(1, profile.getUuid().toString());

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (!resultSet.next()) {
                    return;
                }

                profile.setFirstJoin(resultSet.getLong("firstJoin"));
                profile.setLastJoin(resultSet.getLong("lastJoin"));

                profile.getStats().setExperience(resultSet.getInt("experience"));
                profile.getStats().setWinStreak(resultSet.getInt("winStreak"));
                profile.getStats().setBestWinStreak(resultSet.getInt("bestWinStreak"));
                profile.getStats().setLoseStreak(resultSet.getInt("loseStreak"));
                profile.getStats().setBestLoseStreak(resultSet.getInt("bestLoseStreak"));
            }
        }
    }

    private static void loadLadderStats(Connection connection, Profile profile) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM ladder_stats WHERE uuid=?;")) {
            stmt.setString(1, profile.getUuid().toString());

            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    NormalLadder ladder = LadderManager.getInstance().getLadder(resultSet.getString("ladder"));
                    if (ladder == null) {
                        continue;
                    }

                    LadderStats ladderStats = profile.getStats().getLadderStat(ladder);
                    ladderStats.setUnRankedWins(resultSet.getInt("unrankedWins"));
                    ladderStats.setUnRankedLosses(resultSet.getInt("unrankedLosses"));
                    ladderStats.setRankedWins(resultSet.getInt("rankedWins"));
                    ladderStats.setRankedLosses(resultSet.getInt("rankedLosses"));
                    ladderStats.setElo(resultSet.getInt("elo"));
                    ladderStats.setKills(resultSet.getInt("kills"));
                    ladderStats.setDeaths(resultSet.getInt("deaths"));
                }
            }
        }
    }

    private static ExecutorService getExecutor() {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newFixedThreadPool(2, createThreadFactory());
        }
        return executor;
    }

    private static ThreadFactory createThreadFactory() {
        return runnable -> {
            Thread thread = new Thread(runnable, "ZonePractice-MySQL");
            thread.setDaemon(true);
            return thread;
        };
    }

}
