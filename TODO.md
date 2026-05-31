# Accelerated Rendering Fabric API 迁移

我的目标：将 `AcceleratedRendering` 迁移为 Fabric Loader + Fabric API 客户端 Mod。优先让核心渲染管线在 Fabric 1.21.1 上稳定编译和启动，之后会逐步恢复 Iris/Sodium 与其它可迁移兼容层。当前仓库基线为 Minecraft `1.21.1`、Java `21`、NeoForge `21.1.219`。

## 原则

- [x] 优先单加载器 Fabric 迁移
- [x] 保持 `mod_id=acceleratedrendering`、包名 `com.github.argon4w.acceleratedrendering`、资源命名空间不变。
- [x] 保持 Java 21 与 Minecraft 1.21.1，避免同时升级 Minecraft 主版本。
- [x] 优先确定 Vanilla 渲染路径、Sodium/Iris 路径可运行；Curios/FTB/Sophisticated/Touhou Little Maid/GeckoLib/EMF 等兼容层分批恢复。
- [x] 所有 NeoForge-only API 先做边界封装，再替换调用点。

## 阶段 0：迁移前基线

- [x] 新建迁移分支，例如 `fabric-migration-principles` 或后续实际实现分支。当前分支：`fabric-migration-principles`。
- [x] 在当前 NeoForge 版本上跑一次基线：
- [ ] 无其它 Mod 的主菜单、单人世界进入、实体渲染、物品渲染、文本渲染。
- [ ] Sodium + Iris + 一个 shader pack 下的实体、手持物品、阴影渲染。
- [ ] GUI 批处理：背包、容器、热键栏、文字描边。
- [ ] F3+T 资源重载后 compute shader 重新编译是否正常。
- [x] 保存一份迁移检查清单的基线截图/日志，后续对照回归：日志摘要已记录在本节。

## 阶段 1：Gradle 与依赖迁移

- [ ] 替换 `build.gradle` 插件：
  - [ ] 移除 `net.neoforged.gradle.userdev`。
  - [ ] 引入 `fabric-loom`。
  - [ ] 保留 `java-library`、`maven-publish`、`idea`，按 Fabric Loom 默认行为精简 `eclipse` 配置。
- [ ] 更新 `settings.gradle` 仓库：
  - [ ] 增加 `https://maven.fabricmc.net/`。
  - [ ] 保留 `gradlePluginPortal()`、Maven Central、Modrinth、CurseMaven、JitPack。
  - [ ] 移除 NeoForge 专用 Maven，除非仍有历史依赖需要解析。
- [ ] 重写 Minecraft/Fabric 依赖：
  - [ ] `minecraft "com.mojang:minecraft:${minecraft_version}"`
  - [ ] `mappings loom.officialMojangMappings()`，或在确认 Parchment 可用于 Loom 后使用 layered mappings。
  - [ ] `modImplementation "net.fabricmc:fabric-loader:${fabric_loader_version}"`
  - [ ] `modImplementation "net.fabricmc.fabric-api:fabric-api:${fabric_api_version}"`
- [ ] 清理 NeoForge run 配置：
  - [ ] 删除 `runs { client/server/gameTestServer/data }` 的 NeoForge DSL。
  - [ ] 使用 Loom 的 `runClient`，只保留客户端启动参数和日志参数。
  - [ ] 客户端-only Mod 不强求 `runServer` 通过，但要确保 Fabric 元数据不会让服务端误加载客户端类。
- [ ] 更新 `gradle.properties`：
  - [ ] 删除 `neo_version`、`neo_version_range`、`loader_version_range`。
  - [ ] 增加 `fabric_loader_version`、`fabric_api_version`。
  - [ ] 保留 `minecraft_version=1.21.1`、`mod_id`、`mod_version`、`mod_group_id` 等发布字段。
  - [ ] 版本号建议追加加载器标识，例如 `1.0.9-1.21.1-alpha+fabric` 或发布时按平台拆版本。
- [ ] 依赖替换矩阵：

