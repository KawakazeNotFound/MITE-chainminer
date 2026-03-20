package com.example.chainminer.mixin;

import com.example.chainminer.ChainMinerConfig;
import com.example.chainminer.client.ChainMinerActivationKeyState;
import net.minecraft.FontRenderer;
import net.minecraft.GuiIngame;
import net.minecraft.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public class GuiIngameChainMinerHudMixin {
    private static final String ACTIVATED_TEXT = "[ChainMiner] 已激活";
    private static final int HUD_COLOR = 0x55FF55;
    private static boolean CONFIG_LOADED = false;

    @Inject(method = "renderGameOverlay(FZII)V", at = @At("TAIL"), require = 0)
    private void chainMiner$renderActivationHint(float partialTicks, boolean hasScreen, int mouseX, int mouseY, CallbackInfo ci) {
        if (hasScreen || !ChainMinerActivationKeyState.isActivationDown()) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null) {
            return;
        }

        FontRenderer fontRenderer = minecraft.fontRenderer;
        if (fontRenderer == null) {
            return;
        }

        if (!CONFIG_LOADED) {
            ChainMinerConfig.load(minecraft.mcDataDir);
            CONFIG_LOADED = true;
        }

        fontRenderer.drawStringWithShadow(ACTIVATED_TEXT, ChainMinerConfig.getHudX(), ChainMinerConfig.getHudY(), HUD_COLOR);
    }
}
