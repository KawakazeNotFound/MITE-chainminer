package com.ryosume.chainminer.mixin;

import com.ryosume.chainminer.ChainMinerConfig;
import com.ryosume.chainminer.client.ChainMinerActivationKeyState;
import net.minecraft.InventoryPlayer;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryPlayer.class)
public abstract class InventoryPlayerChainMinerScrollLockMixin {
    @Inject(method = "changeCurrentItem(I)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void chainMiner$lockHotbarScrollWhileSwitchMode(int direction, CallbackInfo ci) {
        if (!ChainMinerConfig.isEnabled()) {
            return;
        }

        boolean hasShift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        boolean hasActivationKey = ChainMinerActivationKeyState.isActivationDown();
        if (hasShift && hasActivationKey) {
            ci.cancel();
        }
    }
}
