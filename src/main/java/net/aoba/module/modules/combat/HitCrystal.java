/*
 * tw9ine - based on Aoba Hacked Client
 * Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.aoba.module.modules.combat;

import net.aoba.Aoba;
import net.aoba.event.events.TickEvent;
import net.aoba.event.listeners.TickListener;
import net.aoba.module.Category;
import net.aoba.module.Module;
import net.aoba.settings.types.BooleanSetting;
import net.aoba.settings.types.FloatSetting;
import net.aoba.utils.player.InteractionUtils;
import net.aoba.utils.player.InventoryUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Hit crystal: while the use key is held and you look at a block, places
 * obsidian, places an end crystal on it and breaks the crystal, all based on
 * where your crosshair already is (no rotations).
 */
public class HitCrystal extends Module implements TickListener {
	private final BooleanSetting requireUseKey = BooleanSetting.builder().id("hitcrystal_require_use_key")
			.displayName("Require Use Key").description("Only run while the use (right-click) key is held.")
			.defaultValue(true).build();

	private final BooleanSetting requireWeapon = BooleanSetting.builder().id("hitcrystal_require_weapon")
			.displayName("Start From Weapon")
			.description("Only start when holding a sword or axe (or already holding obsidian/crystals).")
			.defaultValue(true).build();

	private final BooleanSetting placeObsidian = BooleanSetting.builder().id("hitcrystal_place_obsidian")
			.displayName("Place Obsidian").description("Place obsidian when looking at a block that is not obsidian.")
			.defaultValue(true).build();

	private final BooleanSetting placeCrystal = BooleanSetting.builder().id("hitcrystal_place_crystal")
			.displayName("Place Crystal").description("Place an end crystal on the obsidian you are looking at.")
			.defaultValue(true).build();

	private final BooleanSetting breakCrystal = BooleanSetting.builder().id("hitcrystal_break_crystal")
			.displayName("Break Crystal").description("Attack the end crystal under your crosshair.")
			.defaultValue(true).build();

	private final FloatSetting stepDelay = FloatSetting.builder().id("hitcrystal_step_delay")
			.displayName("Step Delay").description("Ticks to wait between each step (obsidian, crystal, break).")
			.defaultValue(1f).minValue(0f).maxValue(10f).step(1f).build();

	private final BooleanSetting swapBack = BooleanSetting.builder().id("hitcrystal_swap_back")
			.displayName("Swap Back").description("Return to the item you started with when you let go.")
			.defaultValue(true).build();

	private final BooleanSetting pauseInScreens = BooleanSetting.builder().id("hitcrystal_pause_in_screens")
			.displayName("Pause In Screens").description("Do nothing while a menu or inventory is open.")
			.defaultValue(true).build();

	private int ticksSinceStep;
	private boolean active;
	private int startSlot = -1;

	public HitCrystal() {
		super("HitCrystal");

		setCategory(Category.of("Combat"));
		setDescription("Places obsidian, places a crystal and breaks it where you are looking.");

		addSetting(requireUseKey);
		addSetting(requireWeapon);
		addSetting(placeObsidian);
		addSetting(placeCrystal);
		addSetting(breakCrystal);
		addSetting(stepDelay);
		addSetting(swapBack);
		addSetting(pauseInScreens);
	}

	@Override
	public void onDisable() {
		Aoba.getInstance().eventManager.RemoveListener(TickListener.class, this);
		finish();
	}

