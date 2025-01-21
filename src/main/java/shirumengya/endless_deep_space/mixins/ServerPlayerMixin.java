package shirumengya.endless_deep_space.mixins;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shirumengya.endless_deep_space.custom.networking.ModMessages;
import shirumengya.endless_deep_space.custom.networking.packet.SetSeenCreditsS2CPacket;

@Mixin({ServerPlayer.class})
public abstract class ServerPlayerMixin {

	@Shadow private boolean seenCredits;

	public ServerPlayerMixin() {
	}

	@Inject(method = {"tick"}, at = {@At("TAIL")})
	public void tick(CallbackInfo ci) {
		ServerPlayer player = ((ServerPlayer)(Object)this);
		ModMessages.sendToPlayer(new SetSeenCreditsS2CPacket(this.seenCredits), player);
	}
}
