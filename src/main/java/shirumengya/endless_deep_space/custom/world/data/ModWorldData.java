package shirumengya.endless_deep_space.custom.world.data;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;
import shirumengya.endless_deep_space.custom.block.entity.GuidingStoneBlockEntity;
import shirumengya.endless_deep_space.custom.entity.boss.PartBoss;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;
import shirumengya.endless_deep_space.custom.event.SwordBlockEvent;
import shirumengya.endless_deep_space.custom.networking.ModMessages;
import shirumengya.endless_deep_space.custom.networking.packet.UpdateAttritionS2CPacket;
import shirumengya.endless_deep_space.custom.networking.packet.UpdateIsDyingS2CPacket;
import shirumengya.endless_deep_space.custom.networking.packet.UpdateVertigoTimeS2CPacket;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModWorldData {
   public static final Logger LOGGER = LogUtils.getLogger();
   
   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void init(FMLCommonSetupEvent event) {
      EndlessDeepSpaceMod.addNetworkMessage(ModWorldData.SavedDataSyncMessage.class, ModWorldData.SavedDataSyncMessage::buffer, ModWorldData.SavedDataSyncMessage::new, ModWorldData.SavedDataSyncMessage::handler);
   }
   
   @Mod.EventBusSubscriber
   public static class EventBusVariableHandlers {
      @SubscribeEvent(priority = EventPriority.HIGHEST)
      public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
         if (!event.getEntity().level().isClientSide()) {
            SavedData mapdata = ModWorldData.MapVariables.get(event.getEntity().level());
            SavedData worlddata = ModWorldData.WorldVariables.get(event.getEntity().level());
            if (mapdata != null) {
               EndlessDeepSpaceMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getEntity()), new ModWorldData.SavedDataSyncMessage(0, mapdata));
            }
            if (worlddata != null) {
               EndlessDeepSpaceMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getEntity()), new ModWorldData.SavedDataSyncMessage(1, worlddata));
            }
            ModMessages.sendToAllPlayers(new UpdateAttritionS2CPacket(event.getEntity(), false, OceanDefender.getAttrition(event.getEntity()), OceanDefender.getAttritionTick(event.getEntity())));
            ModMessages.sendToAllPlayers(new UpdateIsDyingS2CPacket(event.getEntity(), false, OceanDefender.isDying(event.getEntity())));
            ModMessages.sendToAllPlayers(new UpdateVertigoTimeS2CPacket(event.getEntity(), false, SwordBlockEvent.getVertigoTime(event.getEntity())));
         }
      }
      
      @SubscribeEvent(priority = EventPriority.HIGHEST)
      public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
         if (!event.getEntity().level().isClientSide()) {
            SavedData worlddata = ModWorldData.WorldVariables.get(event.getEntity().level());
            if (worlddata != null) {
               EndlessDeepSpaceMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getEntity()), new ModWorldData.SavedDataSyncMessage(1, worlddata));
            }
         }
      }
      
      @SubscribeEvent(priority = EventPriority.HIGHEST)
      public static void onWorldLoad(LevelEvent.Load event) {
         if (event.getLevel() instanceof ServerLevel serverLevel) {
            ModWorldData.WorldVariables worldData = ModWorldData.WorldVariables.get(serverLevel);
            Iterator<Map.Entry<UUID, BlockPos>> guidingStonePosIterator = worldData.guidingStonePos.entrySet().iterator();
            LOGGER.info("Start putting Guiding Stone force load ticket in {}.", serverLevel.dimension().location());
            while (guidingStonePosIterator.hasNext()) {
               Map.Entry<UUID, BlockPos> entry = guidingStonePosIterator.next();
               if (serverLevel.getBlockEntity(entry.getValue()) instanceof GuidingStoneBlockEntity) {
                  ChunkPos chunkPos = serverLevel.getChunkAt(entry.getValue()).getPos();
                  serverLevel.getChunkSource().addRegionTicket(GuidingStoneBlockEntity.GUIDING_STONE_LOAD, chunkPos, 2, entry.getValue());
                  if (!serverLevel.isLoaded(entry.getValue())) {
                     serverLevel.getChunkAt(entry.getValue()).setLoaded(true);
                  }
                  serverLevel.resetEmptyTime();
                  LOGGER.info("Putting Guiding Stone force load ticket in {}: UUID {}, {}, ChunkPos {}.", serverLevel.dimension().location(), entry.getKey(), entry.getValue(), chunkPos);
               } else {
                  guidingStonePosIterator.remove();
                  LOGGER.error("Putting Guiding Stone force load ticket in {} error: UUID {}, {}, remove it.", serverLevel.dimension().location(), entry.getKey(), entry.getValue());
               }
            }
            worldData.syncData(serverLevel);
            LOGGER.info("Stop putting Guiding Stone force load ticket in {}.", serverLevel.dimension().location());
            
            Iterator<Map.Entry<UUID, BlockPos>> bossPosIterator = worldData.bossPos.entrySet().iterator();
            LOGGER.info("Start putting Boss force load ticket in {}.", serverLevel.dimension().location());
            while (bossPosIterator.hasNext()) {
               Map.Entry<UUID, BlockPos> entry = bossPosIterator.next();
               ChunkPos chunkPos = serverLevel.getChunkAt(entry.getValue()).getPos();
               serverLevel.getChunkSource().addRegionTicket(PartBoss.BOSS_LOAD, chunkPos, 2, entry.getValue());
               if (!serverLevel.isLoaded(entry.getValue())) {
                  serverLevel.getChunkAt(entry.getValue()).setLoaded(true);
               }
               serverLevel.resetEmptyTime();
               LOGGER.info("Putting Boss force load ticket in {}: UUID {}, {}, ChunkPos {}.", serverLevel.dimension().location(), entry.getKey(), entry.getValue(), chunkPos);
            }
            worldData.syncData(serverLevel);
            LOGGER.info("Stop putting Boss force load ticket in {}.", serverLevel.dimension().location());
         }
      }
      
      @SubscribeEvent(priority = EventPriority.HIGHEST)
      public static void onWorldTick(TickEvent.LevelTickEvent event) {
         if (event.phase == TickEvent.Phase.END) {
            if (event.level instanceof ServerLevel level) {
               level.getProfiler().push("EndlessDeepSpaceModServerSideTick");
               
               for (ServerLevel serverLevel : level.getServer().getAllLevels()) {
                  ModWorldData.WorldVariables worldData = ModWorldData.WorldVariables.get(serverLevel);
                  
                  level.getProfiler().push("BossPosCheckIn" + serverLevel.dimension().location());
                  Iterator<Map.Entry<UUID, BlockPos>> bossPosIterator = worldData.bossPos.entrySet().iterator();
                  while (bossPosIterator.hasNext()) {
                     Map.Entry<UUID, BlockPos> entry = bossPosIterator.next();
                     if (serverLevel.getEntity(entry.getKey()) == null) {
                        bossPosIterator.remove();
                        LOGGER.error("Putting Boss force load ticket in {} error: UUID {}, {}, remove it.", serverLevel.dimension().location(), entry.getKey(), entry.getValue());
                     }
                  }
                  level.getProfiler().pop();
                  
               }
               
               level.getProfiler().pop();
            }
         }
      }
   }
   
   public static class WorldVariables extends SavedData {
      public static final String DATA_NAME = "endless_deep_space_world_data";
      public final Map<UUID, BlockPos> guidingStonePos = new HashMap<>();
      public final Map<UUID, BlockPos> bossPos = new HashMap<>();
      
      public static ModWorldData.WorldVariables load(CompoundTag tag) {
         ModWorldData.WorldVariables data = new ModWorldData.WorldVariables();
         data.read(tag);
         return data;
      }
      
      @Nullable
      public BlockPos getGuidingStonePos(UUID uuid) {
         return this.guidingStonePos.getOrDefault(uuid, null);
      }
      
      public void removeGuidingStonePos(UUID uuid) {
         this.guidingStonePos.remove(uuid);
      }
      
      public void addGuidingStonePos(UUID uuid, BlockPos pos) {
         this.guidingStonePos.put(uuid, pos);
      }
      
      @Nullable
      public BlockPos getBossPos(UUID uuid) {
         return this.bossPos.getOrDefault(uuid, null);
      }
      
      public void removeBossPos(UUID uuid) {
         this.bossPos.remove(uuid);
      }
      
      public void addBossPos(UUID uuid, BlockPos pos) {
         this.bossPos.put(uuid, pos);
      }
      
      public void read(CompoundTag nbt) {
         ListTag guidingStonePosList = nbt.getList("GuidingStonePos", 10);
         for(int i = 0; i < guidingStonePosList.size(); i++) {
            CompoundTag tag = guidingStonePosList.getCompound(i);
            this.addGuidingStonePos(tag.getUUID("UUID"), NbtUtils.readBlockPos(tag.getCompound("BlockPos")));
         }
         
         ListTag bossList = nbt.getList("BossPos", 10);
         for(int i = 0; i < bossList.size(); i++) {
            CompoundTag tag = bossList.getCompound(i);
            this.addBossPos(tag.getUUID("UUID"), NbtUtils.readBlockPos(tag.getCompound("BlockPos")));
         }
      }
      
      @Override
      public CompoundTag save(CompoundTag nbt) {
         if (!this.guidingStonePos.isEmpty()) {
            ListTag guidingStonePosList = new ListTag();
            this.guidingStonePos.forEach(((uuid, pos) -> {
               CompoundTag tag = new CompoundTag();
               tag.putUUID("UUID", uuid);
               tag.put("BlockPos", NbtUtils.writeBlockPos(pos));
               guidingStonePosList.add(tag);
            }));
            nbt.put("GuidingStonePos", guidingStonePosList);
         }
         
         if (!this.bossPos.isEmpty()) {
            ListTag bossList = new ListTag();
            this.bossPos.forEach(((uuid, pos) -> {
               CompoundTag tag = new CompoundTag();
               tag.putUUID("UUID", uuid);
               tag.put("BlockPos", NbtUtils.writeBlockPos(pos));
               bossList.add(tag);
            }));
            nbt.put("BossPos", bossList);
         }
         
         return nbt;
      }
      
      public void syncData(LevelAccessor world) {
         this.setDirty();
         if (world instanceof Level level && !level.isClientSide()) {
            EndlessDeepSpaceMod.PACKET_HANDLER.send(PacketDistributor.DIMENSION.with(level::dimension), new ModWorldData.SavedDataSyncMessage(1, this));
         }
      }
      
      static ModWorldData.WorldVariables clientSide = new ModWorldData.WorldVariables();
      
      public static ModWorldData.WorldVariables get(LevelAccessor world) {
         if (world instanceof ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(WorldVariables::load, ModWorldData.WorldVariables::new, DATA_NAME);
         } else {
            return clientSide;
         }
      }
   }
   
   public static class MapVariables extends SavedData {
      public static final String DATA_NAME = "endless_deep_space_map_data";
      
      public static ModWorldData.MapVariables load(CompoundTag tag) {
         ModWorldData.MapVariables data = new ModWorldData.MapVariables();
         data.read(tag);
         return data;
      }
      
      public void read(CompoundTag nbt) {
      }
      
      @Override
      public CompoundTag save(CompoundTag nbt) {
         return nbt;
      }
      
      public void syncData(LevelAccessor world) {
         this.setDirty();
         if (world instanceof Level && !world.isClientSide()) {
            EndlessDeepSpaceMod.PACKET_HANDLER.send(PacketDistributor.ALL.noArg(), new ModWorldData.SavedDataSyncMessage(0, this));
         }
      }
      
      static ModWorldData.MapVariables clientSide = new ModWorldData.MapVariables();
      
      public static ModWorldData.MapVariables get(LevelAccessor world) {
         if (world instanceof ServerLevelAccessor serverLevelAcc) {
            return serverLevelAcc.getLevel().getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(MapVariables::load, ModWorldData.MapVariables::new, DATA_NAME);
         } else {
            return clientSide;
         }
      }
   }
   
   public static class SavedDataSyncMessage {
      private final int type;
      private SavedData data;
      
      public SavedDataSyncMessage(FriendlyByteBuf buffer) {
         this.type = buffer.readInt();
         CompoundTag nbt = buffer.readNbt();
         if (nbt != null) {
            this.data = this.type == 0 ? new ModWorldData.MapVariables() : new ModWorldData.WorldVariables();
            if (this.data instanceof ModWorldData.MapVariables mapVariables)
               mapVariables.read(nbt);
            else if (this.data instanceof ModWorldData.WorldVariables worldVariables)
               worldVariables.read(nbt);
         }
      }
      
      public SavedDataSyncMessage(int type, SavedData data) {
         this.type = type;
         this.data = data;
      }
      
      public static void buffer(ModWorldData.SavedDataSyncMessage message, FriendlyByteBuf buffer) {
         buffer.writeInt(message.type);
         if (message.data != null) {
            buffer.writeNbt(message.data.save(new CompoundTag()));
         }
      }
      
      public static void handler(ModWorldData.SavedDataSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
         NetworkEvent.Context context = contextSupplier.get();
         context.enqueueWork(() -> {
            if (!context.getDirection().getReceptionSide().isServer() && message.data != null) {
               if (message.type == 0)
                  ModWorldData.MapVariables.clientSide = (ModWorldData.MapVariables) message.data;
               else
                  ModWorldData.WorldVariables.clientSide = (ModWorldData.WorldVariables) message.data;
            }
         });
         context.setPacketHandled(true);
      }
   }
}
