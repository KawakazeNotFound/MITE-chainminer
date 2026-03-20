package com.ryosume.chainminer.network;

import net.minecraft.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        // NetServerHandler 有 playerEntity 字段
        EntityPlayer player = handler.playerEntity;
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

        // 收集要破坏的方块信息
        List<BlockBreakPosition> breakPositions = new ArrayList<>();
        
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

            // 获取方块和元数据
            int blockId = world.getBlockId(block.x, block.y, block.z);
            if (blockId <= 0) {
                continue;
            }
            
            Block blockObj = Block.blocksList[blockId];
            if (blockObj == null) {
                continue;
            }
            
            int metadata = world.getBlockMetadata(block.x, block.y, block.z);
            
            // 生成掉落物但不生成实体，只收集ItemStack
            BlockBreakInfo breakInfo = new BlockBreakInfo(world, block.x, block.y, block.z);
            breakInfo.setBlock(blockObj, metadata);
            // 记录要破坏的方块位置
            breakPositions.add(new BlockBreakPosition(block.x, block.y, block.z, blockObj, metadata));
        }
        
        // 在原始方块位置收集该方块的掉落物
        int originBlockId = world.getBlockId(originBlock.x, originBlock.y, originBlock.z);
        if (originBlockId > 0) {
            Block originBlockObj = Block.blocksList[originBlockId];
            if (originBlockObj != null) {
                int originMetadata = world.getBlockMetadata(originBlock.x, originBlock.y, originBlock.z);
                BlockBreakInfo originBreakInfo = new BlockBreakInfo(world, originBlock.x, originBlock.y, originBlock.z);
                originBreakInfo.setBlock(originBlockObj, originMetadata);
                // 这个在客户端已经生成了，服务器这里跳过
            }
        }
        
        // 现在在原始点生成所有连锁方块的掉落物
        for (BlockBreakPosition breakPos : breakPositions) {
            BlockBreakInfo breakInfo = new BlockBreakInfo(world, originBlock.x, originBlock.y, originBlock.z);
            breakInfo.setBlock(breakPos.block, breakPos.metadata);
            breakPos.block.dropBlockAsEntityItem(breakInfo);
            
            // 破坏该方块
            world.setBlockToAir(breakPos.x, breakPos.y, breakPos.z);
        }

        int totalMined = breakPositions.size() + 1;
        handler.sendChatToPlayer("[Ryosume的连锁挖矿] 本次连锁: " + totalMined + " 个方块喵~", EnumChatFormatting.GREEN);
        System.out.println("[ChainMiner] " + player.getEntityName() + " chained " + totalMined + " blocks");
    }
    
    /**
     * 记录需要破坏的方块位置和类型信息
     */
    private static class BlockBreakPosition {
        int x, y, z;
        Block block;
        int metadata;
        
        BlockBreakPosition(int x, int y, int z, Block block, int metadata) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.block = block;
            this.metadata = metadata;
        }
    }

    @Override
    public int getPacketSize() {
        return 4 + (blocks.size() * 13); // 4 bytes for count + 13 bytes per block
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