	@Override
	public void onEnable() {
		Aoba.getInstance().eventManager.AddListener(TickListener.class, this);
		active = false;
		startSlot = -1;
		ticksSinceStep = Integer.MAX_VALUE / 2;
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onTick(TickEvent.Pre event) {
		LocalPlayer player = MC.player;
		if (player == null || MC.level == null || MC.gameMode == null)
			return;

		if (!shouldRun(player)) {
			finish();
			return;
		}

		if (!active) {
			// Only start a new sequence from a valid starting item.
			if (requireWeapon.getValue() && !isStartItem(player.getMainHandItem()))
				return;
			active = true;
			startSlot = player.getInventory().getSelectedSlot();
		}

		ticksSinceStep++;
		if (ticksSinceStep < stepDelay.getValue())
			return;

		if (doStep(player))
			ticksSinceStep = 0;
	}

	@Override
	public void onTick(TickEvent.Post event) {
	}

	private boolean shouldRun(LocalPlayer player) {
		if (!player.isAlive() || player.isSpectator())
			return false;
		if (pauseInScreens.getValue()
				&& (MC.gui.screen() != null || Aoba.getInstance().guiManager.isClickGuiOpen()))
			return false;
		if (requireUseKey.getValue() && !MC.options.keyUse.isDown())
			return false;
		return true;
	}

	/**
	 * Performs the next step of the sequence based on what the crosshair is on.
	 *
	 * @return True if an action was sent.
	 */
	private boolean doStep(LocalPlayer player) {
		HitResult hitResult = MC.hitResult;
		if (hitResult == null)
			return false;

		// Step 3: break a crystal that is under the crosshair.
		if (hitResult instanceof EntityHitResult entityHit) {
			Entity entity = entityHit.getEntity();
			if (breakCrystal.getValue() && entity instanceof EndCrystal crystal && !crystal.isRemoved()) {
				InteractionUtils.attack(crystal);
				return true;
			}
			return false;
		}

		if (!(hitResult instanceof BlockHitResult blockHit) || hitResult.getType() != HitResult.Type.BLOCK)
			return false;

		BlockPos pos = blockHit.getBlockPos();
		BlockState state = MC.level.getBlockState(pos);
		boolean isCrystalBase = state.is(Blocks.OBSIDIAN) || state.is(Blocks.BEDROCK);

		// Step 2: place a crystal on obsidian / bedrock.
		if (isCrystalBase) {
			if (!placeCrystal.getValue() || !AutoCrystal.canPlaceCrystal(pos))
				return false;
			return useItemOn(Items.END_CRYSTAL.getDefaultInstance(), blockHit);
		}

		// Step 1: place obsidian against the block we are looking at.
		if (!placeObsidian.getValue())
			return false;
		BlockPos target = pos.relative(blockHit.getDirection());
		if (!MC.level.getBlockState(target).canBeReplaced())
			return false;
		if (!MC.level.getEntities((Entity) null, new AABB(target)).isEmpty())
			return false;
		return useItemOn(Items.OBSIDIAN.getDefaultInstance(), blockHit);
	}

	/**
	 * Switches to an item (using the off-hand if it is already there) and uses it
	 * on the block under the crosshair.
	 */
	private boolean useItemOn(ItemStack wanted, BlockHitResult hit) {
		LocalPlayer player = MC.player;
		InteractionHand hand;
		if (player.getOffhandItem().is(wanted.getItem())) {
			hand = InteractionHand.OFF_HAND;
		} else {
			if (!player.getMainHandItem().is(wanted.getItem())) {
				int slot = InventoryUtils.findHotbarSlot(stack -> stack.is(wanted.getItem()));
				if (slot == -1)
					return false;
				swap(slot, false);
			}
			hand = InteractionHand.MAIN_HAND;
		}

		ItemStack held = player.getItemInHand(hand);
		SwingAnimation swingAnimation = held.getInteractAnimation();
		InteractionResult result = MC.gameMode.useItemOn(player, hand, hit);
		if (result instanceof InteractionResult.Success success
				&& success.swingSource() == InteractionResult.SwingSource.PREDICTED)
			player.swing(hand, swingAnimation, false);
		return result.consumesAction();
	}

	private void finish() {
		if (active && swapBack.getValue() && startSlot >= 0 && startSlot <= 8 && MC.player != null
				&& MC.player.getInventory().getSelectedSlot() != startSlot)
			swap(startSlot, false);
		active = false;
		startSlot = -1;
	}

	private static boolean isStartItem(ItemStack stack) {
		return stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES) || stack.is(Items.OBSIDIAN)
				|| stack.is(Items.END_CRYSTAL);
	}
}
