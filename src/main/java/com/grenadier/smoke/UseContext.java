package com.grenadier.smoke;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record UseContext(ServerPlayer player, ResourceLocation action, InteractionHand hand, ItemStack stack, Optional<BlockPos> pos) {
}
