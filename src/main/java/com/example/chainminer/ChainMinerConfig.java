package com.example.chainminer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public final class ChainMinerConfig {
    private static final String FILE_NAME = "chainminer.properties";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_HOLD_KEY = "holdKey";
    private static final String KEY_CHAIN_LIMIT = "chainLimit";

    private static boolean enabled = true;
    private static String holdKeyName = "GRAVE";
    private static int chainLimit = 64;
    private static File configFile;

    private ChainMinerConfig() {
    }

    public static void load(File mcDataDir) {
        File baseDir = mcDataDir == null ? new File(".") : mcDataDir;
        File configDir = new File(baseDir, "config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        configFile = new File(configDir, FILE_NAME);
        Properties properties = new Properties();
        if (configFile.exists()) {
            try (FileInputStream inputStream = new FileInputStream(configFile)) {
                properties.load(inputStream);
            } catch (IOException ignored) {
            }
        }

        enabled = parseBoolean(properties.getProperty(KEY_ENABLED), true);
        holdKeyName = parseKeyName(properties.getProperty(KEY_HOLD_KEY), "GRAVE");
        chainLimit = parseInt(properties.getProperty(KEY_CHAIN_LIMIT), 64, 1, 512);

        save();
    }

    public static void save() {
        if (configFile == null) {
            return;
        }

        Properties output = new Properties();
        output.setProperty(KEY_ENABLED, String.valueOf(enabled));
        output.setProperty(KEY_HOLD_KEY, holdKeyName);
        output.setProperty(KEY_CHAIN_LIMIT, String.valueOf(chainLimit));

        try (FileOutputStream outputStream = new FileOutputStream(configFile)) {
            output.store(outputStream, "ChainMiner config");
        } catch (IOException ignored) {
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static String getHoldKeyName() {
        return holdKeyName;
    }

    public static int getChainLimit() {
        return chainLimit;
    }

    public static void setEnabled(boolean enabled) {
        ChainMinerConfig.enabled = enabled;
    }

    public static void setHoldKeyName(String holdKeyName) {
        ChainMinerConfig.holdKeyName = parseKeyName(holdKeyName, ChainMinerConfig.holdKeyName);
    }

    public static void setChainLimit(int chainLimit) {
        if (chainLimit < 1) {
            ChainMinerConfig.chainLimit = 1;
        } else if (chainLimit > 512) {
            ChainMinerConfig.chainLimit = 512;
        } else {
            ChainMinerConfig.chainLimit = chainLimit;
        }
    }

    private static boolean parseBoolean(String value, boolean fallback) {
        if (value == null) {
            return fallback;
        }
        return "true".equalsIgnoreCase(value) || ("false".equalsIgnoreCase(value) ? false : fallback);
    }

    private static String parseKeyName(String value, String fallback) {
        if (value == null) {
            return fallback;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return fallback;
        }

        return trimmed.toUpperCase();
    }

    private static int parseInt(String value, int fallback, int min, int max) {
        if (value == null) {
            return fallback;
        }

        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < min) {
                return min;
            }
            if (parsed > max) {
                return max;
            }
            return parsed;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}