# 掷弹兵 / Grenadier

[中文](#中文) | [English](#english)

## 中文

掷弹兵是一个面向 Minecraft 1.21.1 NeoForge 的独立双端战术投掷物模组。它提供烟雾弹、燃烧弹、闪光弹、破片手雷和可重复使用的信号机，并为服务器玩法预留了使用规则与消耗接口。

### 主要内容

- **烟雾弹**：支持染色识别条和对应颜色的体积烟；烟雾会遮挡视线、干扰敌对生物索敌，并对烟内玩家产生近距离灰视。
- **瀑布烟与挂点烟**：烟雾可以在地形边缘、墙面和顶部挂点处向下扩散。
- **燃烧弹**：生成持续燃烧区域，具有范围伤害、点燃与雨天衰减规则。
- **闪光弹**：根据距离、朝向和遮挡计算白屏强度，并能短暂干扰生物。
- **破片手雷**：带引信的范围爆炸；是否破坏地形可由服务端配置。
- **信号机**：产生烟雾和居中光柱，可用染料切换信号颜色，并具有持续时间与冷却时间。

### 环境与依赖

| 项目 | 版本/要求 |
| --- | --- |
| Minecraft | `1.21.1` |
| NeoForge | `21.1.228+` |
| Java | `21` |
| Veil（客户端） | `4.1.4–5.0.0` |

客户端体积烟依赖 Veil。Sable 2.0.3 已通过 Jar-in-Jar 内嵌 Veil 4.1.4，因此已经安装 Sable 的玩家不需要再安装一份独立 Veil；不使用 Sable 时，安装官方独立 Veil 即可。专用服务端不因渲染功能而强制要求 Veil 或 Sable。

### 安装

1. 在客户端和服务端安装匹配版本的 NeoForge。
2. 将 `grenadier-1.0.0.jar` 放入双方的 `mods` 目录。
3. 客户端安装 Veil，或安装已经内嵌兼容 Veil 的 Sable 2.0.3。

### 构建与测试

```powershell
.\gradlew.bat check
.\gradlew.bat build
```

构建产物位于 `build/libs/grenadier-1.0.0.jar`。完整工程边界和发布审计记录见[项目档案](docs/PROJECT_DOSSIER.md)。

## English

Grenadier is a standalone client-and-server tactical throwable mod for Minecraft 1.21.1 on NeoForge. It adds smoke grenades, incendiary grenades, flashbangs, fragmentation grenades, and a reusable signal beacon, with extension hooks for server-side use rules and resource costs.

### Features

- **Smoke grenades:** Dyeable identification stripes and matching volumetric smoke. Smoke blocks sightlines, disrupts hostile-mob targeting, and applies close-range gray vision to players inside it.
- **Waterfall and attached smoke:** Smoke can descend over terrain edges and from wall or ceiling attachment points.
- **Incendiary grenades:** Persistent fire fields with area damage, ignition, and rain attenuation rules.
- **Flashbangs:** Distance-, facing-, and occlusion-aware whiteout effects with temporary mob disruption.
- **Fragmentation grenades:** Fused area explosions with configurable terrain damage.
- **Signal beacon:** Reusable colored smoke and a centered beacon beam with configurable duration and cooldown.

### Requirements

| Component | Version/requirement |
| --- | --- |
| Minecraft | `1.21.1` |
| NeoForge | `21.1.228+` |
| Java | `21` |
| Veil (client) | `4.1.4–5.0.0` |

Volumetric smoke requires Veil on the client. Sable 2.0.3 embeds Veil 4.1.4 through Jar-in-Jar, so Sable users do not need a duplicate standalone Veil installation. Players who do not use Sable can install the official standalone Veil release. Dedicated servers are not forced to install Veil or Sable for client rendering.

### Installation

1. Install a matching NeoForge version on the client and server.
2. Put `grenadier-1.0.0.jar` in both `mods` directories.
3. On the client, install Veil or Sable 2.0.3 with its embedded compatible Veil.

### Build and test

```powershell
./gradlew check
./gradlew build
```

The output JAR is written to `build/libs/grenadier-1.0.0.jar`. See the [project dossier](docs/PROJECT_DOSSIER.md) for the verified project boundary and release-audit notes.

## License

All Rights Reserved.
