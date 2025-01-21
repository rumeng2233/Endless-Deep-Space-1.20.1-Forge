package shirumengya.endless_deep_space.custom.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import shirumengya.endless_deep_space.custom.client.event.ClientEvent;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;

import java.util.UUID;
import java.util.function.Supplier;

public class UpdateAttritionS2CPacket {
   private final int entityID;
   private final boolean remove;
   private final float attrition;
   private final int attritionTick;
   
   public UpdateAttritionS2CPacket(LivingEntity entity, boolean remove, float attrition, int attritionTick) {
      this.entityID = entity.getId();
      this.remove = remove;
      this.attrition = attrition;
      this.attritionTick = attritionTick;
   }
   
   public UpdateAttritionS2CPacket(FriendlyByteBuf buf) {
      this.entityID = buf.readInt();
      this.remove = buf.readBoolean();
      this.attrition = buf.readFloat();
      this.attritionTick = buf.readInt();
   }
   
   public void toBytes(FriendlyByteBuf buf) {
      buf.writeInt(entityID);
      buf.writeBoolean(remove);
      buf.writeFloat(attrition);
      buf.writeInt(attritionTick);
   }
   
   public boolean handle(Supplier<NetworkEvent.Context> supplier) {
      NetworkEvent.Context context = supplier.get();
      context.enqueueWork(() -> {
         if (Minecraft.getInstance().player.level().getEntity(entityID) instanceof LivingEntity livingEntity) {
            if (remove) {
               OceanDefender.removeAttrition(livingEntity);
            } else {
               OceanDefender.addAttrition(livingEntity, attrition, attritionTick, false, false);
            }
         }
      });
      return true;
   }
}
