package shirumengya.endless_deep_space.mixins;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonSittingPhase;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shirumengya.endless_deep_space.custom.entity.projectile.Arrow;

@Mixin({AbstractDragonSittingPhase.class})
public abstract class AbstractDragonSittingPhaseMixin extends AbstractDragonPhaseInstance {
	public AbstractDragonSittingPhaseMixin(EnderDragon dragon) {
        super(dragon);
    }

	@Inject(method = {"onHurt"}, at = {@At("HEAD")}, cancellable = true)
	public void onHurt(DamageSource p_31199_, float p_31200_, CallbackInfoReturnable<Float> ci) {
		if (p_31199_.getDirectEntity() instanceof AbstractArrow || p_31199_.getDirectEntity() instanceof Arrow) {
			p_31199_.getDirectEntity().setSecondsOnFire(1);
			ci.setReturnValue(0.0F);
		} else {
			ci.setReturnValue(super.onHurt(p_31199_, p_31200_));
		}
	}
}
