# Chain Miner (MITE 1.6.4 / FishModLoader)

## 功能
- 按住指定按键再挖方块，会继续挖掘与其 **六向相邻** 的同类方块。
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

- 聊天命令：
   - `/chainminer hud <x> <y>` 设置激活提示坐标
   - `/chainminer hud show` 查看当前坐标

## 行为说明
- 注入点：`net.minecraft.PlayerControllerMP#onPlayerDestroyBlock(IIII)Z`（客户端）
- 使用 `ThreadLocal` 防止连锁过程中递归重复触发。