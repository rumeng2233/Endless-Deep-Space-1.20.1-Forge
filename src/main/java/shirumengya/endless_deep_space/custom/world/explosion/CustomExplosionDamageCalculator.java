package shirumengya.endless_deep_space.custom.world.explosion;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.Optional;

public class CustomExplosionDamageCalculator {
   public Optional<Float> getBlockExplosionResistance(CustomExplosion p_46099_, BlockGetter p_46100_, BlockPos p_46101_, BlockState p_46102_, FluidState p_46103_) {
      return p_46102_.isAir() && p_46103_.isEmpty() ? Optional.empty() : Optional.of(Math.max(p_46102_.getExplosionResistance(p_46100_, p_46101_, p_46099_), p_46103_.getExplosionResistance(p_46100_, p_46101_, p_46099_)));
   }

   public boolean shouldBlockExplode(CustomExplosion p_46094_, BlockGetter p_46095_, BlockPos p_46096_, BlockState p_46097_, float p_46098_) {
      return true;
   }
}