| 当前依赖 | Fabric 迁移处理 | 优先级 |
| --- | --- | --- |
| `net.neoforged:neoforge` | 替换为 Fabric Loader + Fabric API | P0 |
| Sodium NeoForge | 替换为 Sodium Fabric 同 MC 版本构件 | P1 |
| Iris NeoForge | 替换为 Iris Fabric 同 MC 版本构件 | P1 |
| `guideme` / `ae2` | 替换为 Fabric 构件；仅 dev runtime 需要则用 `modRuntimeOnly` | P2 |
| Entity Texture/Model Features | 使用 Fabric 构件，Mixin 目标逐项校验 | P2 |
| GeckoLib | 使用 Fabric 构件，确认包名和渲染类签名 | P2 |
| ImmediatelyFast | 使用 Fabric 构件，确认 `BatchingBuffers` 类名/方法 | P2 |
| Touhou Little Maid | 先确认是否有 Fabric 1.21.1 构件；没有就禁用该兼容层 | P3 |
| Curios | Fabric 通常不是 Curios；评估是否迁到 Trinkets 或直接移除该兼容层 | P3 |
| FTB Library Forge | 换 Fabric 构件；如果类名差异大，拆成独立兼容任务 | P3 |
| Sophisticated Core | 若无 Fabric 版本，删除依赖和 mixin 声明 | P3 |
| Lombok | 保留 `compileOnly` + `annotationProcessor` | P0 |
| MixinExtras | 确认 Fabric Loader 是否已提供；否则添加 `mixinextras-fabric` 并 include | P0 |

## 阶段 2：Fabric 元数据与资源

- [ ] 新增 `src/main/resources/fabric.mod.json`：
  - [ ] `schemaVersion: 1`
  - [ ] `id: acceleratedrendering`
  - [ ] `version/name/description/authors/license/icon` 从 `gradle.properties` 和现有 TOML 迁移。
  - [ ] `environment: "client"`。
  - [ ] `entrypoints.client` 指向 Fabric 客户端入口类。
  - [ ] `mixins` 列出当前所有 `acceleratedrendering.*.mixins.json`。
  - [ ] `depends` 包含 `fabricloader`、`fabric-api`、`minecraft`、`java`。
  - [ ] `recommends` 或 `suggests` 标注 `sodium`、`iris`、`modmenu`、`cloth-config` 等可选体验依赖。
  - [ ] `accessWidener` 或 class tweaker 文件名按阶段 6 决定。
- [ ] 删除或停止打包 `src/main/resources/META-INF/neoforge.mods.toml`。
- [ ] 删除 `ProcessResources` 中只服务 `neoforge.mods.toml` 的 expand 逻辑，改为 Fabric 常见的 `filesMatching("fabric.mod.json") { expand(...) }`。
- [ ] 保留 `logo.png`、`assets/acceleratedrendering/lang/*.json`、`assets/acceleratedrendering/shaders/**/*.compute` 的资源路径。
- [ ] 检查所有 mixin JSON：
  - [ ] Fabric 可继续读取 `"client"` mixin 列表。
  - [ ] 对只服务已移除兼容层的 mixin JSON，不要继续写入 `fabric.mod.json`。
  - [ ] 对可选 Mod 的 mixin JSON 保持 `@Pseudo` 或 plugin gating，避免缺少目标类时硬崩。

## 阶段 3：入口类与生命周期

- [ ] 重写 `AcceleratedRenderingModEntry`：
  - [ ] 移除 `@Mod`、`Dist.CLIENT`、`IEventBus`、`ModContainer`、`ModConfig`、`IConfigScreenFactory`。
  - [ ] 实现 `net.fabricmc.api.ClientModInitializer`。
  - [ ] 在 `onInitializeClient()` 中注册配置、内部事件、资源重载、可选兼容注册。
- [ ] 保留：
  - [ ] `public static final String MOD_ID = "acceleratedrendering";`
  - [ ] `LOGGER`，建议改为 `LoggerFactory.getLogger(MOD_ID)` 或继续使用 Mojang logger。
- [ ] 新增初始化顺序约束：
  - [ ] 先加载配置。
  - [ ] 再注册核心 shader/program/culling/pipeline 扩展点。
  - [ ] 再按 `FabricLoader.getInstance().isModLoaded(id)` 注册可选兼容。
  - [ ] 最后注册 Fabric resource reload listener。

## 阶段 4：配置系统迁移

当前 `FeatureConfig` 深度依赖 `net.neoforged.neoforge.common.ModConfigSpec`，全仓库约 70+ 处读取 `FeatureConfig.CONFIG.*.get()` / `getAsInt()`。迁移时应保留读取 API 的形状，减少业务代码改动。

- [ ] 新增轻量配置抽象：
  - [ ] `ConfigValue<T>`：提供 `get()`、`set(T)`。
  - [ ] `IntConfigValue`：提供 `getAsInt()`。
  - [ ] `RestartRequired` 标记仅用于 UI 展示，不影响运行时。
