package com.leetftw.complexpipes.common.mixin;

import com.leetftw.complexpipes.common.ComplexPipes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PointedDripstoneBlock.class)
abstract class MixinTest {
    @Inject(method = "maybeTransferFluid", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/block/PointedDripstoneBlock;findFillableCauldronBelowStalactiteTip(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/Fluid;)Lnet/minecraft/core/BlockPos;"))
    private static void fluidTransfer(CallbackInfo ci) {

    }
}
