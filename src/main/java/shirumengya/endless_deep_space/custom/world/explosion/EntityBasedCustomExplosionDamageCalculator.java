package shirumengya.endless_deep_space.custom.world.explosion;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.Optional;

public class EntityBasedCustomExplosionDamageCalculator extends CustomExplosionDamageCalculator {
   	private final Entity source;

   	public EntityBasedCustomExplosionDamageCalculator(Entity p_45894_) {
      	this.source = p_45894_;
   	}

   	public Optional<Float> getBlockExplosionResistance(CustomExplosion p_45902_, BlockGetter p_45903_, BlockPos p_45904_, BlockState p_45905_, FluidState p_45906_) {
      	return super.getBlockExplosionResistance(p_45902_, p_45903_, p_45904_, p_45905_, p_45906_).map((p_45913_) -> {
         	return this.source.getBlockExplosionResistance(p_45902_, p_45903_, p_45904_, p_45905_, p_45906_, p_45913_);
      	});
   	}

   	public boolean shouldBlockExplode(CustomExplosion p_45896_, BlockGetter p_45897_, BlockPos p_45898_, BlockState p_45899_, float p_45900_) {
      	return this.source.shouldBlockExplode(p_45896_, p_45897_, p_45898_, p_45899_, p_45900_);
   	}
}