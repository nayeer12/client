package net.tw9ine.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.tw9ine.Tw9ine;
import net.tw9ine.event.events.BlockStateEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {

	@Shadow
	private Level level;

	@Inject(method = "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Lnet/minecraft/world/level/block/state/BlockState;", at = {
			@At(value = "RETURN") })
	private void onSetBlockState(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir) {
		if (level.isClientSide()) {
			Tw9ine.getInstance().eventManager.Fire(new BlockStateEvent(pos, cir.getReturnValue(), state));
		}
	}
}
