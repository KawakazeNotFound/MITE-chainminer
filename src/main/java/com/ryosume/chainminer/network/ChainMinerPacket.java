package com.ryosume.chainminer.network;

import net.minecraft.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 网络数据包：通知服务器破坏连锁方块
 * 客户端 -> 服务器
 */
public class ChainMinerPacket extends Packet {
    private List<BlockBreak> blocks = new ArrayList<>();

    public ChainMinerPacket() {
    }

    public ChainMinerPacket(List<BlockBreak> blocks) {
        this.blocks = blocks;
    }

    @Override
    public void readPacketData(DataInput dataInput) throws IOException {
        int count = dataInput.readInt();
        blocks.clear();
        for (int i = 0; i < count; i++) {
            int x = dataInput.readInt();
            int y = dataInput.readInt();
            int z = dataInput.readInt();
            int face = dataInput.readByte();
            blocks.add(new BlockBreak(x, y, z, face));
        }
    }

    @Override
    public void writePacketData(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(blocks.size());
        for (BlockBreak block : blocks) {
            dataOutput.writeInt(block.x);
            dataOutput.writeInt(block.y);
            dataOutput.writeInt(block.z);
            dataOutput.writeByte(block.face);
        }
    }

    @Override
    public void processPacket(NetHandler netHandler) {
        if (netHandler instanceof NetServerHandler) {
            processServer((NetServerHandler) netHandler);
        }
    }

    private void processServer(NetServerHandler handler) {
        ServerPlayer player = handler.playerEntity;
        if (player == null || player.worldObj == null) {
            return;
        }

        World world = player.worldObj;
        
        if (blocks.isEmpty()) {
            return;
        }
        
        // 获取原始破坏点（第一个方块）
        BlockBreak originBlock = blocks.get(0);
        
        // 验证原始点距离
        double originDistSq = Math.pow(player.posX - (originBlock.x + 0.5), 2) +
                              Math.pow(player.posY - (originBlock.y + 0.5), 2) +
                              Math.pow(player.posZ - (originBlock.z + 0.5), 2);
        
        if (originDistSq > 256) {
            return;
        }

        int chainedMined = 0;
        
        // 遍历所有要破坏的方块（跳过第一个）
        for (int i = 1; i < blocks.size(); i++) {
            BlockBreak block = blocks.get(i);
            
            // 验证距离（防止作弊）
            double distSq = Math.pow(player.posX - (block.x + 0.5), 2) +
                           Math.pow(player.posY - (block.y + 0.5), 2) +
                           Math.pow(player.posZ - (block.z + 0.5), 2);
            
            if (distSq > 256) { // 16 blocks away
                continue;
            }

            int blockId = world.getBlockId(block.x, block.y, block.z);
            if (blockId <= 0) {
                continue;
            }

            Set<Integer> previousDrops = captureNearbyItemIds(world, block.x, block.y, block.z);

            if (player.theItemInWorldManager != null && player.theItemInWorldManager.tryHarvestBlock(block.x, block.y, block.z)) {
                chainedMined++;
                moveNewDropsToOrigin(world, previousDrops, block.x, block.y, block.z, originBlock.x, originBlock.y, originBlock.z);
            }
        }

        int totalMined = chainedMined + 1;
        handler.sendChatToPlayer("[Ryosume的连锁挖矿] 本次连锁: " + totalMined + " 个方块喵~", EnumChatFormatting.GREEN);
        System.out.println("[ChainMiner] " + player.getEntityName() + " chained " + totalMined + " blocks");
    }

    @Override
    public int getPacketSize() {
        return 4 + (blocks.size() * 13); // 4 bytes for count + 13 bytes per block
    }

    private static Set<Integer> captureNearbyItemIds(World world, int x, int y, int z) {
        Set<Integer> ids = new HashSet<Integer>();
        AxisAlignedBB area = AxisAlignedBB.getBoundingBox(
            x - 1.5D,
            y - 1.5D,
            z - 1.5D,
            x + 2.5D,
            y + 2.5D,
            z + 2.5D
        );

        List nearby = world.getEntitiesWithinAABB(EntityItem.class, area);
        for (Object obj : nearby) {
            EntityItem item = (EntityItem) obj;
            ids.add(item.entityId);
        }
        return ids;
    }

    private static void moveNewDropsToOrigin(World world, Set<Integer> previousDrops, int x, int y, int z, int originX, int originY, int originZ) {
        AxisAlignedBB area = AxisAlignedBB.getBoundingBox(
            x - 1.5D,
            y - 1.5D,
            z - 1.5D,
            x + 2.5D,
            y + 2.5D,
            z + 2.5D
        );

        List nearby = world.getEntitiesWithinAABB(EntityItem.class, area);
        double dropX = originX + 0.5D;
        double dropY = originY + 0.5D;
        double dropZ = originZ + 0.5D;

        for (Object obj : nearby) {
            EntityItem item = (EntityItem) obj;
            if (previousDrops.contains(item.entityId)) {
                continue;
            }

            item.setPosition(dropX, dropY, dropZ);
            item.motionX = 0.0D;
            item.motionY = 0.05D;
            item.motionZ = 0.0D;
        }
    }

    /**
     * 单个方块破坏信息
     */
    public static class BlockBreak {
        public final int x;
        public final int y;
        public final int z;
        public final int face;

        public BlockBreak(int x, int y, int z, int face) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.face = face;
        }
    }
}
