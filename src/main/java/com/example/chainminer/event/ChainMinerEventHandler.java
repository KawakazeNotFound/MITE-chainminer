package com.ryosume.chainminer.event;

import com.ryosume.chainminer.network.ChainMinerPacket;
import net.xiaoyu233.fml.reload.event.PacketRegisterEvent;

/**
 * 处理网络事件：注册自定义数据包
 */
public class ChainMinerEventHandler {
    private static boolean registered = false;

    public static void onPacketRegister(PacketRegisterEvent event) {
        if (registered) {
            return;
        }
        // 注册数据包：在客户端和服务器都处理
        event.register(true, true, ChainMinerPacket.class);
        registered = true;
        System.out.println("[ChainMiner] ChainMinerPacket registered");
    }
}
