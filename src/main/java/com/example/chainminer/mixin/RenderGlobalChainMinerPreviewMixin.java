package com.ryosume.chainminer.mixin;

import com.ryosume.chainminer.ChainMinerConfig;
import com.ryosume.chainminer.client.ChainMinerActivationKeyState;
import com.ryosume.chainminer.client.ChainMiningStrategyExecutor;
import com.ryosume.chainminer.network.ChainMinerPacket;
import net.minecraft.EntityPlayer;
import net.minecraft.RaycastCollision;
import net.minecraft.RenderGlobal;
import net.minecraft.Tessellator;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RenderGlobal.class)
public abstract class RenderGlobalChainMinerPreviewMixin {
    @Inject(method = "drawSelectionBox(Lnet/minecraft/EntityPlayer;Lnet/minecraft/RaycastCollision;IF)V", at = @At("TAIL"), require = 0)
    private void chainMiner$renderPreviewSelectionBoxes(EntityPlayer player, RaycastCollision hitResult, int mode, float partialTicks, CallbackInfo ci) {
        if (!ChainMinerConfig.isEnabled() || !ChainMinerActivationKeyState.isActivationDown()) {
            return;
        }

        if (player == null || hitResult == null || !hitResult.isBlock()) {
            return;
        }

        List<ChainMinerPacket.BlockBreak> previewBlocks = ChainMiningStrategyExecutor.previewTargetChainBlocks(hitResult);
        if (previewBlocks.isEmpty()) {
            return;
        }

        double playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(false);

        GL11.glLineWidth(2.0F);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(GL11.GL_LINES);
        tessellator.setColorRGBA_F(1.0F, 1.0F, 1.0F, 0.95F);
        for (ChainMinerPacket.BlockBreak block : previewBlocks) {
            drawBoxLines(tessellator,
                block.x - playerX - 0.004D,
                block.y - playerY - 0.004D,
                block.z - playerZ - 0.004D,
                block.x - playerX + 1.004D,
                block.y - playerY + 1.004D,
                block.z - playerZ + 1.004D);
        }
        tessellator.draw();

        GL11.glLineWidth(1.4F);
        tessellator.startDrawing(GL11.GL_LINES);
        tessellator.setColorRGBA_F(0.22F, 0.95F, 1.0F, 0.45F);
        for (ChainMinerPacket.BlockBreak block : previewBlocks) {
            drawBoxLines(tessellator,
                block.x - playerX - 0.002D,
                block.y - playerY - 0.002D,
                block.z - playerZ - 0.002D,
                block.x - playerX + 1.002D,
                block.y - playerY + 1.002D,
                block.z - playerZ + 1.002D);
        }
        tessellator.draw();

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private static void drawBoxLines(Tessellator tessellator, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        tessellator.addVertex(minX, minY, minZ);
        tessellator.addVertex(maxX, minY, minZ);
        tessellator.addVertex(maxX, minY, minZ);
        tessellator.addVertex(maxX, minY, maxZ);
        tessellator.addVertex(maxX, minY, maxZ);
        tessellator.addVertex(minX, minY, maxZ);
        tessellator.addVertex(minX, minY, maxZ);
        tessellator.addVertex(minX, minY, minZ);

        tessellator.addVertex(minX, maxY, minZ);
        tessellator.addVertex(maxX, maxY, minZ);
        tessellator.addVertex(maxX, maxY, minZ);
        tessellator.addVertex(maxX, maxY, maxZ);
        tessellator.addVertex(maxX, maxY, maxZ);
        tessellator.addVertex(minX, maxY, maxZ);
        tessellator.addVertex(minX, maxY, maxZ);
        tessellator.addVertex(minX, maxY, minZ);

        tessellator.addVertex(minX, minY, minZ);
        tessellator.addVertex(minX, maxY, minZ);
        tessellator.addVertex(maxX, minY, minZ);
        tessellator.addVertex(maxX, maxY, minZ);
        tessellator.addVertex(maxX, minY, maxZ);
        tessellator.addVertex(maxX, maxY, maxZ);
        tessellator.addVertex(minX, minY, maxZ);
        tessellator.addVertex(minX, maxY, maxZ);
    }
}
