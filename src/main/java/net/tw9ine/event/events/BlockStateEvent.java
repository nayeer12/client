/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.event.events;

import java.util.ArrayList;

import net.tw9ine.event.listeners.AbstractListener;
import net.tw9ine.event.listeners.BlockStateListener;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BlockStateEvent extends AbstractEvent {
	private final BlockPos blockPos;
	private final BlockState blockState;
	private final BlockState previousBlockState;

	public BlockStateEvent(BlockPos blockPos, BlockState state, BlockState previousState) {
		this.blockPos = blockPos;
		blockState = state;
		previousBlockState = previousState;
	}

	public BlockPos getBlockPos() {
		return blockPos;
	}

	public BlockState getBlockState() {
		return blockState;
	}

	public BlockState getPreviousBlockState() {
		return previousBlockState;
	}

	@Override
	public void Fire(ArrayList<? extends AbstractListener> listeners) {
		for (AbstractListener listener : listeners) {
			BlockStateListener blockStateListener = (BlockStateListener) listener;
			blockStateListener.onBlockStateChanged(this);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<BlockStateListener> GetListenerClassType() {
		return BlockStateListener.class;
	}
}