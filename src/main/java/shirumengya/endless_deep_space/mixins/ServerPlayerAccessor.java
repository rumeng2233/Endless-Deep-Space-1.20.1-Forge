package shirumengya.endless_deep_space.mixins;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayer.class)
public interface ServerPlayerAccessor {
	@Accessor("seenCredits")
	public void setSeenCredits(boolean seenCredits);
}