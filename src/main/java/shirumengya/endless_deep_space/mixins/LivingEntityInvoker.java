package shirumengya.endless_deep_space.mixins;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityInvoker {
	@Invoker("actuallyHurt")
	public void invokerActuallyHurt(DamageSource p_21240_, float p_21241_);
	
	@Accessor("DATA_HEALTH_ID")
	public EntityDataAccessor<Float> getDataHealthID();
	
	@Invoker("checkTotemDeathProtection")
	public boolean invokerCheckTotemDeathProtection(DamageSource p_21162_);
	
	@Invoker("playHurtSound")
	public void invokerPlayHurtSound(DamageSource p_21162_);
	
	@Accessor("lastHurtByPlayerTime")
	public void setLastHurtByPlayerTime(int value);
	
	@Accessor("lastHurtByPlayer")
	public void setLastHurtByPlayer(Player value);
	
	@Accessor("lastDamageSource")
	public void setLastDamageSource(DamageSource value);
	
	@Accessor("lastDamageStamp")
	public void setLastDamageStamp(long value);
}