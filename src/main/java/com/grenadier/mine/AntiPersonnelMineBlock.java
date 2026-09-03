package com.grenadier.mine;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AntiPersonnelMineBlock extends AbstractMineBlock {
    public static final MapCodec<AntiPersonnelMineBlock> CODEC = simpleCodec(AntiPersonnelMineBlock::new);
    public static final BooleanProperty SOFT_GROUND = BooleanProperty.create("soft_ground");
    private static final VoxelShape SURFACE_SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 1.5D, 11.0D);
    private static final VoxelShape BURIED_SHAPE = Block.box(5.5D, 0.0D, 5.5D, 10.5D, 0.85D, 10.5D);

    public AntiPersonnelMineBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SOFT_GROUND, false));
    }

    @Override protected MapCodec<? extends AntiPersonnelMineBlock> codec() { return CODEC; }
    @Override public MineKind kind() { return MineKind.ANTI_PERSONNEL; }
    @Override protected VoxelShape mineShape(BlockState state) { return state.getValue(SOFT_GROUND) ? BURIED_SHAPE : SURFACE_SHAPE; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(SOFT_GROUND); }
}
