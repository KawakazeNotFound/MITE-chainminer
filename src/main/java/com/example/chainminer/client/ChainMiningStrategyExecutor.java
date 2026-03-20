package com.example.chainminer.client;

import com.example.chainminer.ChainMinerConfig;
import com.example.chainminer.network.ChainMinerPacket;
import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.Minecraft;
import net.minecraft.RaycastCollision;
import net.minecraft.Vec3;
import net.minecraft.World;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ChainMiningStrategyExecutor {
    private static final int[][] OFFSETS_26 = buildOffsets26();

    private ChainMiningStrategyExecutor() {
    }

    public static List<ChainMinerPacket.BlockBreak> executeChainMining(int originX, int originY, int originZ, int blockId, int sideOrdinal) {
        Minecraft minecraft = Minecraft.getMinecraft();
        World world = minecraft == null ? null : minecraft.theWorld;
        if (world == null) {
            return new ArrayList<>();
        }

        List<ChainMinerPacket.BlockBreak> result = new ArrayList<>();
        result.add(new ChainMinerPacket.BlockBreak(originX, originY, originZ, sideOrdinal));

        switch (ChainMinerConfig.getCurrentMode()) {
            case SHAPELESS:
                executeShapeless(world, originX, originY, originZ, blockId, sideOrdinal, result);
                break;
            case TUNNEL:
                executeTunnel(world, minecraft.thePlayer, originX, originY, originZ, blockId, sideOrdinal, result);
                break;
            case STAIRCASE:
                executeStaircase(world, minecraft.thePlayer, originX, originY, originZ, blockId, sideOrdinal, result);
                break;
            default:
                break;
        }

        return result;
    }

    public static int previewCurrentTargetChainCount() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.theWorld == null || minecraft.thePlayer == null) {
            return 0;
        }

        RaycastCollision hitResult = minecraft.objectMouseOver;
        if (hitResult == null || !hitResult.isBlock()) {
            return 0;
        }

        int x = hitResult.block_hit_x;
        int y = hitResult.block_hit_y;
        int z = hitResult.block_hit_z;
        if (y < 0 || y > 255) {
            return 0;
        }

        int blockId = hitResult.getBlockHitID();
        if (blockId <= 0) {
            return 0;
        }

        int sideOrdinal = hitResult.face_hit == null ? 0 : hitResult.face_hit.ordinal();
        List<ChainMinerPacket.BlockBreak> blocks = executeChainMining(x, y, z, blockId, sideOrdinal);
        return blocks.size();
    }

    private static void executeShapeless(World world, int originX, int originY, int originZ, int blockId, int sideOrdinal, List<ChainMinerPacket.BlockBreak> result) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<Long> visited = new HashSet<>();

        queue.add(new BlockPos(originX, originY, originZ));
        visited.add(pack(originX, originY, originZ));

        int queued = 0;
        int chainLimit = ChainMinerConfig.getChainLimit();

        while (!queue.isEmpty() && queued < chainLimit) {
            BlockPos current = queue.removeFirst();

            for (int[] offset : OFFSETS_26) {
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

                if (world.getBlockId(nx, ny, nz) != blockId) {
                    continue;
                }

                result.add(new ChainMinerPacket.BlockBreak(nx, ny, nz, sideOrdinal));
                queued++;
                queue.addLast(new BlockPos(nx, ny, nz));

                if (queued >= chainLimit) {
                    break;
                }
            }
        }
    }

    private static void executeTunnel(World world, EntityPlayer player, int originX, int originY, int originZ, int blockId, int sideOrdinal, List<ChainMinerPacket.BlockBreak> result) {
        if (player == null) {
            return;
        }

        Vec3 lookVec = player.getLookVec();
        if (lookVec == null) {
            return;
        }

        int dirX = (int) Math.round(lookVec.xCoord);
        int dirY = (int) Math.round(lookVec.yCoord);
        int dirZ = (int) Math.round(lookVec.zCoord);

        int chainLimit = ChainMinerConfig.getChainLimit() - 1;
        for (int i = 1; i <= chainLimit && i <= 3; i++) {
            int nx = originX + dirX * i;
            int ny = originY + dirY * i;
            int nz = originZ + dirZ * i;

            if (ny < 0 || ny > 255) {
                break;
            }

            int currentBlockId = world.getBlockId(nx, ny, nz);
            if (currentBlockId != blockId) {
                break;
            }

            result.add(new ChainMinerPacket.BlockBreak(nx, ny, nz, sideOrdinal));
        }
    }

    private static void executeStaircase(World world, EntityPlayer player, int originX, int originY, int originZ, int blockId, int sideOrdinal, List<ChainMinerPacket.BlockBreak> result) {
        if (player == null) {
            return;
        }

        Vec3 lookVec = player.getLookVec();
        if (lookVec == null) {
            return;
        }

        int dirX = (int) Math.round(lookVec.xCoord);
        int dirZ = (int) Math.round(lookVec.zCoord);
        int dirY = player.rotationPitch > 0 ? -1 : 1;

        int chainLimit = ChainMinerConfig.getChainLimit() - 1;
        for (int step = 1; step <= chainLimit && step <= 10; step++) {
            int nx = originX + dirX * step;
            int ny = originY + dirY * step;
            int nz = originZ + dirZ * step;

            if (ny < 0 || ny > 255) {
                break;
            }

            int currentBlockId = world.getBlockId(nx, ny, nz);
            if (currentBlockId != blockId) {
                break;
            }

            result.add(new ChainMinerPacket.BlockBreak(nx, ny, nz, sideOrdinal));
        }
    }

    private static int[][] buildOffsets26() {
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

    private static long pack(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38)
                | ((long) (z & 0x3FFFFFF) << 12)
                | (y & 0xFFF);
    }

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
}
