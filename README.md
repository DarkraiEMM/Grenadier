# 掷弹兵 / Grenadier

<p align="center">
  <img src="src/main/resources/grenadier.png" width="192" alt="掷弹兵 / Grenadier logo">
</p>

[中文](#中文) | [English](#english)

## 中文

掷弹兵是一个面向 Minecraft 1.21.1 NeoForge 的独立双端战术投掷物模组。它提供多类手榴弹、可自由部署的地雷、体积烟雾和可重复使用的信号机，并允许服主调整主要玩法参数。

### 主要内容

- **烟雾弹**：支持染色识别条和对应颜色的体积烟；烟雾会遮挡视线、干扰敌对生物索敌，并对烟内玩家产生近距离灰视。
- **瀑布烟与挂点烟**：烟雾可以在地形边缘、墙面和顶部挂点处向下扩散。
- **燃烧弹**：生成持续燃烧区域，具有范围伤害、点燃与雨天衰减规则。
- **闪光弹**：根据距离、朝向和遮挡计算白屏强度，并能短暂干扰生物。
- **破片手雷**：带引信的范围爆炸，支持正常自伤；是否破坏地形可由服务端配置。
- **冲击手榴弹**：命中方块或实体后立即起爆的小范围爆炸物。
- **地雷**：包含踩压触发的反步兵地雷、正面扇区触发的定向破片地雷，以及可投掷部署、具有高伤爆燃和短时喷焰的热熔地雷。
- **射弹引爆**：原版箭矢和兼容的枪械射弹可以引爆已部署地雷。
- **信号机**：产生烟雾和居中光柱，可用染料切换信号颜色，并具有持续时间与冷却时间。

### 环境与依赖

| 项目 | 版本/要求 |
| --- | --- |
| Minecraft | `1.21.1` |
| NeoForge | `21.1.228+` |
| Java | `21` |
| Veil（客户端） | `4.1.4–5.0.0` |

客户端体积烟依赖 Veil。已经通过其他模组获得兼容 Veil 的玩家不需要重复安装；不使用此类整合时，安装官方独立 Veil 即可。专用服务端不因渲染功能而强制要求 Veil 或 Sable。

### 安装

1. 在客户端和服务端安装匹配版本的 NeoForge。
2. 将 `grenadier-1.0.5.jar` 放入双方的 `mods` 目录。
3. 客户端安装 Veil 4.1.4–5.0.0，或使用已经提供兼容 Veil 的模组组合。

### 配置文件

首次进入世界后，服务端配置会生成在：

```text
<世界目录>/serverconfig/grenadier-server.toml
```

其中可以调整烟雾浓度和半径、各种投掷物的引信与弹性、燃烧持续时间、爆炸范围、地雷伤害与触发参数等。整合包作者可以把确认后的文件复制到 `defaultconfigs/grenadier-server.toml`，作为新世界默认值。

### 构建与测试

```powershell
.\gradlew.bat check
.\gradlew.bat build
```

构建产物位于 `build/libs/grenadier-1.0.5.jar`。版本变化见[更新日志](CHANGELOG.md)，完整工程边界和发布审计记录见[项目档案](docs/PROJECT_DOSSIER.md)。

## English

Grenadier is a standalone client-and-server tactical explosives mod for Minecraft 1.21.1 on NeoForge. It adds multiple grenade types, freely deployable mines, volumetric smoke concealment, and a reusable signal beacon, with server-side tuning for the main gameplay parameters.

### Features

- **Smoke grenades:** Dyeable identification stripes and matching volumetric smoke. Smoke blocks sightlines, disrupts hostile-mob targeting, and applies close-range gray vision to players inside it.
- **Waterfall and attached smoke:** Smoke can descend over terrain edges and from wall or ceiling attachment points.
- **Incendiary grenades:** Persistent fire fields with area damage, ignition, and rain attenuation rules.
- **Flashbangs:** Distance-, facing-, and occlusion-aware whiteout effects with temporary mob disruption.
- **Fragmentation grenades:** Fused area explosions with configurable terrain damage and normal self-damage.
- **Impact grenades:** Compact contact-fused explosives that detonate immediately on a block or entity hit.
- **Deployable mines:** Pressure-triggered anti-personnel mines, forward-sector directional fragmentation mines, and throwable thermite mines with a high-damage burst followed by a short flame jet.
- **Projectile detonation:** Vanilla arrows and compatible gun projectiles can detonate deployed mines.
- **Signal beacon:** Reusable colored smoke and a centered beacon beam with configurable duration and cooldown.

### Requirements

| Component | Version/requirement |
| --- | --- |
| Minecraft | `1.21.1` |
| NeoForge | `21.1.228+` |
| Java | `21` |
| Veil (client) | `4.1.4–5.0.0` |

Volumetric smoke requires Veil on the client. Players who already receive a compatible Veil build through another mod do not need a duplicate standalone installation. Dedicated servers are not forced to install Veil or Sable for client rendering.

### Installation

1. Install a matching NeoForge version on the client and server.
2. Put `grenadier-1.0.5.jar` in both `mods` directories.
3. On the client, install Veil 4.1.4–5.0.0 or use a compatible mod setup that already provides Veil.

### Configuration

After the world is opened once, the server configuration is generated at:

```text
<world>/serverconfig/grenadier-server.toml
```

It exposes smoke density and radius, fuse and restitution values, incendiary duration, explosion ranges, mine damage, trigger behavior, and other gameplay settings. Modpack authors can copy a tested file to `defaultconfigs/grenadier-server.toml` to use it as the default for newly created worlds.

### Build and test

```powershell
./gradlew check
./gradlew build
```

The output JAR is written to `build/libs/grenadier-1.0.5.jar`. See the [changelog](CHANGELOG.md) and [project dossier](docs/PROJECT_DOSSIER.md) for release details and the verified project boundary.

## License

All Rights Reserved.
