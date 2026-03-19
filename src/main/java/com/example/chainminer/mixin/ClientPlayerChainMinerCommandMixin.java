package com.example.chainminer.mixin;

import com.example.chainminer.ChainMinerConfig;
import net.minecraft.EntityClientPlayerMP;
import net.minecraft.EnumChatFormatting;
import net.minecraft.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(EntityClientPlayerMP.class)
public abstract class ClientPlayerChainMinerCommandMixin {
    @Shadow public abstract void receiveChatMessage(String message, EnumChatFormatting color);

    @Inject(method = "sendChatMessage(Ljava/lang/String;Z)V", at = @At("HEAD"), cancellable = true)
    private void chainMiner$handleCommandWithPermission(String message, boolean permissionOverride, CallbackInfo ci) {
        if (chainMiner$handleCommand(message)) {
            ci.cancel();
        }
    }

    @Inject(method = "sendChatMessage(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void chainMiner$handleCommand(String message, CallbackInfo ci) {
        if (chainMiner$handleCommand(message)) {
            ci.cancel();
        }
    }

    private boolean chainMiner$handleCommand(String message) {
        if (message == null) {
            return false;
        }

        String trimmed = message.trim();
        String[] parts = trimmed.split("\\s+");
        if (parts.length == 0) {
            return false;
        }

        String root = parts[0].toLowerCase();
        if (!root.equals("/chainminer") && !root.equals("/cm")) {
            return false;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null) {
            return true;
        }

        ChainMinerConfig.load(minecraft.mcDataDir);

        if (parts.length == 1 || parts[1].equalsIgnoreCase("help")) {
            this.receiveChatMessage("[ChainMiner] 用法:", EnumChatFormatting.YELLOW);
            this.receiveChatMessage("  /chainminer key <按键名|mouse3/mouse4/mouse5>", EnumChatFormatting.YELLOW);
            this.receiveChatMessage("  /chainminer enable <true/false>", EnumChatFormatting.YELLOW);
            this.receiveChatMessage("  /chainminer limit <1-512>", EnumChatFormatting.YELLOW);
            this.receiveChatMessage("  /chainminer show", EnumChatFormatting.YELLOW);
            return true;
        }

        String sub = parts[1].toLowerCase();

        if (sub.equals("show")) {
            this.receiveChatMessage("[ChainMiner] enabled=" + ChainMinerConfig.isEnabled()
                    + ", holdBinding=" + chainMiner$describeBinding()
                    + ", chainLimit=" + ChainMinerConfig.getChainLimit(), EnumChatFormatting.GREEN);
            return true;
        }

        if (sub.equals("key")) {
            if (parts.length < 3) {
                this.receiveChatMessage("[ChainMiner] 用法: /chainminer key <按键名|mouse3/mouse4/mouse5>", EnumChatFormatting.RED);
                return true;
            }

            Integer mouseButton = chainMiner$parseMouseButton(parts[2]);
            if (mouseButton != null) {
                ChainMinerConfig.setHoldMouseButton(mouseButton);
                ChainMinerConfig.save();
                this.receiveChatMessage("[ChainMiner] 触发按键已设置为: " + chainMiner$describeBinding(), EnumChatFormatting.GREEN);
                return true;
            }

            String keyName = parts[2].toUpperCase(Locale.ROOT);
            int keyCode = Keyboard.getKeyIndex(keyName);
            if (keyCode <= 0) {
                this.receiveChatMessage("[ChainMiner] 无效按键: " + parts[2], EnumChatFormatting.RED);
                return true;
            }

            ChainMinerConfig.setHoldKeyName(keyName);
            ChainMinerConfig.save();
            this.receiveChatMessage("[ChainMiner] 触发按键已设置为: " + chainMiner$describeBinding(), EnumChatFormatting.GREEN);
            return true;
        }

        if (sub.equals("enable")) {
            if (parts.length < 3) {
                this.receiveChatMessage("[ChainMiner] 用法: /chainminer enable <true/false>", EnumChatFormatting.RED);
                return true;
            }

            boolean enabled = "true".equalsIgnoreCase(parts[2]) || "on".equalsIgnoreCase(parts[2]);
            boolean disabled = "false".equalsIgnoreCase(parts[2]) || "off".equalsIgnoreCase(parts[2]);

            if (!enabled && !disabled) {
                this.receiveChatMessage("[ChainMiner] 参数无效，请使用 true/false", EnumChatFormatting.RED);
                return true;
            }

            ChainMinerConfig.setEnabled(enabled);
            ChainMinerConfig.save();
            this.receiveChatMessage("[ChainMiner] 连锁挖矿已" + (enabled ? "启用" : "禁用"), EnumChatFormatting.GREEN);
            return true;
        }

        if (sub.equals("limit")) {
            if (parts.length < 3) {
                this.receiveChatMessage("[ChainMiner] 用法: /chainminer limit <1-512>", EnumChatFormatting.RED);
                return true;
            }

            try {
                int limit = Integer.parseInt(parts[2]);
                ChainMinerConfig.setChainLimit(limit);
                ChainMinerConfig.save();
                this.receiveChatMessage("[ChainMiner] 连锁上限已设置为: " + ChainMinerConfig.getChainLimit(), EnumChatFormatting.GREEN);
            } catch (NumberFormatException e) {
                this.receiveChatMessage("[ChainMiner] limit 必须是数字", EnumChatFormatting.RED);
            }

            return true;
        }

        this.receiveChatMessage("[ChainMiner] 未知子命令: " + parts[1] + "，输入 /chainminer help 查看用法", EnumChatFormatting.RED);
        return true;
    }

    private Integer chainMiner$parseMouseButton(String raw) {
        if (raw == null) {
            return null;
        }

        String value = raw.trim().toUpperCase(Locale.ROOT);
        if (value.startsWith("MOUSE")) {
            String number = value.substring("MOUSE".length()).trim();
            if (number.isEmpty()) {
                return null;
            }

            try {
                int button = Integer.parseInt(number);
                if (button >= 0) {
                    return button;
                }
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    private String chainMiner$describeBinding() {
        if (ChainMinerConfig.isMouseBinding()) {
            int button = ChainMinerConfig.getMouseButton();
            String buttonName = Mouse.isCreated() && button >= 0 ? Mouse.getButtonName(button) : null;
            if (buttonName == null || buttonName.isEmpty()) {
                buttonName = "BUTTON" + button;
            }
            return "MOUSE:" + buttonName + "(" + button + ")";
        }

        return "KEY:" + ChainMinerConfig.getHoldKeyName();
    }
}
