package shirumengya.endless_deep_space.custom.entity.boss.enderlord;

import net.minecraft.world.damagesource.DamageSource;
import shirumengya.endless_deep_space.custom.client.gui.screens.components.wheel.Color;
import shirumengya.endless_deep_space.custom.entity.boss.ColoredEntityPart;

public class EnderLordPart extends ColoredEntityPart<EnderLord> {

   public EnderLordPart(EnderLord p_31014_, String p_31015_, float p_31016_, float p_31017_, Color p_31018) {
      super(p_31014_, p_31015_, p_31016_, p_31017_, p_31018);
   }
   
   public EnderLordPart(EnderLord p_31014_, String p_31015_, float p_31016_, float p_31017_) {
      super(p_31014_, p_31015_, p_31016_, p_31017_);
   }

   public boolean hurt(DamageSource p_31020_, float p_31021_) {
      return this.isInvulnerableTo(p_31020_) ? false : this.parentMob.hurt(this, p_31020_, p_31021_);
   }
}
