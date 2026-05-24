## 加速渲染 1.0.7-1.20.1 ALPHA
- 添加了``过滤器配置 > 启用容器GUI过滤器``选项以阻止部分在渲染加速后会导致需渲染异常的容器GUI被加速, 提供更好的兼容性.
- 修复了玩家在物品栏GUI中以错误光照渲染导致的渲染异常.
- 修复了超出标准范围UV的面在静态剔除中被错误剔除导致的渲染异常.
- 修复了文字渲染错位导致的渲染异常.
- 修复了Oculus中错误硬编码导致的渲染异常.

## 注意事项
- 1.20.1移植的游戏内配置界面需要安装Configured以启用.
- 1.20.1移植的GeckoLib加速需要安装MixinBooster以启用.
- 1.20.1移植的车万女仆加速需要安装MixinBooster以启用.

## Accelerated Rendering 1.0.7-1.20.1 ALPHA
- Adds ``Filter Settings > Enable Menu Filter`` to filter menus that will cause glitches when accelerated.
- Fixes visual glitches on players rendered with wrong light direction in inventory GUI.
- Fixes visual glitches on static culler exceptionally culled geometries with UV outside regular range.
- Fixes visual glitches on texts rendered with wrong offsets.
- Fixes visual glitches on wronly hardcoded offset in Oculus.

## Important Notes
- 1.20.1 port requires Configured to modify configurations in game.
- 1.20.1 port of GeckoLib acceleration requires MixinBooster to enable.
- 1.20.1 port of TouhouLittleMaid acceleration requires MixinBooster to enable.