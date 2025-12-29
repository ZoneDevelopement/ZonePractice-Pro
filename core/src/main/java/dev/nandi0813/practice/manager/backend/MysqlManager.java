package dev.nandi0813.practice.manager.backend;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Collectors;

public enum MysqlManager {
    ;

    @Getter
    private static Connection connection;

    public static void openConnection() {
        if (!ConfigManager.getBoolean("MYSQL-DATABASE.ENABLED")) return;

        final String host = ConfigManager.getString("MYSQL-DATABASE.CONNECTION.HOST");
        final int port = ConfigManager.getInt("MYSQL-DATABASE.CONNECTION.PORT");
        final String database = ConfigManager.getString("MYSQL-DATABASE.CONNECTION.DATABASE");
        final String username = ConfigManager.getString("MYSQL-DATABASE.CONNECTION.USER");
        final String password = ConfigManager.getString("MYSQL-DATABASE.CONNECTION.PASSWORD");

        final String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
        }

        try {
            if (connection != null && !connection.isClosed())
                initDB();
        } catch (SQLException | IOException e) {
            Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (Exception e) {
            Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
        }
    }

    public static boolean isConnected(boolean reconnect) {
        if (ConfigManager.getBoolean("MYSQL-DATABASE.ENABLED")) {
            try {
                if (connection != null && !connection.isClosed() && connection.isValid(5000))
                    return true;
            } catch (SQLException e) {
                Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
            }
            if (reconnect) {
                openConnection();
                return isConnected(false);
            }
            return false;
        }
        return false;
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

        if (setup != null) {
            String[] queries = setup.split(";");
            for (String query : queries) {
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.execute();
            }
        }
    }

}
