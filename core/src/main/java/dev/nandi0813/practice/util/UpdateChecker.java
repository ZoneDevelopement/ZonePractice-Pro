package dev.nandi0813.practice.util;

import dev.nandi0813.practice.ZonePractice;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Checks for new plugin releases via the GitHub REST API and notifies
 * operators / players who hold the {@code zpp.update.notify} permission.
 *
 * <p>The check runs asynchronously so it never blocks the main thread.
 * Results are cached for the lifetime of the server session.
 */
public enum UpdateChecker {
    ;

    /** Permission required to receive the update notification on join. */
    public static final String NOTIFY_PERMISSION = "zpp.update.notify";

    /**
     * GitHub repository in "owner/repo" format.
     * Change this to the actual repository if it differs.
     */
    private static final String GITHUB_REPO = "ZoneDevelopement/ZonePractice-Pro";

    private static final String API_URL =
            "https://api.github.com/repos/" + GITHUB_REPO + "/releases";

    private static final String TAGS_API_URL =
            "https://api.github.com/repos/" + GITHUB_REPO + "/tags";

    // ------------------------------------------------------------------ state

    @Getter private static volatile boolean checked = false;
    @Getter private static volatile boolean updateAvailable = false;
    @Getter private static volatile String latestVersion = null;
    @Getter private static volatile String currentVersion = null;
    @Getter private static volatile int versionsBehind = 0;
    @Getter private static volatile String releaseUrl = null;

    // ------------------------------------------------------------------ API