- [ ] 重写 `FeatureConfig`：
  - [ ] 删除 `ModConfigSpec` 和 `Pair`。
  - [ ] 将所有默认值、范围、枚举、列表配置迁入本地 schema。
  - [ ] 保持字段名不变，让 `FeatureConfig.CONFIG.coreSparseThreshold.getAsInt()` 等调用尽量不用改。
  - [ ] 用 `FeatureConfig.isLoaded()` 或 `ConfigManager.isLoaded()` 替代 `FeatureConfig.SPEC.isLoaded()`。
- [ ] 配置文件格式：
  - [ ] 推荐第一阶段使用 JSON 或 TOML 的本地实现，路径为 `<minecraft>/config/acceleratedrendering-client.json` 或 `.toml`。
  - [ ] 若继续使用 TOML，选择一个小型 TOML 库并 `include`；若避免新增库，使用 Gson JSON。
  - [ ] 启动时缺失配置则生成默认文件。
  - [ ] 启动时配置字段缺失则补默认值，未知字段保留或记录 warning。
  - [ ] 枚举值非法时回退默认值并写日志。
- [ ] 可选游戏内配置 UI：
  - [ ] Fabric API 不提供 Mod 列表配置入口；若要保留“Mods > Config”体验，添加可选 `modmenu` + `cloth-config` 集成。
  - [ ] 不把 `modmenu` / `cloth-config` 设为硬依赖；没有它们时仍可通过配置文件工作。

ASCII UI 草图：

```text
+------------------------------------------------------------+
| Accelerated Rendering                                      |
| [Core] [Entity] [Item] [Text] [Filter] [Compat]            |
+------------------------------------------------------------+
| Core Settings                                              |
|   Debug Context                 [ Enabled  v ]             |
|   Draw Method                   [ INDIRECT v ]             |
|   Sparse Threshold              [----64-----]              |
|   Pooled Ring Buffer Size       [ 8        ]  restart      |
|                                                            |
|                         [Reset] [Cancel] [Save & Close]    |
+------------------------------------------------------------+
```

## 阶段 5：NeoForge 事件总线替换

当前自定义扩展点通过 NeoForge `Event` + `IModBusEvent` + `ModLoader.postEventWithReturn` 实现：

- `LoadComputeShaderEvent`
- `LoadCullingProgramSelectorEvent`
- `LoadPolygonProcessorEvent`
- `LoadShaderProgramOverridesEvent`

Fabric 没有 NeoForge ModBus，需要换成内部 registry/callback。

- [ ] 新增内部注册中心：
  - [ ] `ComputeShaderRegistry`：收集 `ResourceLocation -> ComputeShaderDefinition`。
  - [ ] `CullingProgramRegistry`：按 `VertexFormat` 组合 `ICullingProgramSelector`。
  - [ ] `PolygonProcessorRegistry`：按 `VertexFormat` 组合 `IPolygonProcessor`。
  - [ ] `ShaderProgramOverrideRegistry`：按 `VertexFormat` + `RenderType` 收集 transform/uploading override。
- [ ] 将现有事件类改为普通 registrar/context，或删除事件继承：
  - [ ] 移除 `extends Event implements IModBusEvent`。
  - [ ] 保留 `loadComputeShader()`、`loadFor()`、`getOverrides()` 等业务语义。
- [ ] 将 `@EventBusSubscriber` + `@SubscribeEvent` 替换为显式注册方法：
  - [ ] `ComputeShaderPrograms.registerComputeShaders(registry)`
  - [ ] `ComputeShaderPrograms.registerReloadListeners()`
  - [ ] `OrientationCullingPrograms.register(registry)`
  - [ ] `IrisPrograms.register(registry)`，仅在 `iris` 已加载时调用。
- [ ] 替换调用点：
  - [ ] `ComputeShaderProgramLoader.prepare()` 不再调用 `ModLoader.postEventWithReturn(new LoadComputeShaderEvent())`，改为读取 `ComputeShaderRegistry.snapshot()`。
  - [ ] `VanillaBufferEnvironment` 不再 post `LoadShaderProgramOverridesEvent` / `LoadPolygonProcessorEvent`，改为 registry lookup。
  - [ ] `IrisBufferEnvironment` 同上。
  - [ ] `IndirectDrawMethod` 不再 post `LoadCullingProgramSelectorEvent`，改为 registry lookup。
