## 加速渲染 1.0.8-1.20.1 ALPHA
- 添加了``核心配置 > 裁切区域还原类型``选项以提供对修改了容器GUI渲染的MOD更好的兼容性.
- 添加了``加速物品渲染配置 > 启用合并GUI物品批次``选项以以提供对修改了容器GUI渲染的MOD更好的兼容性.
- 添加了对FTB Library的GUI物品合批加速支持.
- 修复了在``earlyWindowControl``不可用的情况下的导致的崩溃.
- 修复了因为Minecraft因为其他错误没有正确启动导致的无关加速渲染的崩溃.
- 修复了带状态的EMF模型在运行中修改状态导致的渲染异常.
- 修复了OpenGL内存屏障被错误使用导致的潜在渲染异常.
- 修复了Oculus中错误硬编码导致的渲染异常.
- 阻止MOD添加的自定义HUD进行加速, 防止过度调用着色器导致性能下降.

## 注意事项
- 1.20.1移植的游戏内配置界面需要安装Configured以启用.
- 1.20.1移植的GeckoLib加速需要安装MixinBooster以启用.
- 1.20.1移植的车万女仆加速需要安装MixinBooster以启用.

## Accelerated Rendering 1.0.8-1.20.1 ALPHA
- Adds ``Core Settings > Scissor Restoring Type`` to provide better compatibility with modified container GUI.
- Adds ``Accelerated Item Rendering Settings > Enable Merge GUI Item Batches`` to provide better compatibility with modified container GUI.
- Adds GUI item batching acceleration support to FTB Library.
- Fixes crashes when ``earlyWindowControl`` is not available.
- Fixes irrelevant crashes when Minecraft is crashed by other MODs when starting up.
- Fixes visual glitches when EMF models with states modifying its state at runtime.
- Fixes potential visual glitches due to incorrect OpenGL memory barrier usages.
- Fixes visual glitches on wronly hardcoded offset in Oculus.
- Prevent custom HUDs by MODs from being accelerated to reduce shader overhead.

## Important Notes
- 1.20.1 port requires Configured to modify configurations in game.
- 1.20.1 port of GeckoLib acceleration requires MixinBooster to enable.
- 1.20.1 port of TouhouLittleMaid acceleration requires MixinBooster to enable.