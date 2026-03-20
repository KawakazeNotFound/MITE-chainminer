package com.ryosume.chainminer.mixin;

import com.ryosume.chainminer.ChainMinerConfig;
import com.ryosume.chainminer.ChainMinerMode;
import com.ryosume.chainminer.client.ChainMinerActivationKeyState;
import com.ryosume.chainminer.client.ChainMiningStrategyExecutor;
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
    private static final int MODE_COLOR = 0xFFFF00;
    private static final int COUNT_COLOR = 0x55FFFF;

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

        int hudX = ChainMinerConfig.getHudX();
        int hudY = ChainMinerConfig.getHudY();

        fontRenderer.drawStringWithShadow(ACTIVATED_TEXT, hudX, hudY, HUD_COLOR);

        ChainMinerMode mode = ChainMinerConfig.getCurrentMode();
        String modeText = "模式: " + mode.getDisplayName();
        fontRenderer.drawStringWithShadow(modeText, hudX, hudY + 10, MODE_COLOR);

        int previewCount = ChainMiningStrategyExecutor.previewCurrentTargetChainCount();
        String countText = "预计连锁: " + previewCount + " 个";
        fontRenderer.drawStringWithShadow(countText, hudX, hudY + 20, COUNT_COLOR);
    }
}
