package com.grenadier;

import com.grenadier.core.RallyCoreBlock;
import com.grenadier.incendiary.IncendiaryFieldEntity;
import com.grenadier.incendiary.IncendiaryGrenadeItem;
import com.grenadier.incendiary.IncendiaryGrenadeProjectile;
import com.grenadier.flashbang.FlashbangItem;
import com.grenadier.flashbang.FlashbangProjectile;
import com.grenadier.grenade.FragGrenadeItem;
import com.grenadier.grenade.FragGrenadeProjectile;
import com.grenadier.network.SmokeNetwork;
import com.grenadier.signal.SignalFlareItem;
import com.grenadier.signal.SignalFlareProjectile;
import com.grenadier.signal.SignalSmokeMarkerBlock;
import com.grenadier.signal.TacticalSignalBeaconBlock;
import com.grenadier.signal.TacticalSignalBeaconBlockEntity;
import com.grenadier.smoke.SmokeCloudService;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(GrenadierMod.MODID)
public final class GrenadierMod {
    public static final String MODID = "grenadier";
    public static final String ARMS_RACE_LEGACY_NAMESPACE = "armsrace";
    public static final String SMOKE_GRENADE_LEGACY_NAMESPACE = "smoke_grenade";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredBlock<Block> RALLY_CORE = BLOCKS.registerBlock(
            "rally_core", RallyCoreBlock::new,
            BlockBehaviour.Properties.of().strength(8.0F, 12.0F).requiresCorrectToolForDrops().sound(SoundType.METAL));
    public static final DeferredBlock<Block> TACTICAL_SIGNAL_BEACON = BLOCKS.registerBlock(
            "tactical_signal_beacon", TacticalSignalBeaconBlock::new,
            BlockBehaviour.Properties.of().strength(4.0F, 8.0F).requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(TacticalSignalBeaconBlock.ACTIVE) ? 9 : 0).sound(SoundType.METAL));
    public static final DeferredBlock<Block> SIGNAL_SMOKE_MARKER = BLOCKS.registerBlock(
            "signal_smoke_marker", SignalSmokeMarkerBlock::new,
            BlockBehaviour.Properties.of().instabreak().noCollission().noLootTable().sound(SoundType.METAL));

    public static final DeferredItem<Item> RALLY_CORE_ITEM = ITEMS.registerItem(
            "rally_core", properties -> new BlockItem(RALLY_CORE.get(), properties), new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> TACTICAL_SIGNAL_BEACON_ITEM = ITEMS.registerItem(
            "tactical_signal_beacon", properties -> new BlockItem(TACTICAL_SIGNAL_BEACON.get(), properties), new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> SIGNAL_FLARE = ITEMS.registerItem(
            "signal_flare", SignalFlareItem::new, new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> INCENDIARY_GRENADE = ITEMS.registerItem(
            "incendiary_grenade", IncendiaryGrenadeItem::new, new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> FLASHBANG = ITEMS.registerItem(
            "flashbang", FlashbangItem::new, new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> FRAG_GRENADE = ITEMS.registerItem(
            "frag_grenade", FragGrenadeItem::new, new Item.Properties().stacksTo(16));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TacticalSignalBeaconBlockEntity>> TACTICAL_SIGNAL_BEACON_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("tactical_signal_beacon",
                    () -> BlockEntityType.Builder.of(TacticalSignalBeaconBlockEntity::new, TACTICAL_SIGNAL_BEACON.get()).build(null));
    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorParticleOption>> COLORED_SIGNAL_SMOKE =
            PARTICLE_TYPES.register("colored_signal_smoke", () -> new ParticleType<ColorParticleOption>(true) {
                @Override
                public MapCodec<ColorParticleOption> codec() {
                    return ColorParticleOption.codec(this);
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
                    return ColorParticleOption.streamCodec(this);
                }
            });
    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorParticleOption>> GROUND_SMOKE_SHEET =
            PARTICLE_TYPES.register("ground_smoke_sheet", () -> new ParticleType<ColorParticleOption>(true) {
                @Override
                public MapCodec<ColorParticleOption> codec() {
                    return ColorParticleOption.codec(this);
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
                    return ColorParticleOption.streamCodec(this);
                }
            });
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> INCENDIARY_FLAME =
            PARTICLE_TYPES.register("incendiary_flame", () -> new SimpleParticleType(true));
    public static final DeferredHolder<EntityType<?>, EntityType<SignalFlareProjectile>> SIGNAL_FLARE_PROJECTILE =
            ENTITY_TYPES.register("signal_flare", () -> EntityType.Builder.<SignalFlareProjectile>of(SignalFlareProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(6).updateInterval(2)
                    .build(MODID + ":signal_flare"));
    public static final DeferredHolder<EntityType<?>, EntityType<IncendiaryGrenadeProjectile>> INCENDIARY_GRENADE_PROJECTILE =
            ENTITY_TYPES.register("incendiary_grenade", () -> EntityType.Builder.<IncendiaryGrenadeProjectile>of(
                            IncendiaryGrenadeProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(6).updateInterval(2)
                    .build(MODID + ":incendiary_grenade"));
    public static final DeferredHolder<EntityType<?>, EntityType<IncendiaryFieldEntity>> INCENDIARY_FIELD =
            ENTITY_TYPES.register("incendiary_field", () -> EntityType.Builder.<IncendiaryFieldEntity>of(
                            IncendiaryFieldEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F).clientTrackingRange(10).updateInterval(10)
                    .build(MODID + ":incendiary_field"));
    public static final DeferredHolder<EntityType<?>, EntityType<FlashbangProjectile>> FLASHBANG_PROJECTILE =
            ENTITY_TYPES.register("flashbang", () -> EntityType.Builder.<FlashbangProjectile>of(
                            FlashbangProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(8).updateInterval(2)
                    .build(MODID + ":flashbang"));
    public static final DeferredHolder<EntityType<?>, EntityType<FragGrenadeProjectile>> FRAG_GRENADE_PROJECTILE =
            ENTITY_TYPES.register("frag_grenade", () -> EntityType.Builder.<FragGrenadeProjectile>of(
                            FragGrenadeProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(8).updateInterval(2)
                    .build(MODID + ":frag_grenade"));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register(
            "main", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.grenadier"))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .icon(() -> SIGNAL_FLARE.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(SIGNAL_FLARE.get());
                        output.accept(INCENDIARY_GRENADE.get());
                        output.accept(FLASHBANG.get());
                        output.accept(FRAG_GRENADE.get());
                        output.accept(TACTICAL_SIGNAL_BEACON_ITEM.get());
                    }).build());

    public GrenadierMod(IEventBus modBus, ModContainer container) {
        registerLegacyAliases();
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
        PARTICLE_TYPES.register(modBus);
        ENTITY_TYPES.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
        modBus.addListener(SmokeNetwork::registerPayloads);
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER, GrenadierConfig.SPEC);
        NeoForge.EVENT_BUS.register(SmokeCloudService.INSTANCE);
    }

    public static ResourceLocation path(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    /** Returns an identifier from the pre-split Arms Race namespace. */
    public static ResourceLocation legacyPath(String path) {
        return ResourceLocation.fromNamespaceAndPath(ARMS_RACE_LEGACY_NAMESPACE, path);
    }

    private static void registerLegacyAliases() {
        addLegacyAliases(BLOCKS, "rally_core", "tactical_signal_beacon", "signal_smoke_marker");
        addLegacyAliases(ITEMS, "rally_core", "tactical_signal_beacon", "signal_flare",
                "incendiary_grenade", "flashbang", "frag_grenade");
        addLegacyAliases(BLOCK_ENTITY_TYPES, "tactical_signal_beacon");
        addLegacyAliases(PARTICLE_TYPES, "colored_signal_smoke", "ground_smoke_sheet", "incendiary_flame");
        addLegacyAliases(ENTITY_TYPES, "signal_flare", "incendiary_grenade", "incendiary_field",
                "flashbang", "frag_grenade");
        CREATIVE_MODE_TABS.addAlias(
                ResourceLocation.fromNamespaceAndPath(SMOKE_GRENADE_LEGACY_NAMESPACE, "main"),
                path("main"));
    }

    private static void addLegacyAliases(DeferredRegister<?> register, String... paths) {
        for (String entryPath : paths) {
            ResourceLocation target = path(entryPath);
            register.addAlias(ResourceLocation.fromNamespaceAndPath(ARMS_RACE_LEGACY_NAMESPACE, entryPath), target);
            register.addAlias(ResourceLocation.fromNamespaceAndPath(SMOKE_GRENADE_LEGACY_NAMESPACE, entryPath), target);
        }
    }
}
