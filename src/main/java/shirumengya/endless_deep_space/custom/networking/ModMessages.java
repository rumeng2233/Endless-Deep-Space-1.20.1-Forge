package shirumengya.endless_deep_space.custom.networking;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;
import shirumengya.endless_deep_space.custom.networking.packet.*;

public class ModMessages {
   private static SimpleChannel INSTANCE;
   
   private static int packetId = 0;
   private static int id() {
      return packetId++;
   }
   
   public static void register() {
      SimpleChannel net = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(EndlessDeepSpaceMod.MODID, "messages"))
            .networkProtocolVersion(() -> "1.0")
            .clientAcceptedVersions(s -> true)
            .serverAcceptedVersions(s -> true)
            .simpleChannel();
      
      INSTANCE = net;
      
      net.messageBuilder(UpdateBossBarS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(UpdateBossBarS2CPacket::new)
            .encoder(UpdateBossBarS2CPacket::toBytes)
            .consumerMainThread(UpdateBossBarS2CPacket::handle)
            .add();
      
      net.messageBuilder(MushroomCloudS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(MushroomCloudS2CPacket::new)
            .encoder(MushroomCloudS2CPacket::toBytes)
            .consumerMainThread(MushroomCloudS2CPacket::handle)
            .add();
      
      net.messageBuilder(DeleteEntityS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(DeleteEntityS2CPacket::new)
            .encoder(DeleteEntityS2CPacket::toBytes)
            .consumerMainThread(DeleteEntityS2CPacket::handle)
            .add();
      
      net.messageBuilder(SetRecordDelayS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(SetRecordDelayS2CPacket::new)
            .encoder(SetRecordDelayS2CPacket::toBytes)
            .consumerMainThread(SetRecordDelayS2CPacket::handle)
            .add();
      
      net.messageBuilder(SendEndlessDeepSpaceCommonToastS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(SendEndlessDeepSpaceCommonToastS2CPacket::new)
            .encoder(SendEndlessDeepSpaceCommonToastS2CPacket::toBytes)
            .consumerMainThread(SendEndlessDeepSpaceCommonToastS2CPacket::handle)
            .add();
      
      net.messageBuilder(SendEndlessDeepSpaceCommonMessageToastS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(SendEndlessDeepSpaceCommonMessageToastS2CPacket::new)
            .encoder(SendEndlessDeepSpaceCommonMessageToastS2CPacket::toBytes)
            .consumerMainThread(SendEndlessDeepSpaceCommonMessageToastS2CPacket::handle)
            .add();
      
      net.messageBuilder(SendEndlessDeepSpaceCommonRecipeTipToastS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(SendEndlessDeepSpaceCommonRecipeTipToastS2CPacket::new)
            .encoder(SendEndlessDeepSpaceCommonRecipeTipToastS2CPacket::toBytes)
            .consumerMainThread(SendEndlessDeepSpaceCommonRecipeTipToastS2CPacket::handle)
            .add();
      
      net.messageBuilder(BigJumpStringS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(BigJumpStringS2CPacket::new)
            .encoder(BigJumpStringS2CPacket::toBytes)
            .consumerMainThread(BigJumpStringS2CPacket::handle)
            .add();
      
      net.messageBuilder(JumpStringS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(JumpStringS2CPacket::new)
            .encoder(JumpStringS2CPacket::toBytes)
            .consumerMainThread(JumpStringS2CPacket::handle)
            .add();
      
      net.messageBuilder(SmallJumpStringS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(SmallJumpStringS2CPacket::new)
            .encoder(SmallJumpStringS2CPacket::toBytes)
            .consumerMainThread(SmallJumpStringS2CPacket::handle)
            .add();
      
      net.messageBuilder(SetRaidDelayS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(SetRaidDelayS2CPacket::new)
            .encoder(SetRaidDelayS2CPacket::toBytes)
            .consumerMainThread(SetRaidDelayS2CPacket::handle)
            .add();
      
      net.messageBuilder(CrashGameS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(CrashGameS2CPacket::new)
            .encoder(CrashGameS2CPacket::toBytes)
            .consumerMainThread(CrashGameS2CPacket::handle)
            .add();
      
      net.messageBuilder(SetSeenCreditsS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(SetSeenCreditsS2CPacket::new)
            .encoder(SetSeenCreditsS2CPacket::toBytes)
            .consumerMainThread(SetSeenCreditsS2CPacket::handle)
            .add();
      
      net.messageBuilder(ForceTPS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(ForceTPS2CPacket::new)
            .encoder(ForceTPS2CPacket::toBytes)
            .consumerMainThread(ForceTPS2CPacket::handle)
            .add();
      
      net.messageBuilder(UpdateAttritionS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(UpdateAttritionS2CPacket::new)
            .encoder(UpdateAttritionS2CPacket::toBytes)
            .consumerMainThread(UpdateAttritionS2CPacket::handle)
            .add();
      
      net.messageBuilder(UpdateIsDyingS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(UpdateIsDyingS2CPacket::new)
            .encoder(UpdateIsDyingS2CPacket::toBytes)
            .consumerMainThread(UpdateIsDyingS2CPacket::handle)
            .add();
      
      net.messageBuilder(UpdateVertigoTimeS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(UpdateVertigoTimeS2CPacket::new)
            .encoder(UpdateVertigoTimeS2CPacket::toBytes)
            .consumerMainThread(UpdateVertigoTimeS2CPacket::handle)
            .add();
   }
   
   public static <MSG> void sendToServer(MSG message) {
      INSTANCE.sendToServer(message);
   }
   
   public static <MSG> void sendToAllPlayers(MSG message) {
      for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
         sendToPlayer(message, player);
      }
   }
   
   public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
      INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
   }
}