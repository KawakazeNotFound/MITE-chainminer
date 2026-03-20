package com.example.chainminer.mixin;

import com.example.chainminer.ChainMinerConfig;
import com.example.chainminer.client.ChainMinerActivationKeyState;
import com.example.chainminer.network.ChainMinerPacket;
import net.minecraft.EnumFace;
import net.minecraft.Minecraft;
import net.minecraft.PlayerControllerMP;
import net.minecraft.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(PlayerControllerMP.class)
public abstract class ItemInWorldManagerChainMinerMixin {
    @Invoker("onPlayerDestroyBlock")
    protected abstract boolean chainMiner$invokeDestroyBlock(int x, int y, int z, EnumFace side);

    @Unique
        private static final int[][] OFFSETS = buildOffsets();

    @Unique
    private static final ThreadLocal<Boolean> CHAINING = ThreadLocal.withInitial(() -> false);

    @Unique
    private static final ThreadLocal<BreakContext> BREAK_CONTEXT = new ThreadLocal<BreakContext>();

    @Unique
    private static boolean CONFIG_LOADED = false;

    @Unique
    private static long LAST_ACTIVATION_HINT_MS = 0L;

    @Inject(method = "onPlayerDestroyBlock", at = @At("HEAD"))
    private void captureBreakContext(int x, int y, int z, EnumFace side, CallbackInfoReturnable<Boolean> cir) {
        if (Boolean.TRUE.equals(CHAINING.get())) {
            return;
        }

        ensureConfigLoaded();
        if (!isActivationKeyDown()) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        World world = minecraft == null ? null : minecraft.theWorld;
        if (world == null) {
            return;
        }

        int blockId = world.getBlockId(x, y, z);
        if (blockId <= 0) {
            return;
        }

        BREAK_CONTEXT.set(new BreakContext(x, y, z, side, blockId));
    }

    @Inject(method = "onPlayerDestroyBlock", at = @At("RETURN"))
    private void onHarvested(int x, int y, int z, EnumFace side, CallbackInfoReturnable<Boolean> cir) {
        BreakContext context = BREAK_CONTEXT.get();
        BREAK_CONTEXT.remove();

        if (context == null || !cir.getReturnValue()) {
            return;
        }

        if (Boolean.TRUE.equals(CHAINING.get())) {
            return;
        }

        CHAINING.set(true);
        try {
            mineConnectedBlocks(context);
        } finally {
            CHAINING.set(false);
        }
    }

    @Unique
    private void mineConnectedBlocks(BreakContext context) {
        Minecraft minecraft = Minecraft.getMinecraft();
        World world = minecraft == null ? null : minecraft.theWorld;
        if (world == null) {
            return;
        }

        ArrayDeque<BlockPos> queue = new ArrayDeque<BlockPos>();
        Set<Long> visited = new HashSet<Long>();
        List<ChainMinerPacket.BlockBreak> blocksToBreak = new ArrayList<>();

        queue.add(new BlockPos(context.x, context.y, context.z));
        visited.add(pack(context.x, context.y, context.z));
        blocksToBreak.add(new ChainMinerPacket.BlockBreak(context.x, context.y, context.z, context.side.ordinal()));

        int queued = 0;
        int chainLimit = ChainMinerConfig.getChainLimit();
        
        while (!queue.isEmpty() && queued < chainLimit) {
            BlockPos current = queue.removeFirst();

            for (int[] offset : OFFSETS) {
                int nx = current.x + offset[0];
                int ny = current.y + offset[1];
                int nz = current.z + offset[2];

                if (ny < 0 || ny > 255) {
                    continue;
                }

                long packed = pack(nx, ny, nz);
                if (!visited.add(packed)) {
                    continue;
                }

                if (world.getBlockId(nx, ny, nz) != context.blockId) {
                    continue;
                }

                // 添加到破坏列表而不是直接破坏
                blocksToBreak.add(new ChainMinerPacket.BlockBreak(nx, ny, nz, context.side.ordinal()));
                queued++;
                queue.addLast(new BlockPos(nx, ny, nz));

                if (queued >= chainLimit) {
                    break;
                }
            }
        }

        // 发送数据包到服务器
        if (blocksToBreak.size() > 1) {
            long now = System.currentTimeMillis();
            if (minecraft.ingameGUI != null && now - LAST_ACTIVATION_HINT_MS > 800L) {
                LAST_ACTIVATION_HINT_MS = now;
            }
            // 只有当有连锁方块时才发送（>1是因为包含原始方块）
            minecraft.thePlayer.sendQueue.addToSendQueue(new ChainMinerPacket(blocksToBreak));
        }
    }

    @Unique
    private static int[][] buildOffsets() {
        int[][] offsets = new int[26][3];
        int index = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    offsets[index][0] = dx;
                    offsets[index][1] = dy;
                    offsets[index][2] = dz;
                    index++;
                }
            }
        }
        return offsets;
    }

    @Unique
    private static void ensureConfigLoaded() {
        if (CONFIG_LOADED) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        ChainMinerConfig.load(minecraft == null ? null : minecraft.mcDataDir);
        CONFIG_LOADED = true;
    }

    @Unique
    private static boolean isActivationKeyDown() {
        return ChainMinerActivationKeyState.isActivationDown();
    }

    @Unique
    private static long pack(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38)
                | ((long) (z & 0x3FFFFFF) << 12)
                | (y & 0xFFF);
    }

    @Unique
    private static final class BlockPos {
        private final int x;
        private final int y;
        private final int z;

        private BlockPos(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    @Unique
    private static final class BreakContext {
        private final int x;
        private final int y;
        private final int z;
        private final EnumFace side;
        private final int blockId;

        private BreakContext(int x, int y, int z, EnumFace side, int blockId) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.side = side;
            this.blockId = blockId;
        }
    }
}