package shirumengya.endless_deep_space.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shirumengya.endless_deep_space.custom.client.renderer.entity.EnderLordRenderer;
import shirumengya.endless_deep_space.custom.config.ModClientConfig;

import javax.annotation.Nullable;
import java.awt.*;

@Mixin({LevelRenderer.class})
public abstract class LevelRendererMixin {
   @Shadow
   private static void renderShape(PoseStack p_109783_, VertexConsumer p_109784_, VoxelShape p_109785_, double p_109786_, double p_109787_, double p_109788_, float p_109789_, float p_109790_, float p_109791_, float p_109792_) {
   }
   
   @Shadow @Nullable private ClientLevel level;
	
	@Inject(method = {"renderHitOutline"}, at = {@At("HEAD")}, cancellable = true)
	private void renderHitOutline(PoseStack p_109638_, VertexConsumer p_109639_, Entity p_109640_, double p_109641_, double p_109642_, double p_109643_, BlockPos p_109644_, BlockState p_109645_, CallbackInfo ci) {
		if (ModClientConfig.CUSTOM_HIT_OUTLINE_RENDER__ENABLE.get()) {
			java.awt.Color color = EnderLordRenderer.rainbow(ModClientConfig.CUSTOM_HIT_OUTLINE_RENDER__ANIMATION_COLOR_SETTINGS__CYCLE_SPEED.get(), ModClientConfig.CUSTOM_HIT_OUTLINE_RENDER__ANIMATION_COLOR_SETTINGS__SATURATION.get() / 100.0F, ModClientConfig.CUSTOM_HIT_OUTLINE_RENDER__ANIMATION_COLOR_SETTINGS__LIGHT.get() / 100.0F);
			if (!ModClientConfig.CUSTOM_HIT_OUTLINE_RENDER__ANIMATION_COLOR.get()) {
				color = new Color(ModClientConfig.CUSTOM_HIT_OUTLINE_RENDER__COLOR_RED.get(), ModClientConfig.CUSTOM_HIT_OUTLINE_RENDER__COLOR_GREEN.get(), ModClientConfig.CUSTOM_HIT_OUTLINE_RENDER__COLOR_BLUE.get());
			}
			renderShape(p_109638_, p_109639_, p_109645_.getShape(this.level, p_109644_, CollisionContext.of(p_109640_)), (double) p_109644_.getX() - p_109641_, (double) p_109644_.getY() - p_109642_, (double) p_109644_.getZ() - p_109643_, color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, ModClientConfig.CUSTOM_HIT_OUTLINE_RENDER__COLOR_ALPHA.get() / 255.0F);
			ci.cancel();
		}
	}
}