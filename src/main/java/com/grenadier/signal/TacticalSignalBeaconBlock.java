package com.grenadier.signal;

import com.grenadier.GrenadierMod;
import com.grenadier.smoke.UseContext;
import com.grenadier.smoke.UseDecision;
import com.grenadier.smoke.UsePolicyRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Optional;

public class TacticalSignalBeaconBlock extends Block implements EntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public TacticalSignalBeaconBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE, FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TacticalSignalBeaconBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != GrenadierMod.TACTICAL_SIGNAL_BEACON_BLOCK_ENTITY.get()) {
            return null;
        }
        return (tickerLevel, pos, tickerState, blockEntity) ->
                TacticalSignalBeaconBlockEntity.serverTick(tickerLevel, pos, tickerState, (TacticalSignalBeaconBlockEntity) blockEntity);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(ACTIVE, false);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(stack.getItem() instanceof DyeItem dye)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TacticalSignalBeaconBlockEntity beacon) {
            beacon.setBaseColor(dye.getDyeColor().getTextureDiffuseColor());
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            player.displayClientMessage(Component.translatable("message.grenadier.signal_color_changed"), true);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level instanceof ServerLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!(level.getBlockEntity(pos) instanceof TacticalSignalBeaconBlockEntity beacon)) {
            return InteractionResult.FAIL;
        }
        if (!beacon.canActivate(level.getGameTime())) {
            serverPlayer.displayClientMessage(Component.translatable("message.grenadier.signal_cooling_down", beacon.cooldownSecondsRemaining(level.getGameTime())), true);
            return InteractionResult.FAIL;
        }

        UseDecision decision = UsePolicyRegistry.authorize(new UseContext(
                serverPlayer,
                GrenadierMod.path("signal_beacon"),
                InteractionHand.MAIN_HAND,
                ItemStack.EMPTY,
                Optional.of(pos)
        ));
        if (!decision.allowed()) {
            serverPlayer.displayClientMessage(decision.denial(), true);
            return InteractionResult.FAIL;
        }
        int beamColor = decision.effectColor().orElse(beacon.baseColor());
        int smokeColor = decision.effectColor().orElse(beacon.baseSmokeColor());
        beacon.activate(beamColor, smokeColor);
        return InteractionResult.CONSUME;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return state.getValue(ACTIVE);
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(ACTIVE) ? 15 : 0;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
