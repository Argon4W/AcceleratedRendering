## 加速渲染 1.0.9-1.21.1 ALPHA
- 添加了 `杂项MOD兼容性配置` 以单独配置各 MOD 的兼容性功能开关.
- 添加了 `核心配置 > 绘制方法类型` 以选择不同的绘制方案获取更好性能.
- 添加了新的性能更高的模型上传方式.
- 修复了盔甲纹饰和渲染层的渲染顺序问题.
- 修复了 EMF 模型加速的内存泄漏问题.
- 修复了 Log 中 OpenGL 持续报错的问题.
- 重写了 GUI 合批逻辑.
- 重写了 BufferSource 获取逻辑, 改善了部分 MOD 的兼容性.

## Accelerated Rendering 1.0.9-1.21.1 ALPHA
- Adds `Miscellaneous Mods Compatibility Settings` to configure compatibility settings of mods separately.
- Adds `Core Settings > Draw Method Type` to select between two draw methods to have better performance.
- Adds a new performant mesh uploading method.
- Fixes rendering order issue of armor trims and render layers.
- Fixes memory leak issue in EMF ModelPart Acceleration.
- Fixes OpenGL error log spam issue.
- Rewrites GUI batching logic.
- Rewrites BufferSource gathering logic, which improves compatibility with some mods.