package shirumengya.endless_deep_space.mixins;

import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({SoundManager.class})
public abstract class SoundManagerMixin {
	@Final
	@Shadow
	private SoundEngine soundEngine;

	public SoundManagerMixin() {
		
	}

	@ModifyVariable(method = "tick", at = @At("HEAD"), argsOnly = true)
	public boolean battleMusicPlayingTick(boolean pause) {
		this.soundEngine.tick(false);
		return false;
	}
}
