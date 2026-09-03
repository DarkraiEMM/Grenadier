# “掷弹兵”独立模组项目档案

## 1. 建档信息

| 字段 | 当前记录 |
| --- | --- |
| 档案编号 | `GRD-MOD-001` |
| 中文名称 | 掷弹兵 |
| 英文名称 | Grenadier |
| 模组 ID | `grenadier` |
| Java 包名 | `com.grenadier` |
| 当前版本 | `1.0.5` |
| 项目性质 | 独立双端模组 |
| 运行边界 | 独立注册、独立配置、独立网络协议、独立 Jar |
| 源码形态 | 可独立构建的 Gradle 工程，同时由 `armsrace-suite` 作为子模块引用 |
| 建档状态 | 已建档，发布前审计中 |
| 建档日期 | 2026-08-02 |
| 许可证 | All Rights Reserved |

本档案确认：“掷弹兵”与“军备竞赛”是两个不同的模组。军备竞赛根工程当前会在开发构建中引用 `:grenadier`，但掷弹兵源码没有反向引用 `com.armsrace`，游戏加载时也使用独立的 `grenadier` 模组 ID。

## 2. 正式功能边界

当前面向玩家的正式内容为：

1. 烟雾弹
   - 可染色识别条和烟雾颜色；
   - 烟云遮挡、玩家烟内灰视和敌对生物索敌干扰；
   - Veil 体积烟与 Iris 光影桥接；
   - 支持落地烟、弹墙/挂点烟和向下瀑布烟。
2. 燃烧弹
   - 低弹跳投掷；
   - 范围燃烧场、持续伤害和点燃；
   - 包含雨天规则。
3. 闪光弹
   - 视线、距离和有效半径判定；
   - 玩家白屏/衰减效果与生物眩晕。
4. 破片手雷
   - 延时引信和范围爆炸；
   - 是否破坏方块可由服务端配置控制。
5. 信号机
   - 可重复使用；
   - 默认光柱和烟雾源色；
   - 可使用染料改变本次信号颜色；
   - 具有持续时间与冷却配置。
6. 冲击手榴弹
   - 接触方块或实体后立即爆炸；
   - 与延时引信的破片手雷作为独立品类共存。
7. 可部署地雷
   - 反步兵地雷采用脚下接触触发；
   - 定向破片地雷采用正面扇区检测；
   - 热熔地雷可投掷部署，先产生无击退、高穿甲的瞬时爆发，再追加短时喷焰；
   - 已部署地雷为实体，可在方块表面自由定位，并支持射弹引爆。

创造模式页签展示上述投掷物、三类地雷和信号机；旧版兼容项不会展示。

## 3. 已验证技术环境

| 项目 | 固定值 | 证据位置 |
| --- | --- | --- |
| Minecraft | `1.21.1` | 根目录 `gradle.properties` |
| NeoForge | `21.1.228` | 根目录 `gradle.properties` |
| ModDevGradle | `2.0.141` | `grenadier/build.gradle` |
| Java 工具链 | `21` | `grenadier/build.gradle` |
| Parchment | MC `1.21.1` / mappings `2024.11.17` | 根目录 `gradle.properties` |
| Veil 编译版本 | `4.1.4` | 根目录 `gradle.properties`、`grenadier/build.gradle` |
| 测试框架 | JUnit Jupiter `5.10.3` | `grenadier/build.gradle` |

没有 Access Transformer。Mixin 配置为可选客户端配置，当前仅包含 Iris 渲染管线桥接。

## 4. 代码与资源边界

