package shirumengya.endless_deep_space.custom.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;
import shirumengya.endless_deep_space.custom.event.SwordBlockEvent;

import java.util.function.Supplier;

public class UpdateVertigoTimeS2CPacket {
   private final int entityID;
   private final boolean remove;
   private final int vertigoTime;
   
   public UpdateVertigoTimeS2CPacket(LivingEntity entity, boolean remove, int vertigoTime) {
      this.entityID = entity.getId();
      this.remove = remove;
      this.vertigoTime = vertigoTime;
   }
   
   public UpdateVertigoTimeS2CPacket(FriendlyByteBuf buf) {
      this.entityID = buf.readInt();
      this.remove = buf.readBoolean();
      this.vertigoTime = buf.readInt();
   }
   
   public void toBytes(FriendlyByteBuf buf) {
      buf.writeInt(entityID);
      buf.writeBoolean(remove);
      buf.writeInt(vertigoTime);
   }
   
   public boolean handle(Supplier<NetworkEvent.Context> supplier) {
      NetworkEvent.Context context = supplier.get();
      context.enqueueWork(() -> {
         if (Minecraft.getInstance().player.level().getEntity(entityID) instanceof LivingEntity livingEntity) {
            if (remove) {
               SwordBlockEvent.removeVertigoTime(livingEntity);
            } else {
               SwordBlockEvent.setVertigoTime(livingEntity, vertigoTime);
            }
         }
      });
      return true;
   }
}
