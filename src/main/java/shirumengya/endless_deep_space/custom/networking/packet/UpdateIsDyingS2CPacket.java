package shirumengya.endless_deep_space.custom.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;

import java.util.function.Supplier;

public class UpdateIsDyingS2CPacket {
   private final int entityID;
   private final boolean remove;
   private final boolean isDying;
   
   public UpdateIsDyingS2CPacket(LivingEntity entity, boolean remove, boolean isDying) {
      this.entityID = entity.getId();
      this.remove = remove;
      this.isDying = isDying;
   }
   
   public UpdateIsDyingS2CPacket(FriendlyByteBuf buf) {
      this.entityID = buf.readInt();
      this.remove = buf.readBoolean();
      this.isDying = buf.readBoolean();
   }
   
   public void toBytes(FriendlyByteBuf buf) {
      buf.writeInt(entityID);
      buf.writeBoolean(remove);
      buf.writeBoolean(isDying);
   }
   
   public boolean handle(Supplier<NetworkEvent.Context> supplier) {
      NetworkEvent.Context context = supplier.get();
      context.enqueueWork(() -> {
         if (Minecraft.getInstance().player.level().getEntity(entityID) instanceof LivingEntity livingEntity) {
            if (remove) {
               OceanDefender.removeDying(livingEntity);
            } else {
               OceanDefender.setIsDying(livingEntity, isDying);
            }
         }
      });
      return true;
   }
}
