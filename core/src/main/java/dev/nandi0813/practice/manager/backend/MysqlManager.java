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
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public enum MysqlManager {
    ;

    private static final Pattern ADD_COLUMN_PATTERN = Pattern.compile(
            "^ALTER\\s+TABLE\\s+`?([a-zA-Z0-9_]+)`?\\s+ADD\\s+COLUMN\\s+`?([a-zA-Z0-9_]+)`?.*$",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern CREATE_UNIQUE_INDEX_PATTERN = Pattern.compile(
            "^CREATE\\s+UNIQUE\\s+INDEX\\s+`?([a-zA-Z0-9_]+)`?\\s+ON\\s+`?([a-zA-Z0-9_]+)`?.*$",
            Pattern.CASE_INSENSITIVE);

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
            "username, uuid, ladder, unrankedWins, unrankedLosses, unrankedWinStreak, unrankedBestWinStreak, unrankedLoseStreak, unrankedBestLoseStreak, " +
            "rankedWins, rankedLosses, rankedWinStreak, rankedBestWinStreak, rankedLoseStreak, rankedBestLoseStreak, elo, `rank`, kills, deaths" +
            ") VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE " +
            "username=VALUES(username), unrankedWins=VALUES(unrankedWins), unrankedLosses=VALUES(unrankedLosses), " +
            "unrankedWinStreak=VALUES(unrankedWinStreak), unrankedBestWinStreak=VALUES(unrankedBestWinStreak), " +
            "unrankedLoseStreak=VALUES(unrankedLoseStreak), unrankedBestLoseStreak=VALUES(unrankedBestLoseStreak), " +
            "rankedWins=VALUES(rankedWins), rankedLosses=VALUES(rankedLosses), rankedWinStreak=VALUES(rankedWinStreak), " +
            "rankedBestWinStreak=VALUES(rankedBestWinStreak), rankedLoseStreak=VALUES(rankedLoseStreak), " +
            "rankedBestLoseStreak=VALUES(rankedBestLoseStreak), elo=VALUES(elo), `rank`=VALUES(`rank`), kills=VALUES(kills), deaths=VALUES(deaths);";

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
            // Explicitly load the MariaDB driver to ensure it's available for JDBC DriverManager
            Class.forName("org.mariadb.jdbc.Driver");

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
            Common.sendConsoleMMMessage("<red>Error: Failed to connect to MySQL database");
            Common.sendConsoleMMMessage("<red>Details: " + e.getMessage());
            if (e.getCause() != null) {
                Common.sendConsoleMMMessage("<red>Caused by: " + e.getCause().getMessage());
            }
            Common.sendConsoleMMMessage("<yellow>Make sure MariaDB JDBC driver is available and MySQL server is running.");
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

    public static void saveProfilesBlocking(Collection<Profile> profiles) {
        if (!isConnected(true) || profiles == null || profiles.isEmpty()) {
            return;
        }

        try (Connection connection = getConnection()) {
            int saved = 0;
            for (Profile profile : profiles) {
                saveGlobalStats(connection, profile);
                saveLadderStats(connection, profile);
                saved++;
            }

            Common.sendConsoleMMMessage("<gray>MySQL shutdown flush completed: <green>" + saved + "<gray> profiles saved.");
        } catch (SQLException e) {
            Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
        }
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
            int applied = 0;
            int skipped = 0;
            int failed = 0;

            try (Connection connection = getConnection()) {
                for (String query : queries) {
                    String normalizedQuery = query.trim();
                    if (normalizedQuery.isEmpty()) {
                        continue;
                    }

                    if (shouldSkipMigrationQuery(connection, normalizedQuery)) {
                        skipped++;
                        continue;
                    }

                    try (PreparedStatement stmt = connection.prepareStatement(normalizedQuery)) {
                        stmt.execute();
                        applied++;
                    } catch (SQLException e) {
                        if (isIgnorableMigrationError(e, normalizedQuery)) {
                            skipped++;
                            continue;
                        }

                        failed++;
                        Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
                    }
                }
            }

            Common.sendConsoleMMMessage("<gray>Database schema migration: <green>applied=" + applied +
                    " <yellow>skipped=" + skipped + " <red>failed=" + failed);
        }
    }

    private static boolean isIgnorableMigrationError(SQLException e, String query) {
        String upperQuery = query.toUpperCase();
        int errorCode = e.getErrorCode();
        String message = e.getMessage() == null ? "" : e.getMessage();

        // Duplicate column in ALTER TABLE ... ADD COLUMN ...
        if (upperQuery.startsWith("ALTER TABLE") && upperQuery.contains("ADD COLUMN")) {
            return errorCode == 1060 || message.contains("Duplicate column name");
        }

        // Duplicate index/key in CREATE UNIQUE INDEX ...
        if (upperQuery.startsWith("CREATE UNIQUE INDEX")) {
            return errorCode == 1061 || message.contains("Duplicate key name");
        }

        return false;
    }

    private static boolean shouldSkipMigrationQuery(Connection connection, String query) throws SQLException {
        Matcher addColumnMatcher = ADD_COLUMN_PATTERN.matcher(query);
        if (addColumnMatcher.matches()) {
            String tableName = addColumnMatcher.group(1);
            String columnName = addColumnMatcher.group(2);
            return columnExists(connection, tableName, columnName);
        }

        Matcher createIndexMatcher = CREATE_UNIQUE_INDEX_PATTERN.matcher(query);
        if (createIndexMatcher.matches()) {
            String indexName = createIndexMatcher.group(1);
            String tableName = createIndexMatcher.group(2);
            return indexExists(connection, tableName, indexName);
        }

        return false;
    }

    private static boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();

        try (ResultSet rs = metaData.getColumns(catalog, null, tableName, columnName)) {
            return rs.next();
        }
    }

    private static boolean indexExists(Connection connection, String tableName, String indexName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();

        try (ResultSet rs = metaData.getIndexInfo(catalog, null, tableName, true, false)) {
            while (rs.next()) {
                String existingName = rs.getString("INDEX_NAME");
                if (existingName != null && existingName.equalsIgnoreCase(indexName)) {
                    return true;
                }
            }
        }

        return false;
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
                stmt.setInt(6, ladderStats.getUnRankedWinStreak());
                stmt.setInt(7, ladderStats.getUnRankedBestWinStreak());
                stmt.setInt(8, ladderStats.getUnRankedLoseStreak());
                stmt.setInt(9, ladderStats.getUnRankedBestLoseStreak());
                stmt.setInt(10, ladderStats.getRankedWins());
                stmt.setInt(11, ladderStats.getRankedLosses());
                stmt.setInt(12, ladderStats.getRankedWinStreak());
                stmt.setInt(13, ladderStats.getRankedBestWinStreak());
                stmt.setInt(14, ladderStats.getRankedLoseStreak());
                stmt.setInt(15, ladderStats.getRankedBestLoseStreak());
                stmt.setInt(16, ladderStats.getElo());
                stmt.setNull(17, Types.VARCHAR);
                stmt.setInt(18, ladderStats.getKills());
                stmt.setInt(19, ladderStats.getDeaths());
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
                Set<String> columns = readResultSetColumns(resultSet);

                while (resultSet.next()) {
                    NormalLadder ladder = LadderManager.getInstance().getLadder(resultSet.getString("ladder"));
                    if (ladder == null) {
                        continue;
                    }

                    LadderStats ladderStats = profile.getStats().getLadderStat(ladder);
                    ladderStats.setUnRankedWins(resultSet.getInt("unrankedWins"));
                    ladderStats.setUnRankedLosses(resultSet.getInt("unrankedLosses"));
                    ladderStats.setUnRankedWinStreak(getIntColumnOrDefault(resultSet, columns, "unrankedWinStreak", 0));
                    ladderStats.setUnRankedBestWinStreak(getIntColumnOrDefault(resultSet, columns, "unrankedBestWinStreak", 0));
                    ladderStats.setUnRankedLoseStreak(getIntColumnOrDefault(resultSet, columns, "unrankedLoseStreak", 0));
                    ladderStats.setUnRankedBestLoseStreak(getIntColumnOrDefault(resultSet, columns, "unrankedBestLoseStreak", 0));
                    ladderStats.setRankedWins(resultSet.getInt("rankedWins"));
                    ladderStats.setRankedLosses(resultSet.getInt("rankedLosses"));
                    ladderStats.setRankedWinStreak(getIntColumnOrDefault(resultSet, columns, "rankedWinStreak", 0));
                    ladderStats.setRankedBestWinStreak(getIntColumnOrDefault(resultSet, columns, "rankedBestWinStreak", 0));
                    ladderStats.setRankedLoseStreak(getIntColumnOrDefault(resultSet, columns, "rankedLoseStreak", 0));
                    ladderStats.setRankedBestLoseStreak(getIntColumnOrDefault(resultSet, columns, "rankedBestLoseStreak", 0));
                    ladderStats.setElo(resultSet.getInt("elo"));
                    ladderStats.setKills(resultSet.getInt("kills"));
                    ladderStats.setDeaths(resultSet.getInt("deaths"));
                }
            }
        }
    }

    private static Set<String> readResultSetColumns(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        Set<String> columns = new HashSet<>();

        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            columns.add(metaData.getColumnLabel(i).toLowerCase());
        }

        return columns;
    }

    private static int getIntColumnOrDefault(ResultSet resultSet, Set<String> columns, String column, int defaultValue) throws SQLException {
        return columns.contains(column.toLowerCase()) ? resultSet.getInt(column) : defaultValue;
    }

    // -------------------------------------------------------------------------
    // Match History — MySQL methods
    // All match-history DB logic lives here, following the existing pattern.
    // -------------------------------------------------------------------------

    private static final String MATCH_HISTORY_INSERT =
            "INSERT INTO match_history " +
            "(player_uuid, opponent_uuid, player_name, opponent_name, kit_name, arena_name, " +
            " player_score, opponent_score, player_final_health, opponent_final_health, " +
            " winner_uuid, match_duration, played_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    /**
     * Saves a match history row to MySQL, then prunes old rows for both players.
     * Fire-and-forget — errors are logged but never propagated.
     */
    public static void saveMatchHistoryAsync(UUID playerUuid, UUID opponentUuid,
                                             String playerName, String opponentName,
                                             String kitName, String arenaName,
                                             int playerScore, int opponentScore,
                                             double playerFinalHealth, double opponentFinalHealth,
                                             UUID winnerUuid, int matchDuration, long playedAt) {
        if (!isConnected(false)) return;

        CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                insertMatchHistoryRow(conn, playerUuid, opponentUuid, playerName, opponentName,
                        kitName, arenaName, playerScore, opponentScore,
                        playerFinalHealth, opponentFinalHealth, winnerUuid, matchDuration, playedAt);
                pruneMatchHistory(conn, playerUuid);
                pruneMatchHistory(conn, opponentUuid);
            } catch (SQLException e) {
                Common.sendConsoleMMMessage("<red>[MatchHistory] MySQL save error: " + e.getMessage());
            }
        }, getExecutor());
    }

    /**
     * Synchronously fetches the last {@code limit} matches for a player.
     * Returns an empty list on error. Call only from an async context.
     */
    public static List<dev.nandi0813.practice.manager.matchhistory.MatchHistoryEntry> loadMatchHistorySync(
            UUID playerUuid, int limit) {
        List<dev.nandi0813.practice.manager.matchhistory.MatchHistoryEntry> result = new java.util.ArrayList<>();
        if (!isConnected(false)) return result;

        String sql = "SELECT * FROM match_history " +
                "WHERE player_uuid = ? OR opponent_uuid = ? " +
                "ORDER BY match_id DESC LIMIT ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, playerUuid.toString());
            stmt.setInt(3, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String winStr = rs.getString("winner_uuid");
                    java.util.UUID winnerUuid = (winStr != null && !winStr.isEmpty())
                            ? java.util.UUID.fromString(winStr) : null;

                    result.add(new dev.nandi0813.practice.manager.matchhistory.MatchHistoryEntry(
                            rs.getInt("match_id"),
                            java.util.UUID.fromString(rs.getString("player_uuid")),
                            java.util.UUID.fromString(rs.getString("opponent_uuid")),
                            rs.getString("player_name"),
                            rs.getString("opponent_name"),
                            rs.getString("kit_name"),
                            rs.getString("arena_name"),
                            rs.getInt("player_score"),
                            rs.getInt("opponent_score"),
                            rs.getDouble("player_final_health"),
                            rs.getDouble("opponent_final_health"),
                            winnerUuid,
                            rs.getInt("match_duration"),
                            rs.getLong("played_at")
                    ));
                }
            }
        } catch (SQLException e) {
            Common.sendConsoleMMMessage("<red>[MatchHistory] MySQL load error: " + e.getMessage());
        }
        return result;
    }

    private static void insertMatchHistoryRow(Connection conn,
                                              UUID playerUuid, UUID opponentUuid,
                                              String playerName, String opponentName,
                                              String kitName, String arenaName,
                                              int playerScore, int opponentScore,
                                              double playerFinalHealth, double opponentFinalHealth,
                                              UUID winnerUuid, int matchDuration, long playedAt)
            throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(MATCH_HISTORY_INSERT)) {
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, opponentUuid.toString());
            stmt.setString(3, playerName);
            stmt.setString(4, opponentName);
            stmt.setString(5, kitName);
            stmt.setString(6, arenaName);
            stmt.setInt(7, playerScore);
            stmt.setInt(8, opponentScore);
            stmt.setDouble(9, playerFinalHealth);
            stmt.setDouble(10, opponentFinalHealth);
            stmt.setString(11, winnerUuid != null ? winnerUuid.toString() : null);
            stmt.setInt(12, matchDuration);
            stmt.setLong(13, playedAt);
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes match_history rows for {@code uuid} beyond the last 5.
     */
    private static void pruneMatchHistory(Connection conn, UUID uuid) throws SQLException {
        String selectSql = "SELECT match_id FROM match_history " +
                "WHERE player_uuid = ? OR opponent_uuid = ? ORDER BY match_id DESC";
        List<Integer> ids = new java.util.ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("match_id"));
            }
        }

        if (ids.size() <= 5) return;

        List<Integer> toDelete = ids.subList(5, ids.size());
        StringBuilder sb = new StringBuilder("DELETE FROM match_history WHERE match_id IN (");
        for (int i = 0; i < toDelete.size(); i++) sb.append(i == 0 ? "?" : ",?");
        sb.append(")");

        try (PreparedStatement stmt = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < toDelete.size(); i++) stmt.setInt(i + 1, toDelete.get(i));
            stmt.executeUpdate();
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
