package com.example.chainminer.client;

import com.example.chainminer.ChainMinerConfig;
import net.minecraft.GuiButton;
import net.minecraft.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.util.Locale;

public class ChainMinerConfigScreen extends GuiScreen {
    private static final int BTN_TOGGLE_ENABLED = 1;
    private static final int BTN_SET_KEY = 2;
    private static final int BTN_SAVE = 3;
    private static final int BTN_CANCEL = 4;

    private boolean enabled;
    private String holdKeyName;
    private boolean waitingForKey;

    @Override
    public void initGui() {
        this.enabled = ChainMinerConfig.isEnabled();
        this.holdKeyName = ChainMinerConfig.getHoldKeyName();
        this.waitingForKey = false;

        this.buttonList.clear();
        int centerX = this.width / 2;
        int baseY = this.height / 4;

        this.buttonList.add(new GuiButton(BTN_TOGGLE_ENABLED, centerX - 100, baseY + 20, 200, 20, getEnabledText()));
        this.buttonList.add(new GuiButton(BTN_SET_KEY, centerX - 100, baseY + 46, 200, 20, getKeyText()));
        this.buttonList.add(new GuiButton(BTN_SAVE, centerX - 100, baseY + 90, 98, 20, "保存"));
        this.buttonList.add(new GuiButton(BTN_CANCEL, centerX + 2, baseY + 90, 98, 20, "取消"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) {
            return;
        }

        if (button.id == BTN_TOGGLE_ENABLED) {
            this.enabled = !this.enabled;
            button.displayString = getEnabledText();
            return;
        }

        if (button.id == BTN_SET_KEY) {
            this.waitingForKey = true;
            button.displayString = "请按下一个按键...";
            return;
        }

        if (button.id == BTN_SAVE) {
            ChainMinerConfig.setEnabled(this.enabled);
            ChainMinerConfig.setHoldKeyName(this.holdKeyName);
            ChainMinerConfig.save();
            this.mc.displayGuiScreen((GuiScreen) null);
            return;
        }

        if (button.id == BTN_CANCEL) {
            this.mc.displayGuiScreen((GuiScreen) null);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (this.waitingForKey) {
            if (keyCode != Keyboard.KEY_ESCAPE) {
                String keyName = Keyboard.getKeyName(keyCode);
                if (keyName != null && !keyName.isEmpty()) {
                    this.holdKeyName = keyName.toUpperCase(Locale.ROOT);
                }
            }

            this.waitingForKey = false;
            GuiButton keyButton = (GuiButton) this.buttonList.get(1);
            keyButton.displayString = getKeyText();
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, "ChainMiner 配置", this.width / 2, this.height / 4 - 10, 0xFFFFFF);
        this.drawCenteredString(this.fontRenderer, "命令: /chainminer config", this.width / 2, this.height / 4 + 120, 0xA0A0A0);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String getEnabledText() {
        return "启用连锁: " + (this.enabled ? "是" : "否");
    }

    private String getKeyText() {
        return "触发按键: " + this.holdKeyName;
    }
}
