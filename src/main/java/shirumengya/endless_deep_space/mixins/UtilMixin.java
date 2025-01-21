package shirumengya.endless_deep_space.mixins;

import net.minecraft.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Util.class})
public abstract class UtilMixin {
	public UtilMixin() {
		
	}

	@Inject(method = "getMillis", at = @At("HEAD"), cancellable = true)
	private static void getMillis(CallbackInfoReturnable<Long> ci) {
		ci.setReturnValue(Util.getNanos() / 1000000L);
	}
}
