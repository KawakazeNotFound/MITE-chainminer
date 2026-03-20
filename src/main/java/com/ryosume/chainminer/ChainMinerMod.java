package com.ryosume.chainminer;

import com.google.common.eventbus.Subscribe;
import com.ryosume.chainminer.event.ChainMinerEventHandler;
import net.fabricmc.api.ModInitializer;
import net.xiaoyu233.fml.reload.event.MITEEvents;
import net.xiaoyu233.fml.reload.event.PacketRegisterEvent;

public class ChainMinerMod implements ModInitializer {
    @Override
    public void onInitialize() {
        System.out.println("[ChainMiner] initialized");

        // 注册事件处理器
        MITEEvents.MITE_EVENT_BUS.register(this);
    }

    // 处理数据包注册事件
    @Subscribe
    public void onPacketRegister(PacketRegisterEvent event) {
        ChainMinerEventHandler.onPacketRegister(event);
    }
}