package shirumengya.endless_deep_space.mixins;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({ClientboundCustomPayloadPacket.class})
public abstract class ClientboundCustomPayloadPacketMixin {
   
   @Shadow @Final private static int MAX_PAYLOAD_SIZE;
   
   public ClientboundCustomPayloadPacketMixin() {
   }
   
   @Redirect(method = "<init>*", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writerIndex()I"))
   public int writerIndex(FriendlyByteBuf instance) {
      return MAX_PAYLOAD_SIZE - 1;
   }
}
