package shirumengya.endless_deep_space.mixins;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.EnderLord;
import shirumengya.endless_deep_space.custom.entity.boss.enderlord.EnderLordPart;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;
import shirumengya.endless_deep_space.custom.event.SwordBlockEvent;

@Mixin({Entity.class})
public abstract class EntityMixin {
	@Shadow
	private Vec3 deltaMovement;
	
	@Shadow public boolean hasImpulse;
	
	@Shadow private float eyeHeight;
	
	public EntityMixin() {
	}
	
	/*@Inject(method = {"getBoundingBox"}, at = {@At("HEAD")}, cancellable = true)
	public void getBoundingBox(CallbackInfoReturnable<AABB> ci) {
		Entity entity = ((Entity)(Object)this);
		if (entity instanceof OceanDefender defender) {
			ci.setReturnValue(defender.size.makeBoundingBox(defender.position()));
		}
	}*/
	
	@Inject(method = {"getEyeHeight()F"}, at = {@At("HEAD")}, cancellable = true)
	public void getEyeHeight(CallbackInfoReturnable<Float> ci) {
		Entity entity = ((Entity)(Object)this);
		if (entity instanceof OceanDefender defender) {
			ci.setReturnValue(defender.size.height * 0.5F);
		}
	}

	@Inject(method = {"setRemoved"}, at = {@At("HEAD")}, cancellable = true)
	public void passRemoved(Entity.RemovalReason p_146876_, CallbackInfo ci) {
		Entity entity = ((Entity)(Object)this);
		if (!entity.level().isClientSide) {
			if (entity instanceof EnderLordPart) {
				ci.cancel();
				return;
			}
			if (entity instanceof EnderLord && p_146876_.shouldDestroy() && ((EnderLord) entity).dragonDeathTime < 799) {
				ci.cancel();
				return;
			}
		}
		
		if (entity instanceof LivingEntity livingEntity) {
			SwordBlockEvent.removeVertigoTime(livingEntity);
			OceanDefender.removeAttrition(livingEntity);
			OceanDefender.removeDying(livingEntity);
		}
	}
	
	@Inject(method = {"getDeltaMovement"}, at = {@At("HEAD")}, cancellable = true)
	public void getDeltaMovement(CallbackInfoReturnable<Vec3> ci) {
		Entity entity = ((Entity)(Object)this);
		if (entity instanceof LivingEntity livingEntity && SwordBlockEvent.hasVertigoTime(livingEntity)) {
			ci.setReturnValue(Vec3.ZERO);
		}
	}

	@Inject(method = {"tick"}, at = {@At("TAIL")})
	public void tick(CallbackInfo ci) {
		Entity entity = ((Entity)(Object)this);
		if (entity instanceof LivingEntity livingEntity && SwordBlockEvent.hasVertigoTime(livingEntity)) {
			entity.setDeltaMovement(Vec3.ZERO);
			this.deltaMovement = Vec3.ZERO;
			this.hasImpulse = true;
		}
		
		if (entity instanceof OceanDefender defender) {
			this.eyeHeight = defender.getEyeHeight();
		}
	}
}
