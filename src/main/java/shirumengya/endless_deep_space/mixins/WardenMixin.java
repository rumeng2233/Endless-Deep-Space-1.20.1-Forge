package shirumengya.endless_deep_space.mixins;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shirumengya.endless_deep_space.custom.client.event.CustomServerBossEvent;

@Mixin({Warden.class})
public abstract class WardenMixin extends Monster {
	@Unique
	private final Warden warden = ((Warden)(Object)this);
	@Unique
	private final CustomServerBossEvent bossEvent = new CustomServerBossEvent(warden, this.getDisplayName(), BossEvent.BossBarColor.BLUE, true, 4);

	public WardenMixin(EntityType<? extends Monster> p_219350_, Level p_219351_) {
		super(p_219350_, p_219351_);
	}

	@Inject(method = {"tick"}, at = {@At("HEAD")})
	public void tick(CallbackInfo ci) {
		Warden warden = ((Warden)(Object)this);
		if (!warden.level().isClientSide) {
			this.bossEvent.setProgress(warden.getHealth() / warden.getMaxHealth());
		}
	}

	@Unique
	public void startSeenByPlayer(ServerPlayer p_31483_) {
		super.startSeenByPlayer(p_31483_);
		this.bossEvent.addPlayer(p_31483_);
	}

	@Unique
	public void stopSeenByPlayer(ServerPlayer p_31488_) {
		super.stopSeenByPlayer(p_31488_);
		this.bossEvent.removePlayer(p_31488_);
	}
}
