package com.ryosume.chainminer.mixin;

import com.ryosume.chainminer.client.ChainMinerKeyBindingBridge;
import net.minecraft.GameSettings;
import net.minecraft.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameSettings.class)
public abstract class GameSettingsChainMinerKeyBindingMixin {
    @Shadow
    public KeyBinding[] keyBindings;

    @Inject(method = "initKeybindings()V", at = @At("RETURN"), require = 0)
    private void chainMiner$injectKeyBindingIntoControls(CallbackInfo ci) {
        ChainMinerKeyBindingBridge.ensureRegistered((GameSettings) (Object) this);
    }

    @Inject(method = "setKeyBinding(II)V", at = @At("RETURN"), require = 0)
    private void chainMiner$syncConfigWhenControlsChanged(int index, int keyCode, CallbackInfo ci) {
        ChainMinerKeyBindingBridge.onKeyBindingChanged((GameSettings) (Object) this, index);
    }

    @Inject(method = "loadOptions()V", at = @At("RETURN"), require = 0)
    private void chainMiner$syncConfigFromLoadedOptions(CallbackInfo ci) {
        ChainMinerKeyBindingBridge.syncConfigFromSettings((GameSettings) (Object) this);
    }
}