- [ ] 事件优先级处理：
  - [ ] `OrientationCullingPrograms` 里原 `EventPriority.HIGH` 需要变成注册顺序或 registry priority。
  - [ ] 明确核心注册先于兼容注册；兼容注册可以装饰已有 selector/processor。

## 阶段 6：Fabric resource reload listener

- [ ] `ComputeShaderProgramLoader`：
  - [ ] 保持 `SimplePreparableReloadListener` 业务逻辑。
  - [ ] 如 Fabric 需要可识别 listener，额外实现 `IdentifiableResourceReloadListener` 或用 Fabric API 对应包装。
  - [ ] `getFabricId()` 返回 `acceleratedrendering:compute_shader_program_loader`。
  - [ ] 用 `ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(...)` 注册。
- [ ] `TextureUtils`：
  - [ ] 移除 `RegisterClientReloadListenersEvent`、`@EventBusSubscriber`、`@SubscribeEvent`。
  - [ ] 改成入口类显式注册 Fabric resource reload listener。
- [ ] 验证：
  - [ ] 启动时 compute shader 全部被发现和编译。
  - [ ] F3+T 后旧 program 正确 `delete()`，新 program 正确加载。
  - [ ] shader 编译错误能输出资源路径和 info log。

## 阶段 7：Access Transformer 迁移

当前 `src/main/resources/META-INF/accesstransformer.cfg` 全部目标都是 Vanilla Minecraft 客户端类，可迁移到 Fabric Access Widener 或 Loader 0.18+ 的 Class Tweaker。

- [ ] 决定目标：
  - [ ] 若 Fabric Loader 目标版本 >= `0.18.0` 且 Loom >= `1.12`，优先使用 `acceleratedrendering.classtweaker`。
  - [ ] 若需要兼容旧 Fabric Loader，使用 `acceleratedrendering.accesswidener`。
- [ ] 在 `build.gradle` 中配置：
  - [ ] `loom { accessWidenerPath = file("src/main/resources/acceleratedrendering.accesswidener") }`，或 class tweaker 文件路径。
- [ ] 在 `fabric.mod.json` 中声明：
  - [ ] `"accessWidener": "acceleratedrendering.accesswidener"`，或 class tweaker 文件。
- [ ] 逐条转换 `accesstransformer.cfg`：
  - [ ] 类访问：`public net.minecraft.client.renderer.OutlineBufferSource$EntityOutlineGenerator`
  - [ ] 字段访问：`RenderType$CompositeState texturingState` 等需要补 JVM descriptor。
  - [ ] 方法访问：`Font getFontSet(...)`、`GuiGraphics innerBlit(...)` 等需要补方法 descriptor。
- [ ] 优先用 IntelliJ Minecraft Development 插件或 mcsrc.dev 复制条目，减少 descriptor 手写错误。
- [ ] 跑校验：
  - [ ] `./gradlew validateAccessWidener`
  - [ ] `./gradlew compileJava`
- [ ] 若某些目标在 Fabric/Mojang mappings 下名字不同，先修 access widener，再修源码引用。

## 阶段 8：NeoForge API 调用点替换

- [ ] `net.neoforged.fml.loading.LoadingModList`
  - [ ] 文件：`compat/AbstractCompatMixinPlugin.java`
  - [ ] 替换为 `FabricLoader.getInstance().isModLoaded(id)`。
- [ ] `net.neoforged.api.distmarker.Dist`
  - [ ] 文件：`AcceleratedRenderingModEntry.java`、`ComputeShaderPrograms.java`、`TextureUtils.java`、`OrientationCullingPrograms.java`
  - [ ] Fabric 使用 `fabric.mod.json` 的 `"environment": "client"` 和 client entrypoint，不需要 `Dist` 注解。
- [ ] `ModConfigSpec` / `ModConfig`
  - [ ] 文件：`configs/FeatureConfig.java`、`AcceleratedRenderingModEntry.java`、`core/CoreFeature.java`
  - [ ] 按阶段 4 替换。
- [ ] `ConfigurationScreen` / `IConfigScreenFactory`
  - [ ] 文件：`AcceleratedRenderingModEntry.java`
  - [ ] 替换为可选 ModMenu integration；无 ModMenu 时不注册。
- [ ] `ModLoader.postEventWithReturn`
  - [ ] 文件：`ComputeShaderProgramLoader.java`、`VanillaBufferEnvironment.java`、`IrisBufferEnvironment.java`、`IndirectDrawMethod.java`
  - [ ] 按阶段 5 替换为内部 registry。
