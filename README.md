# Chain Miner (MITE 1.6.4 / FishModLoader)

## 功能
- 按住指定按键再挖方块，会继续挖掘与其相关的同类方块。
- 支持 **三种连锁模式**：
  1. **不定形**（默认）：26方向无约束连锁
  2. **小隧道**：沿玩家视线方向直线开采
  3. **逃生通道**：梯形斜向开采（根据玩家视角上下）
- 默认按键为飘号键（`GRAVE`，即 `~` / `` ` ``）。
- 默认最多连锁 `64` 个方块（可在配置中修改）。

## 关键文件
- `src/main/resources/fml.mod.json`
- `src/main/resources/chainminer.mixins.json`
- `src/main/java/com/example/chainminer/ChainMinerMod.java`
- `src/main/java/com/example/chainminer/mixin/ItemInWorldManagerChainMinerMixin.java`

## 打包说明
1. 将 `chainminer/src/main/java` 与 `chainminer/src/main/resources` 编译并打包为一个 jar。
2. 确保 jar 根目录包含：
   - `fml.mod.json`
   - `chainminer.mixins.json`
   - `com/example/chainminer/**.class`
3. 把 jar 放入 FishModLoader 的 `mods` 目录后启动游戏。

## 配置
- 配置文件：`<.minecraft>/config/chainminer.properties`
- 首次进入游戏会自动生成，支持：
   - `enabled=true|false`
   - `holdBinding=KEY:GRAVE`（键盘示例：`KEY:LSHIFT`、`KEY:LCONTROL`、`KEY:R`）
   - `holdBinding=MOUSE:4`（鼠标按键示例：`MOUSE:3`、`MOUSE:4`、`MOUSE:5`）
   - `holdKey=GRAVE`（旧字段，保留兼容）
   - `chainLimit=64`（范围 `1` 到 `512`）
   - `hudX=2`、`hudY=22`（左上角激活提示位置）
   - `mode=0`（当前连锁模式：0=不定形，1=小隧道，2=逃生通道）

- 聊天命令：
   - 激活按键请在 `选项 -> 按键设定` 中修改“连锁挖矿激活键”
   - `/chainminer enable <true/false>` 启用/禁用连锁
   - `/chainminer limit <1-512>` 设置连锁数量限制
   - `/chainminer hud <x> <y>` 设置激活提示坐标
   - `/chainminer hud show` 查看当前坐标
   - `/chainminer show` 查看所有配置

- 模式切换：
   - **按住 Shift + 鼠标滚轮** 切换连锁模式
   - 当前模式会显示在左上角 HUD（黄色）
   - 切换时会在聊天中显示新模式名称和描述

## 行为说明
- 注入点：`net.minecraft.PlayerControllerMP#onPlayerDestroyBlock(IIII)Z`（客户端）
- 使用 `ThreadLocal` 防止连锁过程中递归重复触发。