package com.example.chainminer.mixin;

import com.example.chainminer.ChainMinerConfig;
import com.example.chainminer.client.ChainMinerActivationKeyState;
import net.minecraft.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftChainMinerModeSwitchMixin {
    private static boolean wasSwitchComboActive = false;

    @Inject(method = "runTick()V", at = @At("HEAD"), require = 0)
    private void chainMiner$handleModeSwitchByWheel(CallbackInfo ci) {
        if (!ChainMinerConfig.isEnabled()) {
            wasSwitchComboActive = false;
            return;
        }

        boolean hasShift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        boolean hasActivationKey = ChainMinerActivationKeyState.isActivationDown();
        boolean switchComboActive = hasShift && hasActivationKey;
        if (!switchComboActive) {
            wasSwitchComboActive = false;
            return;
        }

        if (!wasSwitchComboActive) {
            Mouse.getDWheel();
            wasSwitchComboActive = true;
            return;
        }

        int scroll = Mouse.getDWheel();
        if (scroll > 0) {
            ChainMinerConfig.cycleMode();
            ChainMinerConfig.save();
        } else if (scroll < 0) {
            ChainMinerConfig.cycleModeBackward();
            ChainMinerConfig.save();
        }
    }
}
