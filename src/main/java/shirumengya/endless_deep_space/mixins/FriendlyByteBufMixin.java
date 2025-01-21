package shirumengya.endless_deep_space.mixins;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin({FriendlyByteBuf.class})
public abstract class FriendlyByteBufMixin {
   
   @Shadow @Nullable public abstract CompoundTag readAnySizeNbt();
   
   public FriendlyByteBufMixin() {
   }
   
   @Inject(method = {"readNbt()Lnet/minecraft/nbt/CompoundTag;"}, at = {@At("HEAD")}, cancellable = true)
   public void readNbt(CallbackInfoReturnable<CompoundTag> ci) {
      
      ci.setReturnValue(this.readAnySizeNbt());
   }
}
