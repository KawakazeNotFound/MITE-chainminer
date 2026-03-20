package com.example.chainminer;

public enum ChainMinerMode {
    SHAPELESS(0, "不定形", "无约束的26方向连锁"),
    TUNNEL(1, "小隧道", "沿视线方向直线开采"),
    STAIRCASE(2, "逃生通道", "梯形斜向开采");

    private final int id;
    private final String displayName;
    private final String description;

    ChainMinerMode(int id, String displayName, String description) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static ChainMinerMode fromId(int id) {
        for (ChainMinerMode mode : values()) {
            if (mode.id == id) {
                return mode;
            }
        }
        return SHAPELESS;
    }

    public ChainMinerMode next() {
        ChainMinerMode[] modes = values();
        return modes[(ordinal() + 1) % modes.length];
    }

    public ChainMinerMode previous() {
        ChainMinerMode[] modes = values();
        return modes[(ordinal() - 1 + modes.length) % modes.length];
    }
}
