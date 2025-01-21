package shirumengya.endless_deep_space.mixins;

import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shirumengya.endless_deep_space.custom.client.gui.CustomBossBar;

import java.util.UUID;

@Mixin({LerpingBossEvent.class})
public abstract class LerpingBossEventMixin extends BossEvent {

	public LerpingBossEventMixin(UUID p_169021_, Component p_169022_, float p_169023_, BossEvent.BossBarColor p_169024_, BossEvent.BossBarOverlay p_169025_, boolean p_169026_, boolean p_169027_, boolean p_169028_) {
		super(p_169021_, p_169022_, p_169024_, p_169025_);
    }

	@Inject(method = {"setProgress"}, at = {@At("HEAD")})
	public void setProgress(float p_283175_, CallbackInfo ci) {
		if (p_283175_ < this.progress) {
			CustomBossBar.customBossBarsLastBufferTime.put(this.getId(), 40.0D);
		}
	}
}
