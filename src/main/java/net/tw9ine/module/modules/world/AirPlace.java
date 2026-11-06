package net.tw9ine.module.modules.world;

import net.tw9ine.Tw9ine;
import net.tw9ine.event.events.Render3DEvent;
import net.tw9ine.event.events.TickEvent.Post;
import net.tw9ine.event.events.TickEvent.Pre;
import net.tw9ine.event.listeners.Render3DListener;
import net.tw9ine.event.listeners.TickListener;
import net.tw9ine.gui.colors.Color;
import net.tw9ine.module.Category;
import net.tw9ine.module.Module;
import net.tw9ine.rendering.Renderer3D;
import net.tw9ine.rendering.shaders.Shader;
import net.tw9ine.settings.types.BooleanSetting;
import net.tw9ine.settings.types.FloatSetting;
import net.tw9ine.settings.types.ShaderSetting;
import net.tw9ine.utils.player.InteractionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class AirPlace extends Module implements TickListener, Render3DListener {
	private HitResult hitResult;

	private final ShaderSetting color = ShaderSetting.builder().id("airplace_color").displayName("Color")
			.description("Color").defaultValue(Shader.solid(new Color(0f, 1f, 1f, 0.65f))).build();

	private final FloatSetting radius = FloatSetting.builder().id("airplace_radius").displayName("Radius")
			.description("Distance in front of the player to place a floating block.")
			.defaultValue(5f).minValue(0f).maxValue(15f).step(1f).build();

	private final BooleanSetting allowBlocks = BooleanSetting.builder()
			.id("airplace_allow_blocks")
			.displayName("Allow Blocks?")
			.description("Whether to allow placing of blocks.")
			.defaultValue(true)
			.build();
	
	private final BooleanSetting allowSpawnEggs = BooleanSetting.builder()
			.id("airplace_allow_spawn_eegs")
			.displayName("Allow Spawn Eggs?")
			.description("Whether to allow placing of spawn eggs in the air.")
			.defaultValue(true)
			.build();
	
	public AirPlace() {
		super("AirPlace");
		setCategory(Category.of("World"));
		setDescription("Places a block at your crosshair, including floating in the air.");

		this.addSetting(radius);
		this.addSetting(allowBlocks);
		this.addSetting(allowSpawnEggs);
		this.addSetting(color);
	}

	@Override
	public void onDisable() {
		Tw9ine.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
		Tw9ine.getInstance().eventManager.RemoveListener(TickListener.class, this);
	}

	@Override
	public void onEnable() {
		Tw9ine.getInstance().eventManager.AddListener(Render3DListener.class, this);
		Tw9ine.getInstance().eventManager.AddListener(TickListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onTick(Post event) {

	}

	@Override
	public void onTick(Pre event) {
		if (MC.player == null)
			return;

		// Skip if the holder is holding a block and blocks aren't allowed.
		if (!allowBlocks.getValue() && MC.player.getMainHandItem().getItem() instanceof BlockItem) {
			hitResult = null;
			return;
		}
		
		// Skip if the holder is holding a spawn egg and spawn eggs aren't allowed.
		if(!allowSpawnEggs.getValue() && MC.player.getMainHandItem().getItem() instanceof SpawnEggItem) {
			hitResult = null;
			return;
		}

		double distance = radius.getValue().doubleValue();
		HitResult crosshair = MC.getCameraEntity().pick(distance, 0, false);

		// Skip if the player is already looking at a block.
		if (crosshair.getType() == HitResult.Type.BLOCK && crosshair instanceof BlockHitResult) {
			hitResult = null;
			return;
		}
			
		// Create our own raycast result and send it.
		Vec3 eye = MC.player.getEyePosition();
		Vec3 look = MC.player.getLookAngle();
			
		BlockPos cell = BlockPos.containing(eye.add(look.scale(distance)));
		Direction face = Direction.orderedByNearest(MC.player)[0].getOpposite();
		BlockHitResult target = new BlockHitResult(Vec3.atCenterOf(cell), face, cell, false);

		hitResult = target;

		if (MC.options.keyUse.isDown()) {
			InteractionUtils.interactBlock(target, InteractionHand.MAIN_HAND);
		}
	}

	@Override
	public void onRender(Render3DEvent event) {
		if (!(hitResult instanceof BlockHitResult blockHitResult) || 
				!MC.level.getBlockState(blockHitResult.getBlockPos()).canBeReplaced())
			return;

		Renderer3D renderer = event.getRenderer();
		BlockPos blockPos = blockHitResult.getBlockPos();
		renderer.drawBox(new AABB(blockPos), color.getValue(), 1.0f);
	}
}