- [ ] `RegisterClientReloadListenersEvent`
  - [ ] 文件：`ComputeShaderPrograms.java`、`TextureUtils.java`
  - [ ] 按阶段 6 替换为 Fabric resource reload listener。
- [ ] `RenderLevelStageEvent.Stage`
  - [ ] 文件：`features/filter/FilterFeature.java`、`features/filter/mixins/ClientHooksMixin.java`
  - [ ] 新增内部 `RenderStage` enum，或映射到 Fabric `WorldRenderEvents` 阶段。
  - [ ] 原 `ClientHooks.dispatchRenderStage` mixin 不能在 Fabric 使用，改 mixin Vanilla `LevelRenderer` 或使用 Fabric render event。
- [ ] `ClientHooks` / `GuiLayerManager`
  - [ ] 文件：`features/items/mixins/compatibility/*`、`features/filter/mixins/ClientHooksMixin.java`
  - [ ] 删除 NeoForge hook mixin，改用 Vanilla/Fabric screen/HUD/world render 注入点。
- [ ] `ItemDecoratorHandler`
  - [ ] 文件：`GuiBatchingController.java`、`DecoratorDrawContext.java`、`GuiGraphicsMixin.java`
  - [ ] Fabric Vanilla 没有 NeoForge item decorator pipeline；第一阶段删除该 NeoForge decorator 加速路径。
  - [ ] 若后续发现 Fabric 生态有目标 Mod 的装饰 API，再为该 Mod 单独做 compat。
- [ ] `ModelData`
  - [ ] 文件：`IAcceleratedBakedModel.java`、`ModelBlockRendererMixin.java`、多个 BakedModel mixin。
  - [ ] Fabric/Vanilla `BakedModel` 路径没有 NeoForge `ModelData` 参数；新增 `ModelDataBridge` 或删除参数。
  - [ ] 优先把 `IAcceleratedBakedModel.renderBlockFast(..., ModelData data, RenderType renderType)` 改成 Fabric 友好的内部上下文对象，例如 `BlockModelRenderContext data`，Fabric 初始实现为空对象。
- [ ] `CompositeModel` / `SeparateTransformsModel`
  - [ ] 文件：`BakedCompositeModelMixin.java`、`BakedSeparateTransformsModelMixin.java`
  - [ ] 这些是 NeoForge client model 类；Fabric 第一阶段移除或替换为 Fabric model API 等价路径。
- [ ] `IQuadTransformer`
  - [ ] 文件：`BakedQuadMixin.java`、`SimpleBakedModelMixin.java`
  - [ ] 用本地 `BakedQuadLayout` 常量替代 `STRIDE/POSITION/COLOR/UV0/UV2/NORMAL`。
  - [ ] 用实际 `BakedQuad#getVertices()` 布局校验常量，避免颜色/UV/light/normal 偏移错误。

## 阶段 9：Mixin 与映射校验

- [ ] 全局搜索并清零 `net.neoforged` import。
- [ ] 全局搜索并清零 mixin target descriptor 中的 `Lnet/neoforged/`。
- [ ] 对所有 Vanilla mixin 目标重新校验 Minecraft 1.21.1 Fabric/Mojang mappings 下的方法签名：
  - [ ] `LevelRenderer#renderLevel`
  - [ ] `GuiGraphics#renderItem` / `renderItemDecorations` / `innerBlit`
  - [ ] `Font#drawInBatch*`
  - [ ] `ItemRenderer#render` / `renderModelLists`
  - [ ] `ModelBlockRenderer#renderModel`
  - [ ] `BakedGlyph` / `BakedGlyph.Effect`
  - [ ] `RenderType.CompositeState`
- [ ] 对外部 Mod mixin 分批开启：
  - [ ] 第一轮只启用 core、feature.entities、feature.modelparts、feature.items、feature.text、feature.filter、compat.vanilla。
  - [ ] 第二轮启用 compat.iris。
  - [ ] 第三轮逐个启用 GeckoLib/EMF/ImmediatelyFast/FTB/TLM 等。
- [ ] 给可选 compat mixin plugin 增加 Fabric Loader 检查：
  - [ ] `iris`
  - [ ] `sodium`
  - [ ] `entity_model_features`
  - [ ] `geckolib`
  - [ ] `immediatelyfast`
  - [ ] `ftblibrary`
  - [ ] 其它确认存在 Fabric 版本的 Mod ID。
