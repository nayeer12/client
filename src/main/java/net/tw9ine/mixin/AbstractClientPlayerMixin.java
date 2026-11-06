package net.tw9ine.mixin;

import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends PlayerMixin {

}
