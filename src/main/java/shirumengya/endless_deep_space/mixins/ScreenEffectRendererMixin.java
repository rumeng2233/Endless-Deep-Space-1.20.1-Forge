package shirumengya.endless_deep_space.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shirumengya.endless_deep_space.custom.client.gui.overlay.AttritionOverlay;
import shirumengya.endless_deep_space.custom.client.gui.screens.components.wheel.Color;
import shirumengya.endless_deep_space.custom.client.renderer.entity.OceanDefenderRenderer;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;
import shirumengya.endless_deep_space.custom.event.SwordBlockEvent;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {
	@Inject(method = {"renderScreenEffect"}, at = {@At("TAIL")})
	private static void renderScreenEffect(Minecraft p_110719_, PoseStack p_110720_, CallbackInfo ci) {
		if (p_110719_.getCameraEntity() instanceof LivingEntity livingEntity) {
			if (OceanDefender.getAttrition(livingEntity) > 0.0F) {
				AttritionOverlay.renderBlock(p_110720_, OceanDefenderRenderer.ATTRITION_FIRE_1.sprite(), Color.of(1.0F, 1.0F, 1.0F, Math.max(0.1F, 1.0F - (float) OceanDefender.getAttritionTick(livingEntity) / OceanDefender.getAttritionMaxTick(livingEntity))));
			}
			
			if (SwordBlockEvent.hasVertigoTime(livingEntity)) {
				AttritionOverlay.renderBlock(p_110720_, AttritionOverlay.ICE.sprite(), Color.of(1.0F, 1.0F, 1.0F, Math.min(1.0F, (float) (SwordBlockEvent.getVertigoTime(livingEntity) == MobEffectInstance.INFINITE_DURATION ? 2.5F : ((SwordBlockEvent.getVertigoTime(livingEntity) / 20.0F)) * 0.4))));
			}
		}
	}
}