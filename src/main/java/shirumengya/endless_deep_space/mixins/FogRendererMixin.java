package shirumengya.endless_deep_space.mixins;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shirumengya.endless_deep_space.network.EndlessDeepSpaceModVariables;

@Mixin({FogRenderer.class})
public abstract class FogRendererMixin {
	@Inject(method = {"setupFog"}, at = {@At("TAIL")})
	private static void setupFog(Camera p_234173_, FogRenderer.FogMode p_234174_, float p_234175_, boolean p_234176_, float p_234177_, CallbackInfo ci) {
		if ((Minecraft.getInstance().player.getCapability(EndlessDeepSpaceModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new EndlessDeepSpaceModVariables.PlayerVariables())).NoFog) {
			FogRenderer.setupNoFog();
		}
	}
}