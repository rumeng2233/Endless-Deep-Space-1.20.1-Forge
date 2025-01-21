package shirumengya.endless_deep_space.mixins;

import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shirumengya.endless_deep_space.custom.client.renderer.ModRenderType;
import shirumengya.endless_deep_space.custom.client.renderer.entity.EnderLordRenderer;

@Mixin(RenderType.class)
public abstract class RenderTypeMixin {

	public RenderTypeMixin() {

	}

	@Inject(method = {"endGateway"}, at = {@At("HEAD")}, cancellable = true)
	private static void endGateway(CallbackInfoReturnable<RenderType> ci) {
		ci.setReturnValue(ModRenderType.EndGateway(EnderLordRenderer.END_SKY_LOCATION, EnderLordRenderer.END_PORTAL_LOCATION, true));
	}

	@Inject(method = {"endPortal"}, at = {@At("HEAD")}, cancellable = true)
	private static void endPortal(CallbackInfoReturnable<RenderType> ci) {
		ci.setReturnValue(ModRenderType.EndPortal(EnderLordRenderer.END_SKY_LOCATION, EnderLordRenderer.END_PORTAL_LOCATION, true));
	}
}