- [ ] 所有 `@Pseudo` mixin 确认缺失目标类时不会加载失败。
- [ ] 所有 MixinExtras 注入确认运行时可用。

## 阶段 10：Fabric 兼容层恢复顺序

- [ ] P0 Core/Vanilla：
  - [ ] 核心 buffer、mesh、compute shader、draw method。
  - [ ] Vanilla entity/modelpart/text/item/gui/filter。
  - [ ] 无 Sodium/Iris 时可进入世界并无明显视觉错误。
- [ ] P1 Sodium/Iris：
  - [ ] 替换 Fabric 构件。
  - [ ] 校验 Iris 包名、vertex format、shadow renderer、hand renderer、shader pipeline 类名。
  - [ ] 单独运行 `compat.iris.mixins.json`。
  - [ ] shader pack 开关、阴影渲染、手持物品渲染、实体渲染都要测试。
- [ ] P2 常见 Fabric 优化/模型 Mod：
  - [ ] ImmediatelyFast Fabric。
  - [ ] Entity Model Features Fabric。
  - [ ] Entity Texture Features Fabric。
  - [ ] GeckoLib Fabric。
- [ ] P3 生态差异大的 Mod：
  - [ ] Curios：评估替换为 Trinkets，类和 API 完全不同，不建议机械迁移。
  - [ ] Sophisticated Core：若无 Fabric 版，删除 compat。
  - [ ] FTB Library：确认 Fabric 包名和 UI 类。
  - [ ] Touhou Little Maid：确认是否存在 Fabric 1.21.1 版本，否则延后。

## 阶段 11：编译与启动验收

- [ ] 编译任务：
  - [ ] `./gradlew clean`
  - [ ] `./gradlew validateAccessWidener`
  - [ ] `./gradlew compileJava`
  - [ ] `./gradlew processResources`
  - [ ] `./gradlew build`
- [ ] 启动任务：
  - [ ] `./gradlew runClient`
  - [ ] 确认主菜单 Mod 列表显示名称、版本、图标。
  - [ ] 确认客户端-only 元数据不会让服务端加载客户端类。
- [ ] 运行时 smoke test：
  - [ ] 新世界进入不崩。
  - [ ] 实体渲染、盔甲层、手持物品、掉落物、方块实体。
  - [ ] 背包 GUI、容器 GUI、物品装饰文字、字体描边、热键栏。
  - [ ] F3+T 资源重载。
  - [ ] 切换配置项后重启生效。
  - [ ] 禁用每个 feature 后能回退 Vanilla 渲染。
- [ ] 兼容 smoke test：
  - [ ] Fabric API only。
  - [ ] Fabric API + Sodium。
  - [ ] Fabric API + Sodium + Iris。
  - [ ] Fabric API + Sodium + Iris + ImmediatelyFast。
  - [ ] 每次只增加一个可选兼容 Mod，发现崩溃就定位到对应 mixin config。

## 阶段 12：性能与视觉回归

- [ ] 对照阶段 0 的 NeoForge 基线：
  - [ ] 大量实体场景 FPS。
  - [ ] 大量高顶点实体/模型场景 FPS。
  - [ ] GUI 物品批处理场景 FPS。
  - [ ] Iris shader pack 下 FPS 和视觉一致性。
- [ ] 重点视觉检查：
  - [ ] 半透明排序。
  - [ ] 实体阴影。
  - [ ] 盔甲 trim。
  - [ ] 文本颜色、阴影、描边。
  - [ ] GUI scissor/viewport 恢复。
  - [ ] 光照、overlay、normal、UV 偏移。
- [ ] 重点稳定性检查：
  - [ ] 切换资源包。
  - [ ] 切换 shader pack。
  - [ ] 反复进出世界。
  - [ ] 窗口尺寸变化。
  - [ ] 显卡不支持 OpenGL 4.6 时给出清晰错误或自动禁用。

## 完成定义

- [ ] `./gradlew build` 成功生成 Fabric remapped jar。
- [ ] Fabric Loader + Fabric API 环境可启动客户端。
- [ ] 无可选兼容 Mod 时核心功能可用。
- [ ] Sodium + Iris 环境可启动并通过基础视觉测试。
- [ ] 配置文件可读写，核心配置项生效。
- [ ] 资源重载后 compute shader 不泄漏旧 program，且能重新编译。
- [ ] README 和 changelog 明确 Fabric 支持范围与已知限制。
