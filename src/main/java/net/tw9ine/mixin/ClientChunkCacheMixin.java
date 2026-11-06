/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.tw9ine.Tw9ine;
import net.tw9ine.event.events.ChunkEvent;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

@Mixin(ClientChunkCache.class)
public abstract class ClientChunkCacheMixin {

	@Shadow
	public abstract LevelChunk getChunk(int x, int z, ChunkStatus status, boolean create);

	@Inject(method = "replaceWithPacketData", at = @At("RETURN"))
	private void onChunkLoaded(int x, int z, ClientboundLevelChunkPacketData chunkData, CallbackInfoReturnable<LevelChunk> cir) {
		LevelChunk chunk = cir.getReturnValue();
		if (chunk != null) {
			Tw9ine.getInstance().eventManager.Fire(new ChunkEvent.Loaded(chunk));
		}
	}

	@Inject(method = "drop", at = @At("HEAD"))
	private void onChunkUnloaded(ChunkPos pos, CallbackInfo ci) {
		LevelChunk chunk = getChunk(pos.x(), pos.z(), ChunkStatus.FULL, false);
		if (chunk != null) {
			Tw9ine.getInstance().eventManager.Fire(new ChunkEvent.Unloaded(chunk));
		}
	}
}
