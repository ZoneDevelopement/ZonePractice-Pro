package dev.nandi0813.practice.telemetry;

import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public enum ServerFingerprintUtil {
    ;

    private static final String FALLBACK_SERVER_ID = "unknown-host";
    private static volatile String cachedServerId;

    public static String getServerId() {
        String current = cachedServerId;
        if (current != null && !current.isBlank()) {
            return current;
        }

        String resolved = resolveServerIdFingerprint();
        cachedServerId = resolved;
        return resolved;
    }

    private static String resolveServerIdFingerprint() {
        List<String> macs = collectMacAddresses();
        if (!macs.isEmpty()) {
            macs.sort(String::compareTo);
            String joined = String.join("|", macs);
            return "macsha256:" + sha256Hex(joined);
        }

        String host = System.getenv("HOSTNAME");
        if (host == null || host.isBlank()) {
            host = System.getProperty("user.name", "unknown") + "@" + System.getProperty("os.name", "unknown");
        }
        return "hostuuid:" + UUID.nameUUIDFromBytes(host.getBytes(StandardCharsets.UTF_8));
    }

    private static List<String> collectMacAddresses() {
        List<String> result = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                    continue;
                }

                byte[] hardwareAddress = networkInterface.getHardwareAddress();
                String mac = normalizeMac(hardwareAddress);
                if (mac != null) {
                    result.add(mac);
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    private static String normalizeMac(byte[] mac) {
        if (mac == null || mac.length < 6) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for (byte b : mac) {
            builder.append(String.format(Locale.ROOT, "%02x", b));
        }

        String value = builder.toString();
        if (value.equals("000000000000") || value.equals("ffffffffffff")) {
            return null;
        }
        return value;
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format(Locale.ROOT, "%02x", b));
            }
            return builder.toString();
        } catch (Exception ignored) {
            return FALLBACK_SERVER_ID;
        }
    }
}