- 公共入口：`src/main/java/com/grenadier/GrenadierMod.java`
- 服务端配置：`src/main/java/com/grenadier/GrenadierConfig.java`
- 客户端专用代码：`src/main/java/com/grenadier/client/`
- 烟雾系统：`src/main/java/com/grenadier/smoke/`
- 燃烧弹：`src/main/java/com/grenadier/incendiary/`
- 闪光弹：`src/main/java/com/grenadier/flashbang/`
- 破片手雷：`src/main/java/com/grenadier/grenade/`
- 信号机与烟雾弹投射物：`src/main/java/com/grenadier/signal/`
- 网络协议：`src/main/java/com/grenadier/network/`
- Iris 兼容 Mixin：`src/main/java/com/grenadier/mixin/iris/`
- 资产与数据：`src/main/resources/assets/grenadier/`、`src/main/resources/data/grenadier/`
- 模组元数据模板：`src/main/templates/META-INF/neoforge.mods.toml`
- 自动化测试：`src/test/java/com/grenadier/`

语言资源已同时提供：`en_us`、`zh_cn`、`zh_tw`。

## 5. 构建和验证

进入掷弹兵目录后执行：

```powershell
.\gradlew.bat check
.\gradlew.bat build
```

发布候选 Jar 生成于：

```text
build/libs/grenadier-1.0.5.jar
```

2026-09-03 发布验证：`clean check build` 成功，40 项自动化测试全部通过；产物校验值以 1.0.5 GitHub Release 记录为准。

## 6. 独立性结论

### 已经独立的部分

- 独立 `modId=grenadier`；
- 独立 `com.grenadier` 包名；
- 独立注册表、配置和网络 payload；
- 独立资源命名空间 `grenadier`；
- 独立构建任务和 Jar；
- 根工程依赖掷弹兵，而掷弹兵不依赖军备竞赛源码。

### 与父工程的关系

- `grenadier` 目录拥有自己的 `settings.gradle`、`gradle.properties`、Gradle Wrapper 和构建脚本；
- 该目录可以单独复制为源码仓库并独立构建；
- `armsrace-suite` 仍通过 `implementation project(':grenadier')` 在开发环境引用它，但这不会让掷弹兵反向依赖军备竞赛；
- 两种构建入口必须继续锁定相同的 Minecraft、NeoForge、Java、Parchment 和 Veil 版本。

## 7. 发布前必须复核

1. **Veil 与 Sable 的发布说明**
   - 客户端体积烟直接使用 Veil API，因此模组元数据声明客户端要求 `veil` `4.1.4–5.0.0`；
   - Sable 2.0.3 的 Jar-in-Jar 内嵌并注册了 `veil` 4.1.4，可直接满足依赖，安装 Sable 时无需重复安装独立 Veil；
   - 不使用 Sable 的玩家只需安装独立 Veil；
   - 专用服务端不因渲染功能被强制要求安装 Veil 或 Sable。
2. **旧注册兼容壳**
   - `rally_core` 和 `signal_smoke_marker` 仍被注册并打入 Jar，但不在创造页展示；
   - 它们当前用于旧世界/旧命名空间兼容，不属于掷弹兵正式玩法；
   - 发布前需决定保留兼容、迁移数据或彻底移除。
3. **遗留资源命名空间**
   - 资源目录仍包含 `assets/steamdimension`；
   - 正式发布前需确认是否为必要兼容资源，否则应移除。
4. **发布资料**
   - 补充作者、项目主页、问题追踪地址、图标、中文/英文介绍、许可文本和更新日志。

## 8. 兼容与服务端原则

- 模组设计为双端安装；服务端负责投掷物、伤害、烟云判定、索敌干扰和配置。
- 客户端负责模型、粒子、闪光 HUD、体积烟与光影桥接。
- 客户端类必须继续限制在客户端订阅/加载边界内，专用服务端不得解析 Veil、Iris 或渲染类。
- 当前保留旧命名空间 `armsrace` 与 `smoke_grenade` 的注册别名，用于已有测试世界迁移；这不是两个额外模组。

## 9. 发布判定

当前结论是：**“掷弹兵”已经是独立运行模组和可独立构建的源码工程；依赖边界已收口为客户端 Veil，1.0.5 保留必要的旧注册别名以兼容既有测试世界。**
