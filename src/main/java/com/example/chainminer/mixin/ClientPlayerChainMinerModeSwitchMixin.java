package com.example.chainminer.mixin;

import com.example.chainminer.ChainMinerConfig;
import com.example.chainminer.client.ChainMinerActivationKeyState;
import net.minecraft.EntityClientPlayerMP;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityClientPlayerMP.class)
public abstract class ClientPlayerChainMinerModeSwitchMixin {
    private static int lastMouseScroll = 0;

    @Inject(method = "onUpdate()V", at = @At("HEAD"), require = 0)
    private void chainMiner$onUpdateCheckModeSwitch(CallbackInfo ci) {
        if (!ChainMinerConfig.isEnabled()) {
            lastMouseScroll = 0;
            return;
        }

        boolean hasShift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        boolean hasActivationKey = ChainMinerActivationKeyState.isActivationDown();

        if (!hasShift || !hasActivationKey) {
            lastMouseScroll = 0;
            return;
        }

        int currentScroll = Mouse.getDWheel();
        if (lastMouseScroll == currentScroll || currentScroll == 0) {
            return;
        }

        lastMouseScroll = currentScroll;

        if (currentScroll > 0) {
            ChainMinerConfig.cycleMode();
            ChainMinerConfig.save();
        }
    }
}
