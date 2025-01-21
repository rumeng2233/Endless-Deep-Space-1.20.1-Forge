package shirumengya.endless_deep_space.mixins;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.ObjectUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shirumengya.endless_deep_space.custom.entity.boss.ColoredEntityPart;
import shirumengya.endless_deep_space.custom.entity.boss.PartBoss;
import shirumengya.endless_deep_space.custom.entity.miniboss.BlaznanaShulkerTrick;
import shirumengya.endless_deep_space.custom.entity.miniboss.MutationRavager;

@Mixin({SynchedEntityData.class})
public abstract class SynchedEntityDataMixin {
	@Shadow
	protected abstract <T> SynchedEntityData.DataItem<T> getItem(EntityDataAccessor<T> p_135380_);
	@Shadow
	@Final
	private Entity entity;
	
	@Shadow private boolean isDirty;
	
	public SynchedEntityDataMixin() {
		
	}

	@Inject(method = {"get"}, at = {@At("HEAD")}, cancellable = true)
	public <T> void get(EntityDataAccessor<T> p_135371_, CallbackInfoReturnable<T> ci) {
		if (entity instanceof PartBoss || entity instanceof ColoredEntityPart<?> || entity instanceof MutationRavager || entity instanceof BlaznanaShulkerTrick) {
			ci.setReturnValue(this.getItem(p_135371_).getValue());
		}
	}
	
	@Inject(method = {"set(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;)V"}, at = {@At("HEAD")}, cancellable = true)
	public <T> void set(EntityDataAccessor<T> p_135382_, T p_135383_, CallbackInfo ci) {
		if (entity instanceof PartBoss || entity instanceof ColoredEntityPart<?> || entity instanceof MutationRavager || entity instanceof BlaznanaShulkerTrick) {
			this.set(p_135382_, p_135383_, false);
			ci.cancel();
		}
	}
	
	@Unique
	public <T> void set(EntityDataAccessor<T> p_276368_, T p_276363_, boolean p_276370_) {
		SynchedEntityData.DataItem<T> dataitem = this.getItem(p_276368_);
		if (p_276370_ || ObjectUtils.notEqual(p_276363_, dataitem.getValue())) {
			dataitem.setValue(p_276363_);
			this.entity.onSyncedDataUpdated(p_276368_);
			dataitem.setDirty(true);
			this.isDirty = true;
		}
	}
}
