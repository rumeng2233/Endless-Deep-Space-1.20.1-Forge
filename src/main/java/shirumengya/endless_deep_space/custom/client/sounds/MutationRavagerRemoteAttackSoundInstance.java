package shirumengya.endless_deep_space.custom.client.sounds;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import shirumengya.endless_deep_space.custom.entity.miniboss.MutationRavager;

@OnlyIn(Dist.CLIENT)
public class MutationRavagerRemoteAttackSoundInstance extends AbstractTickableSoundInstance {
   private static final float VOLUME_MIN = 0.0F;
   private static final float VOLUME_SCALE = 1.0F;
   private static final float PITCH_MIN = 0.7F;
   private static final float PITCH_SCALE = 0.5F;
   private final MutationRavager ravager;

   public MutationRavagerRemoteAttackSoundInstance(MutationRavager p_119690_) {
      super(SoundEvents.GUARDIAN_ATTACK, SoundSource.HOSTILE, SoundInstance.createUnseededRandom());
      this.ravager = p_119690_;
      this.attenuation = SoundInstance.Attenuation.NONE;
      this.looping = true;
      this.delay = 0;
   }

   public boolean canPlaySound() {
      return !this.ravager.isSilent();
   }

   public void tick() {
      if (!this.ravager.isRemoved() && this.ravager.getTarget() == null) {
         this.x = (double)((float)this.ravager.getX());
         this.y = (double)((float)this.ravager.getY());
         this.z = (double)((float)this.ravager.getZ());
         float f = this.ravager.getAttackAnimationScale(0.0F);
         this.volume = 0.0F + 1.0F * f * f;
         this.pitch = 0.7F + 0.5F * f;
      } else {
         this.stop();
      }
   }
}