    /**
     * Runs the GitHub API check on a new async thread. When the check is
     * complete, results are stored in the static fields above and the console
     * is notified automatically.
     *
     * @param plugin the owning plugin instance
     */
    public static void checkAsync(ZonePractice plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                currentVersion = plugin.getDescription().getVersion()
                        .replace("-SNAPSHOT", "")
                        .replace("-snapshot", "")
                        .trim();

                List<String> releaseVersions = fetchReleaseVersions();

                if (releaseVersions.isEmpty()) {
                    checked = true;
                    Common.sendConsoleMMMessage(
                            "<gray>[ZonePractice] <yellow>Could not retrieve any releases from GitHub.");
                    return;
                }

                latestVersion = releaseVersions.get(0);
                releaseUrl = "https://github.com/" + GITHUB_REPO + "/releases/latest";

                // Count how many published releases are "newer" than the running version.
                int behind = 0;
                for (String v : releaseVersions) {
                    if (isNewerThan(v, currentVersion)) {
                        behind++;
                    } else {
                        break; // releases are sorted newest-first
                    }
                }

                versionsBehind = behind;
                updateAvailable = behind > 0;
                checked = true;

                // Always log to console regardless of permission
                notifyConsole();

            } catch (Exception e) {
                checked = true;
                Common.sendConsoleMMMessage("<gray>[ZonePractice] <yellow>Update check failed: <red>" + e.getMessage());
                plugin.getLogger().log(Level.FINE, "[ZonePractice] Update check exception details:", e);
            }
        });
    }

    /**
     * Sends the update notification to a player if they have the
     * {@value #NOTIFY_PERMISSION} permission and an update is available.
     *
     * @param player the player to notify
     */
    public static void notifyPlayer(Player player) {
        if (!checked || !updateAvailable) return;
        if (!player.hasPermission(NOTIFY_PERMISSION)) return;

        sendUpdateLines(player);
    }

    // --------------------------------------------------------------- helpers

    private static void notifyConsole() {
        if (!updateAvailable) {
            Common.sendConsoleMMMessage(
                    "<gray>[ZonePractice] <green>You are running the latest version <white>(" + latestVersion + ")<green>.");
            return;
        }

        Common.sendConsoleMMMessage("<gray>[ZonePractice] <yellow>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Common.sendConsoleMMMessage("<gray>[ZonePractice] <yellow>⚠ Update available for ZonePractice Pro!");
        Common.sendConsoleMMMessage("<gray>[ZonePractice] <gray>  Current version : <red>" + currentVersion);
        Common.sendConsoleMMMessage("<gray>[ZonePractice] <gray>  Latest version  : <green>" + latestVersion);

        if (versionsBehind > 1) {
            Common.sendConsoleMMMessage(
                    "<gray>[ZonePractice] <gray>  Versions behind : <gold>" + versionsBehind);
        }

        Common.sendConsoleMMMessage("<gray>[ZonePractice] <gray>  Download        : <aqua>" + releaseUrl);
        Common.sendConsoleMMMessage("<gray>[ZonePractice] <yellow>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private static void sendUpdateLines(Player player) {
        Common.sendMMMessage(player, "<yellow>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Common.sendMMMessage(player, "<yellow>⚠ <gold><bold>ZonePractice Pro</bold> <yellow>update available!");
        Common.sendMMMessage(player, "<gray>  Current version : <red>" + currentVersion);
        Common.sendMMMessage(player, "<gray>  Latest version  : <green>" + latestVersion);

        if (versionsBehind > 1) {
            Common.sendMMMessage(player,
                    "<gray>  Versions behind : <gold>" + versionsBehind);
        }

        Common.sendMMMessage(player, "<gray>  Download        : <aqua><click:open_url:'" + releaseUrl + "'>"
                + releaseUrl + "</click>");
        Common.sendMMMessage(player, "<yellow>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    /**
     * Fetches the list of release tag names from GitHub, newest-first.
     */
    private static List<String> fetchReleaseVersions() throws Exception {
        JSONArray releases = fetchJsonArray(API_URL + "?per_page=20&page=1");
        List<String> versions = new ArrayList<>();
        for (int i = 0; i < releases.length(); i++) {
            JSONObject release = releases.getJSONObject(i);

            // Skip drafts and pre-releases
            if (release.optBoolean("draft", false)) continue;
            if (release.optBoolean("prerelease", false)) continue;

            String tag = release.optString("tag_name", "");
            if (tag.startsWith("v") || tag.startsWith("V")) {
                tag = tag.substring(1);
            }
            if (!tag.isEmpty()) {
                versions.add(tag);
            }
        }

        // This repository may only publish pre-releases. Fall back to tags in that case.
        if (versions.isEmpty()) {
            JSONArray tags = fetchJsonArray(TAGS_API_URL + "?per_page=20&page=1");
            for (int i = 0; i < tags.length(); i++) {
                JSONObject tagObj = tags.getJSONObject(i);
                String tag = tagObj.optString("name", "");
                if (tag.startsWith("v") || tag.startsWith("V")) {
                    tag = tag.substring(1);
                }
                if (!tag.isEmpty()) {
                    versions.add(tag);
                }
            }
        }

        return versions;
    }

    private static JSONArray fetchJsonArray(String endpoint) throws IOException {
        URL url = URI.create(endpoint).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5_000);
        conn.setReadTimeout(5_000);
        conn.setRequestProperty("Accept", "application/vnd.github+json");
        conn.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
        conn.setRequestProperty("User-Agent", "ZonePractice-UpdateChecker");

        if (conn.getResponseCode() != 200) {
            conn.disconnect();
            return new JSONArray();
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } finally {
            conn.disconnect();
        }

        return new JSONArray(response.toString());
    }

    /**
     * Returns {@code true} when {@code candidate} is strictly newer than
     * {@code base}, using semantic versioning (MAJOR.MINOR.PATCH).
     */
    private static boolean isNewerThan(String candidate, String base) {
        int[] c = parseVersion(candidate);
        int[] b = parseVersion(base);

        for (int i = 0; i < Math.max(c.length, b.length); i++) {
            int cv = i < c.length ? c[i] : 0;
            int bv = i < b.length ? b[i] : 0;
            if (cv > bv) return true;
            if (cv < bv) return false;
        }
        return false; // equal
    }

    private static int[] parseVersion(String version) {
        String[] parts = version.split("[.\\-]");
        int[] nums = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                nums[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                nums[i] = 0;
            }
        }
        return nums;
    }
}

