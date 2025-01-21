package shirumengya.endless_deep_space.custom.entity.boss.oceandefenders;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import shirumengya.endless_deep_space.custom.client.gui.screens.components.wheel.Color;
import shirumengya.endless_deep_space.custom.entity.boss.ColoredEntityPart;

public class OceanDefenderPart extends ColoredEntityPart<OceanDefender> {
   public OceanDefenderPart(OceanDefender p_31014_, String p_31015_, float p_31016_, float p_31017_, Color p_31018) {
      super(p_31014_, p_31015_, p_31016_, p_31017_, p_31018);
      this.setSize(p_31016_, p_31017_);
   }
   
   public OceanDefenderPart(OceanDefender p_31014_, String p_31015_, float p_31016_, float p_31017_) {
      super(p_31014_, p_31015_, p_31016_, p_31017_);
      this.setSize(p_31016_, p_31017_);
   }
   
   public void setSize(float p_31016_, float p_31017_) {
      this.size = EntityDimensions.scalable(p_31016_, p_31017_);
      this.refreshDimensions();
   }
   
   public boolean hurt(DamageSource p_31020_, float p_31021_) {
      return !this.isInvulnerableTo(p_31020_) && this.parentMob.hurt(this, p_31020_, p_31021_);
   }
}
