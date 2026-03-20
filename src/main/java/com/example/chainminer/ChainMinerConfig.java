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
    private static final String KEY_HOLD_BINDING = "holdBinding";
    private static final String KEY_CHAIN_LIMIT = "chainLimit";
    private static final String KEY_HUD_X = "hudX";
    private static final String KEY_HUD_Y = "hudY";

    private static boolean enabled = true;
    private static String holdBinding = "KEY:GRAVE";
    private static int chainLimit = 64;
    private static int hudX = 2;
    private static int hudY = 22;
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
        holdBinding = parseBinding(properties.getProperty(KEY_HOLD_BINDING),
            properties.getProperty(KEY_HOLD_KEY),
            "KEY:GRAVE");
        chainLimit = parseInt(properties.getProperty(KEY_CHAIN_LIMIT), 64, 1, 512);
        hudX = parseInt(properties.getProperty(KEY_HUD_X), 2, 0, 10000);
        hudY = parseInt(properties.getProperty(KEY_HUD_Y), 22, 0, 10000);

        save();
    }

    public static void save() {
        if (configFile == null) {
            return;
        }

        Properties output = new Properties();
        output.setProperty(KEY_ENABLED, String.valueOf(enabled));
        output.setProperty(KEY_HOLD_BINDING, holdBinding);
        output.setProperty(KEY_HOLD_KEY, getHoldKeyName());
        output.setProperty(KEY_CHAIN_LIMIT, String.valueOf(chainLimit));
        output.setProperty(KEY_HUD_X, String.valueOf(hudX));
        output.setProperty(KEY_HUD_Y, String.valueOf(hudY));

        try (FileOutputStream outputStream = new FileOutputStream(configFile)) {
            output.store(outputStream, "ChainMiner config");
        } catch (IOException ignored) {
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static String getHoldKeyName() {
        int split = holdBinding.indexOf(':');
        if (split <= 0 || split >= holdBinding.length() - 1) {
            return "GRAVE";
        }

        String type = holdBinding.substring(0, split);
        String value = holdBinding.substring(split + 1);
        if (!"KEY".equals(type)) {
            return "GRAVE";
        }
        return value;
    }

    public static String getHoldBinding() {
        return holdBinding;
    }

    public static boolean isMouseBinding() {
        return holdBinding.startsWith("MOUSE:");
    }

    public static int getMouseButton() {
        if (!isMouseBinding()) {
            return -1;
        }

        int split = holdBinding.indexOf(':');
        if (split <= 0 || split >= holdBinding.length() - 1) {
            return -1;
        }

        try {
            int button = Integer.parseInt(holdBinding.substring(split + 1));
            if (button < 0) {
                return -1;
            }
            return button;
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    public static int getChainLimit() {
        return chainLimit;
    }

    public static int getHudX() {
        return hudX;
    }

    public static int getHudY() {
        return hudY;
    }

    public static void setEnabled(boolean enabled) {
        ChainMinerConfig.enabled = enabled;
    }

    public static void setHoldKeyName(String holdKeyName) {
        String keyName = parseKeyName(holdKeyName, getHoldKeyName());
        ChainMinerConfig.holdBinding = "KEY:" + keyName;
    }

    public static void setHoldMouseButton(int mouseButton) {
        if (mouseButton < 0) {
            return;
        }
        ChainMinerConfig.holdBinding = "MOUSE:" + mouseButton;
    }

    public static void setHoldBinding(String holdBinding) {
        ChainMinerConfig.holdBinding = parseBinding(holdBinding, null, ChainMinerConfig.holdBinding);
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

    public static void setHudPosition(int x, int y) {
        if (x < 0) {
            x = 0;
        } else if (x > 10000) {
            x = 10000;
        }

        if (y < 0) {
            y = 0;
        } else if (y > 10000) {
            y = 10000;
        }

        hudX = x;
        hudY = y;
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

    private static String parseBinding(String preferred, String legacyKey, String fallback) {
        String parsedPreferred = parseBindingCore(preferred);
        if (parsedPreferred != null) {
            return parsedPreferred;
        }

        String parsedLegacy = parseBindingCore(legacyKey);
        if (parsedLegacy != null) {
            return parsedLegacy;
        }

        return fallback;
    }

    private static String parseBindingCore(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        String upper = trimmed.toUpperCase();

        if (upper.startsWith("KEY:")) {
            String keyName = parseKeyName(upper.substring(4), "GRAVE");
            return "KEY:" + keyName;
        }

        if (upper.startsWith("MOUSE:")) {
            String rawButton = upper.substring(6).trim();
            if (rawButton.isEmpty()) {
                return null;
            }

            try {
                int button = Integer.parseInt(rawButton);
                if (button < 0) {
                    return null;
                }
                return "MOUSE:" + button;
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return "KEY:" + parseKeyName(upper, "GRAVE");
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