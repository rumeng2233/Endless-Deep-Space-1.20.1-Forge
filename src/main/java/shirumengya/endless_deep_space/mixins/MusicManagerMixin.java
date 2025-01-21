package shirumengya.endless_deep_space.mixins;

import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shirumengya.endless_deep_space.custom.config.ModClientConfig;

@Mixin({MusicManager.class})
public abstract class MusicManagerMixin {
	public MusicManagerMixin() {
		
	}

	@Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
	public void battleMusicPlayingTick(CallbackInfo ci) {
		if (ModClientConfig.BATTLE_MUSIC.get()) {
			ci.cancel();
		}
	}
}
