## 加速渲染 1.0.6-1.20.1 ALPHA
- 添加了``核心配置 > 启用缓存动态RenderType``选项以支持对动态渲染类型的加速.
- 添加了``核心配置 > 模型合并类型``选项以支持对完全一致模型的合并以改善显存占用.
- 改善了过滤器的灵活性, 现在可以在过滤器列表中使用正则表达式.
- 修复了GUI物品合批时没有正确还原帧缓冲状态导致的渲染异常.
- 修复了自定义渲染阶段被错误加速导致的渲染异常.
- 修复了物品附魔光效无法正确渲染导致的渲染异常.
- 修复了可能的部分特殊物品渲染导致的内存泄漏问题.

## 注意事项
- 1.20.1移植的游戏内配置界面需要安装Configured以启用.
- 1.20.1移植的GeckoLib加速需要安装MixinBooster以启用.
- 1.20.1移植的车万女仆加速需要安装MixinBooster以启用.

## Accelerated Rendering 1.0.6-1.20.1 ALPHA
- Adds ``Core Settings > Enable Cache Dynamic Render Type`` to support the acceleration of dynamic render types.
- Adds ``Core Settings > Mesh Merge Type`` to support reducing VRAM usage by merging duplicated meshes together.
- Improves filter flexibility. Regular Expression can now be used in filter values.
- Fixes visual glitches on container GUI rendering due to not correctly restoring framebuffer state.
- Fixes visual glitches on geometries rendered in custom rendering stages due to incorrect acceleration.
- Fixes visual glitches on item glints.
- Fixes memory leaks due to acceleration on certain special items.

## Important Notes
- 1.20.1 port requires Configured to modify configurations in game.
- 1.20.1 port of GeckoLib acceleration requires MixinBooster to enable.
- 1.20.1 port of TouhouLittleMaid acceleration requires MixinBooster to enable.