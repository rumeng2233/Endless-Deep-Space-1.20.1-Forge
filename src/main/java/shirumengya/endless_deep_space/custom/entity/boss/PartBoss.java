package shirumengya.endless_deep_space.custom.entity.boss;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

public abstract class PartBoss extends Monster {
   public static final TicketType<BlockPos> BOSS_LOAD = TicketType.create("boss_load", Vec3i::compareTo, 80);
   
   public PartBoss(EntityType<? extends PartBoss> p_33002_, Level p_33003_) {
      super(p_33002_, p_33003_);
   }
   
   @Override
   public void setId(int p_20235_) {
      super.setId(p_20235_);
      for (int i = 0; i < this.getSubEntities().length; i++) // Forge: Fix MC-158205: Set part ids to successors of parent mob id
         this.getSubEntities()[i].setId(p_20235_ + i + 1);
   }

   public abstract ColoredEntityPart<?>[] getSubEntities();

   @Override
   public final boolean isMultipartEntity() {
       return true;
   }

   public void recreateFromPacket(ClientboundAddEntityPacket p_218825_) {
       super.recreateFromPacket(p_218825_);
       if (true) return; // Forge: Fix MC-158205: Moved into setId()
      ColoredEntityPart<?>[] aEnderLordPart = this.getSubEntities();

       for(int i = 0; i < aEnderLordPart.length; ++i) {
           aEnderLordPart[i].setId(i + p_218825_.getId());
       }

   }

   @Override
   public void checkDespawn() {
   }

   @Override
   public boolean addEffect(MobEffectInstance p_147208_, @Nullable Entity p_147209_) {
       return false;
   }
   
   @Override
   public boolean canBeAffected(MobEffectInstance p_21197_) {
      return false;
   }
   
   @Override
   public boolean canChangeDimensions() {
       return false;
   }
   
   @Override
   public boolean isPreventingPlayerRest(Player p_33036_) {
       return false;
   }

   @Override
   public final net.minecraftforge.entity.PartEntity<?>[] getParts() {
      return this.getSubEntities();
   }
   
   @Override
   public boolean startRiding(Entity p_21396_, boolean p_21397_) {
      return false;
   }
   
   @Override
   protected boolean canRide(Entity p_20339_) {
      return false;
   }
   
   @Override
   protected boolean couldAcceptPassenger() {
      return false;
   }
   
   @Override
   public boolean canBeRiddenUnderFluidType(FluidType type, Entity rider) {
      return false;
   }
   
   @Override
   public void tick() {
      super.tick();
      this.stopRiding();
      if (this.getFirstPassenger() != null) {
         this.getFirstPassenger().stopRiding();
      }
   }
}