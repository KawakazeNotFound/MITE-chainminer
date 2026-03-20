package com.ryosume.chainminer.client;

import com.ryosume.chainminer.ChainMinerConfig;
import net.minecraft.GameSettings;
import net.minecraft.KeyBinding;
import net.minecraft.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public final class ChainMinerKeyBindingBridge {
    private static final String KEY_DESC = "连锁挖矿激活键";
    private static final KeyBinding CHAIN_MINER_KEY = new KeyBinding(KEY_DESC, Keyboard.KEY_GRAVE);
    private static boolean initializedFromConfig = false;

    private ChainMinerKeyBindingBridge() {
    }

    public static void ensureRegistered(GameSettings settings) {
        if (settings == null || settings.keyBindings == null) {
            return;
        }

        if (!containsBinding(settings.keyBindings)) {
            KeyBinding[] expanded = new KeyBinding[settings.keyBindings.length + 1];
            System.arraycopy(settings.keyBindings, 0, expanded, 0, settings.keyBindings.length);
            expanded[expanded.length - 1] = CHAIN_MINER_KEY;
            settings.keyBindings = expanded;
            KeyBinding.resetKeyBindingArrayAndHash();
        }

        syncKeyCodeFromConfigOnce();
    }

    public static void syncFromConfigNow() {
        initializedFromConfig = false;
        syncKeyCodeFromConfigOnce();
    }

    public static void syncConfigFromSettings(GameSettings settings) {
        if (settings == null || settings.keyBindings == null) {
            return;
        }

        for (int i = 0; i < settings.keyBindings.length; i++) {
            KeyBinding binding = settings.keyBindings[i];
            if (binding == CHAIN_MINER_KEY || (binding != null && KEY_DESC.equals(binding.keyDescription))) {
                applyKeyCodeToConfig(binding.keyCode);
                return;
            }
        }
    }

    public static int getRuntimeKeyCode() {
        return CHAIN_MINER_KEY.keyCode;
    }

    public static boolean isActivationDownByRuntimeBinding() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.gameSettings == null) {
            return false;
        }

        ensureRegistered(minecraft.gameSettings);

        int keyCode = CHAIN_MINER_KEY.keyCode;
        if (keyCode < 0) {
            if (!Mouse.isCreated()) {
                return false;
            }

            int mouseButton = keyCode + 100;
            return mouseButton >= 0 && Mouse.isButtonDown(mouseButton);
        }

        if (!Keyboard.isCreated()) {
            return false;
        }

        return Keyboard.isKeyDown(keyCode);
    }

    public static void onKeyBindingChanged(GameSettings settings, int index) {
        if (settings == null || settings.keyBindings == null) {
            return;
        }

        if (index < 0 || index >= settings.keyBindings.length) {
            return;
        }

        KeyBinding binding = settings.keyBindings[index];
        if (binding == CHAIN_MINER_KEY || (binding != null && KEY_DESC.equals(binding.keyDescription))) {
            applyKeyCodeToConfig(binding.keyCode);
        }
    }

    private static boolean containsBinding(KeyBinding[] keyBindings) {
        for (KeyBinding binding : keyBindings) {
            if (binding == CHAIN_MINER_KEY || (binding != null && KEY_DESC.equals(binding.keyDescription))) {
                return true;
            }
        }
        return false;
    }

    private static void syncKeyCodeFromConfigOnce() {
        if (initializedFromConfig) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null) {
            return;
        }

        ChainMinerConfig.load(minecraft.mcDataDir);

        if (ChainMinerConfig.isMouseBinding()) {
            int mouseButton = ChainMinerConfig.getMouseButton();
            CHAIN_MINER_KEY.keyCode = mouseButton >= 0 ? (-100 + mouseButton) : Keyboard.KEY_GRAVE;
        } else {
            String keyName = ChainMinerConfig.getHoldKeyName();
            int keyCode = Keyboard.getKeyIndex(keyName);
            CHAIN_MINER_KEY.keyCode = keyCode > 0 ? keyCode : Keyboard.KEY_GRAVE;
        }

        initializedFromConfig = true;
        KeyBinding.resetKeyBindingArrayAndHash();
    }

    private static void applyKeyCodeToConfig(int keyCode) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft != null) {
            ChainMinerConfig.load(minecraft.mcDataDir);
        }

        if (keyCode < 0) {
            int mouseButton = keyCode + 100;
            if (mouseButton >= 0) {
                ChainMinerConfig.setHoldMouseButton(mouseButton);
            } else {
                ChainMinerConfig.setHoldKeyName("GRAVE");
            }
        } else {
            String keyName = Keyboard.getKeyName(keyCode);
            if (keyName == null || keyName.isEmpty()) {
                keyName = "GRAVE";
            }
            ChainMinerConfig.setHoldKeyName(keyName);
        }

        ChainMinerConfig.save();
    }
}
