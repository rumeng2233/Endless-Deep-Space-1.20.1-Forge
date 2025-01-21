package shirumengya.endless_deep_space.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.WinScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shirumengya.endless_deep_space.custom.client.gui.screens.EndlessDeepSpaceCredits;

@Mixin({WinScreen.class})
public abstract class WinScreenMixin {
	@Final
	@Shadow
	private Runnable onFinished;
	@Final
	@Shadow
	private boolean poem;

	public WinScreenMixin() {
		
	}

	@Inject(method = {"tick"}, at = {@At("HEAD")})
	public void tick(CallbackInfo ci) {
		Minecraft.getInstance().setScreen(new EndlessDeepSpaceCredits(this.poem ? EndlessDeepSpaceCredits.Chapters.MINECRAFT_POEM : EndlessDeepSpaceCredits.Chapters.MINECRAFT_CREDITS, this.poem ? 0.5F : 0.75F, this.onFinished));
	}
}
