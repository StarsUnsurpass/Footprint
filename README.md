# 足迹 (Footprint) - 赛博朋克探索记录器

<div align="center">

**一款基于 Jetpack Compose 构建，拥有极致液态玻璃 (Glassmorphism) 视觉效果的足迹追踪应用**

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![Map](https://img.shields.io/badge/Map-AMap-blue.svg)](https://lbs.amap.com/)

</div>

---

## ✨ 核心特性

- 🧪 **液态玻璃 UI**：深度定制的 Material 3 界面，模拟 iOS/macOS 的背景模糊与毛玻璃质感。
- 🌃 **赛博朋克配色**：内置高饱和度霓虹配色方案，让旅行记录极具科技感。
- 📍 **高德定位集成**：针对国内环境优化的 AMap 定位引擎，支持实时轨迹绘制。
- 🛡️ **智能隐私围栏**：独创“幽灵模式”，自动隐藏敏感区域（如家、公司）的精准坐标。
- 🏆 **勋章博物馆**：根据探索深度自动解锁地域特色成就。
- 📝 **AI 故事引擎**：基于地理位置和心情，一键生成旅行文学草稿。

---

## 🎨 最新视觉更新 (New!)

- **全域液态玻璃**：所有页面（概览、足迹海、目标页、弹窗）均已升级为高通透的液态玻璃风格。
- **动态渐变背景**：新增 `AppBackground`，提供淡雅且富有层次的全局背景，增强沉浸感。
- **高分屏优化**：摒弃传统模糊滤镜，采用高精度矢量渐变与光影模拟，确保在高分辨率屏幕上字体与图标锐利清晰，无锯齿。

---

## 🚀 快速上手 (配置 API Key)

为了保护隐私，本项目仓库不包含高德地图 API Key。请按照以下步骤配置以运行项目：

### 1. 申请高德 Key
1. 前往 [高德开放平台控制台](https://console.amap.com/)。
2. 创建一个 **Android 平台** 的应用。
3. **获取 SHA1**：在项目根目录下运行 `./gradlew signingReport`，复制 `Variant: debug` 下的 SHA1 值。
4. 将你的 **包名** (`com.footprint`) 和 **SHA1** 填入高德后台，生成 API Key。

### 2. 应用内配置 Key (New!)
无需修改代码或配置文件！
1. 编译并安装应用。
2. 打开地图界面，点击右上角的 **设置 (⚙️)** 按钮。
3. 在弹出的玻璃风格对话框中输入你的 Key 并保存。
4. 重启应用即可生效。

---

## 🛠️ 技术架构

- **UI**: Jetpack Compose (Declarative UI)
- **Navigation**: Compose Navigation with Custom Animations
- **Database**: Room Persistence Library
- **Architecture**: MVVM + Repository Pattern
- **Async**: Kotlin Coroutines & Flow
- **Maps**: AMap 3D SDK & Location SDK

---

## 📂 项目结构

- `app/src/main/java/com/footprint/ui/theme`：定制的液态玻璃主题与赛博朋克调色板。
- `app/src/main/java/com/footprint/ui/components`：核心 UI 组件库，包含 `GlassMorphicCard` 和 `AppBackground`。
- `app/src/main/java/com/footprint/service`：高性能后台定位追踪服务。
- `app/src/main/java/com/footprint/utils`：API Key 安全管理工具。
- `app/src/main/java/com/footprint/ui/screens/MapScreen`：核心地图交互逻辑。

---

## 🕒 更新日志 (Changelog)

### 2026-01-03: iOS 26 液态玻璃视觉重构 (v1.8.0)

#### ✨ 新增特性 (Features)
- **iOS 26 液态玻璃 UI (Liquid Glass 重构)**: 
    - 升级了全站 `GlassMorphicCard` 组件，引入了基于物理折射模拟的渐变边框与多重阴影，视觉通透感大幅提升。
    - 所有列表项与统计卡片均已适配全新的液态质感，支持微弱的内部高光。
- **动态流动背景 (Liquid Animated Backgrounds)**: 
    - 引入了基于 `InfiniteTransition` 的动态气泡背景。柔和的彩色光斑在屏幕后方缓缓流动，模拟流体质感，为应用注入生命力。
- **交互级物理反馈**: 
    - **触控缩放**: 点击统计方块时，卡片会产生细腻的物理收缩反馈（Scale-down），模拟真实的物理按压感。
    - **底部栏进化**: 导航栏图标在选中时拥有弹性放大效果（Bouncy Scaling），配合沉浸式毛玻璃，手感极佳。
- **视觉层级精修**: 优化了全局组件的圆角（24dp）与间距，确保在不同尺寸屏幕上的精致感。

#### 🐛 修复日志 (Bug Fixes)
- **稳定性增强**: 彻底修复了因多流合并导致的 `type variable R` 推断错误及 Repository 误删方法的隐患。
- **路由鲁棒性**: 优化了底部栏显示判定逻辑，彻底解决启动时的空指针闪退。

### 2026-01-03: 智能交互看板与年度数据深度联动 (v1.7.0)

#### ✨ 新增特性 (Features)
- **智能交互数据看板**:
    - **年度聚合**: 数据概览全面升级为年度视角，活力指数与主情绪均基于全年数据加权统计。
    - **点击下钻**: 点击“记录”看板可瞬间弹出全年记录明细；点击“地点”看板可查看年度足迹分布。
    - **足迹回放联动**: 点击“足迹”看板将直接跳转至**对应年份**的轨迹回放，实现跨页面状态同步。
- **智能按月折叠列表**:
    - “年度足迹轨迹”与“年度旅行目标”引入月份折叠功能，支持一键展开/隐藏，大幅提升长列表的操作效率。
    - 列表项视觉区分优化：目标板块采用全新的橙色系风格与边框设计，与足迹流形成鲜明对比。
- **全局布局优化**: 
    - 年份筛选器移至首页顶端，作为全局数据的“总开关”。
    - 移除了冗余的文案说明（如“精确到分钟”），界面更趋极简。

#### 🐛 修复日志 (Bug Fixes)
- **代码健壮性**: 修复了 `AddFootprintDialog` 中的语法嵌套错误，确保编译完全通过。
- **动画优化**: 引入 `animateContentSize` 为看板切换和列表折叠提供物理级流畅反馈。

### 2026-01-03: 个性化图标与旅行者等级系统 (v1.6.0)

#### ✨ 新增特性 (Features)
- **自定义图标选择 (Custom Icon Selection)**: 
    - 在添加足迹或创建旅程目标时，用户可以从数十款精选图标中自由选择（如餐厅、飞行、骑行、摄影等）。
    - 列表展示全面升级，使用用户选定的图标替换原有的文字占位符，视觉更加直观生动。
- **旅行者等级系统 (Traveler Ranking)**: 
    - 引入基于年度总里程的荣誉等级：从“新手旅行者”到“传奇旅行家”。
    - 在首页顶部展示动态等级勋章，实时记录您的探索成就。
- **动态时段寄语**: 首页欢迎语现已支持随时间（早安、午后、晚安）动态变化，提供更温情的交互体验。
- **月份折叠交互升级**: 优化了年度足迹与目标的按月折叠动画，视觉效果更加丝滑。

#### 🐛 修复日志 (Bug Fixes)
- **统计逻辑修复**: 修复了 StatDetailContent 在某些分支下的语法异常问题。
- **布局优化**: 将年份筛选器上移至数据概览上方，优化了整体操作流。

### 2026-01-03: 智能回忆引擎与活力数据看板 (v1.5.0)

#### ✨ 新增特性 (Features)
- **智能回忆引擎 (On This Day)**: 
    - **历史重连**: 首页“那年今日”板块现在会自动寻找往年同一日期的足迹，带您精准重温时空记忆。
    - **诗意寄语**: 若历史上今日无记录，系统将随机展示关于时光与回忆的唯美寄语，确保应用始终充满温度。
- **活力数据看板 (Real-time Analytics)**:
    - **活力指数 (Vitality Index)**: 引入基于记录频率、能量水平和行走里程的加权计算模型，实时反映您的生活活跃度。
    - **年度/月度深度统计**: 数据概览卡片现已支持随年份选择实时刷新，足迹、里程、地点分布一目了然。
- **智能视觉进化**:
    - **自适应心情主题 (Auto Mood Theme)**: 新增“智能自适应”视觉风格，应用主题色可随本月主导情绪（如开心->赛博朋克，平静->森林）自动变换。
    - **自适应图标 (Adaptive Icon)**: 全新设计的 App 图标上线，采用深邃科技蓝背景与极简足迹定位符，完美适配 Android 各类启动器形状。
- **全局导航优化**:
    - 统一了“时光碎片”到“足迹簿”的跳转逻辑，修复了因返回栈冲突导致无法回到首页的问题。
    - 解决了全局年份过滤对全量足迹流的误伤，确保“足迹簿”能完整展示所有历史时刻。

#### 🐛 修复日志 (Bug Fixes)
- **类型安全性**: 修复了 `FootprintAnalytics` 中 Double 与 Int 字面量类型不匹配导致的编译失败。
- **全量列表展示**: 移除全局年份过滤器对搜索结果和足迹流的限制。

### 2026-01-02: 数据备份与恢复功能上线 (v1.4.0)

#### ✨ 新增特性 (Features)
- **全量数据备份 (Data Export)**: 在设置页面新增“导出足迹备份”功能，支持将所有足迹、旅行目标及轨迹点导出为 JSON 文件，保存到本地或云盘。
- **数据恢复 (Data Import)**: 新增“导入历史记录”功能，支持从备份文件一键恢复所有历史数据，彻底解决因应用重装或数据库迁移导致的数据丢失问题。
- **地图定位深度优化**: 修复了地图定位“点击两次才生效”的 Bug，实现了启动即定位、切回即对焦的流畅体验。
- **黑夜模式 UI 补完**: 修复了“记录足迹”弹窗中活力指数等文本在黑夜模式下的颜色显示问题。

### 2026-01-02: Telegram 风格视觉重构与交互深度美化 (v1.3.0)

#### ✨ 新增特性 (Features)
- **Telegram 视觉语言注入**:
    - **沉浸式顶部栏**: 实现了仿 Telegram 的半透明毛玻璃顶部栏，随滑动内容动态响应。
    - **圆形头像系统**: 足迹记录引入了首字母圆形头像标识，配合心情颜色，大幅提升列表辨识度。
    - **层级感列表布局**: 优化了信息的垂直排布，使用更现代的间距与排版，支持标签与元数据的紧凑展示。
- **悬浮悬挂式导航 (Floating Bottom Nav)**: 
    - 重新设计了底部导航栏，采用全悬浮圆角胶囊造型，视觉更轻盈。
- **全局 UI 细节优化**:
    - **Squircle 圆角**: 所有的卡片和按钮均采用了更平滑的连续圆角处理。
    - **搜索栏进化**: 新增了极简风格的嵌入式搜索框，交互更自然。
    - **目标进度可视化**: 目标管理页面引入了仿 Telegram Bot 的线性进度展示与自定义复选交互。

#### 🐛 修复日志 (Bug Fixes)
- **回调逻辑修正**: 修复了概览页“新建计划”按钮错误跳转到足迹记录弹窗的问题。

### 2026-01-02: 物理弹性动画与深度交互升级 (v1.2.0)

#### ✨ 新增特性 (Features)
- **物理弹性动画 (Elastic Physics Animations)**: 
    - 导航切换升级为基于物理特性的 `spring` 动画，过渡更自然。
    - 列表容器全面重构为 `LazyColumn`，滑到最上端和最下端时具备富有弹性的“阻尼”效果，手感媲美主流社交 App。
- **外观定制与持久化 (Theme & Persistence)**:
    - 新增 **设置界面**，支持“白天/黑夜/系统跟随”模式的手动切换。
    - 引入 `PreferenceManager` 实现设置持久化，即使重启应用也能记住您的偏好。
- **深度数据下钻 (Data Drill-down)**:
    - 点击首页的“总记录”、“里程”、“独特地点”、“活力”、“主情绪”等统计模块，可弹出玻璃风格详情页，查看对应维度的详细记录与分布统计。
- **足迹坐标关联 (Marker Integration)**:
    - 足迹记录现已支持保存经纬度坐标（创建时自动抓取当前位置）。
    - 地图页面支持展示历史足迹标记，点击标记可弹出该足迹的迷你详情卡片。

#### 🐛 修复日志 (Bug Fixes)
- **全域夜间模式优化**: 修复了夜间模式下背景颜色与地图样式不同步的问题，实现了包含对话框、模糊遮罩及高德地图在内的全局深度适配。
- **引用错误修复**: 解决了 `spring` 动画与 `Brush` 绘图类在特定类中的 unresolved reference 问题。

### 2026-01-01: 历史足迹回放与交互体验升级 (v1.1.0)

#### ✨ 新增特性 (Features)
- **历史足迹回放 (History Replay)**: 在概览页新增“历史足迹”模块，支持按分钟级精度筛选时间段，在地图上动态回放用户的移动轨迹。
- **无限列表展示 (Unlimited Lists)**: 概览页的“最近灵感”与“我的目标”现已解除显示数量限制，支持浏览所有历史记录与计划。
- **全域编辑支持 (Editable Content)**: 概览页的“最近灵感”卡片和目标页的“旅行目标”均支持点击编辑，随时修正记录。
- **沉浸式交互优化 (Enhanced UI)**: 优化全局弹窗体验，当对话框（如新建记录、编辑目标）弹出时，背景自动应用高斯模糊与雾化遮罩，提升文字可读性。

#### 🐛 修复日志 (Bug Fixes)
- **定位干扰修复**: 优化 `LocationTrackingService` 错误处理逻辑，屏蔽因弱网或GPS信号波动导致的非关键错误（Error 10）弹窗，仅保留关键鉴权与权限错误提示。

### 2025-12-31: 液态玻璃 UI 重构与核心 Bug 修复

#### ✨ 新增特性 (Features)
- **UI 重构**: 全面升级为液态玻璃 (Glassmorphism) 风格，新增 `GlassMorphicCard` 和 `AppBackground` 组件，适配高分屏光影。
- **动态配置**: 支持应用内动态配置高德地图 API Key，移除对 `AndroidManifest.xml` 硬编码依赖。
- **开发者辅助**: 在 Key 配置弹窗中新增 **SHA1 一键复制** 功能，解决高德控制台配置难题。

#### 🐛 核心修复日志 (Bug Fixes & Root Causes)

| 问题现象 (Issue) | 根本原因 (Root Cause) | 解决方案 (Fix) |
| :--- | :--- | :--- |
| **地图白屏/黑屏** | Jetpack Compose 的 `AndroidView` 与高德 `MapView` 生命周期不同步。当 Compose 首次渲染时，Activity 可能已过 `ON_CREATE`，导致地图 OpenGL 上下文未初始化。 | 在 `DisposableEffect` 中显式手动调用 `mapView.onCreate()`，并根据当前 Lifecycle 状态自动补偿 `onResume()`。 |
| **定位错误 7 (鉴权失败)** | `build.gradle.kts` 配置中 `debug` 类型包含 `applicationIdSuffix = ".debug"`。这导致调试版包名变为 `com.footprint.debug`，与高德后台注册的 `com.footprint` 不匹配，触发安全拦截。 | 移除 `applicationIdSuffix` 配置，确保 Debug 和 Release 版本包名严格一致 (`com.footprint`)。 |
| **Key 设置后不生效** | SDK 的 `MapsInitializer` 和 `LocationClient` 默认只在应用启动时读取一次 Key，运行时更新未触发重新初始化。 | 在保存 Key 的瞬间，代码级强制调用 SDK 的 `setApiKey` 接口，实现配置即时生效无需重启。 |
| **定位一直请求中** | 错误信息被吞没，权限、网络还是 Key 的问题。 | 增加错误码拦截机制，在主线程弹出 Toast 明确提示错误类型（如“Error 7: 包名不匹配”或“Error 12: 缺权限”）。 |

---

## 🤝 贡献与反馈

欢迎提交 PR 或 Issue 来完善这个项目！

1. Fork 本项目。
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)。
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)。
4. 推送到分支 (`git push origin feature/AmazingFeature`)。
5. 开启 Pull Request。

---

<div align="center">
Made with ❤️ by StarsUnsurpass
</div>
