package com.example.chainminer.client;

import com.example.chainminer.ChainMinerConfig;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.Locale;

public final class ChainMinerActivationKeyState {
    private static String lastHoldBinding = "";
    private static boolean lastIsMouseBinding = false;
    private static int lastHoldKeyCode = Keyboard.KEY_GRAVE;
    private static int lastHoldMouseButton = -1;

    private ChainMinerActivationKeyState() {
    }

    public static boolean isActivationDown() {
        if (!ChainMinerConfig.isEnabled()) {
            return false;
        }

        String holdBinding = ChainMinerConfig.getHoldBinding();
        if (!holdBinding.equals(lastHoldBinding)) {
            lastHoldBinding = holdBinding;

            if (ChainMinerConfig.isMouseBinding()) {
                lastIsMouseBinding = true;
                lastHoldMouseButton = ChainMinerConfig.getMouseButton();
            } else {
                lastIsMouseBinding = false;
                String holdKeyName = ChainMinerConfig.getHoldKeyName();
                int keyCode = Keyboard.getKeyIndex(holdKeyName.toUpperCase(Locale.ROOT));
                lastHoldKeyCode = keyCode > 0 ? keyCode : Keyboard.KEY_GRAVE;
            }
        }

        if (lastIsMouseBinding) {
            if (!Mouse.isCreated()) {
                return false;
            }
            return lastHoldMouseButton >= 0 && Mouse.isButtonDown(lastHoldMouseButton);
        }

        if (!Keyboard.isCreated()) {
            return false;
        }

        return Keyboard.isKeyDown(lastHoldKeyCode);
    }
}
