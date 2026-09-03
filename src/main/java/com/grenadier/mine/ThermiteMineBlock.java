package com.grenadier.mine;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ThermiteMineBlock extends AbstractMineBlock {
    public static final MapCodec<ThermiteMineBlock> CODEC = simpleCodec(ThermiteMineBlock::new);
    private static final VoxelShape SHAPE = Block.box(5.5D, 0.0D, 5.5D, 10.5D, 1.15D, 10.5D);

    public ThermiteMineBlock(BlockBehaviour.Properties properties) { super(properties); }
    @Override protected MapCodec<? extends ThermiteMineBlock> codec() { return CODEC; }
    @Override public MineKind kind() { return MineKind.THERMITE; }
    @Override protected VoxelShape mineShape(BlockState state) { return SHAPE; }
}
