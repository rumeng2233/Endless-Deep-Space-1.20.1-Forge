package shirumengya.endless_deep_space.custom.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import shirumengya.endless_deep_space.custom.block.GuidingStoneBlock;
import shirumengya.endless_deep_space.custom.init.ModBlockEntities;
import shirumengya.endless_deep_space.custom.init.ModItems;
import shirumengya.endless_deep_space.custom.world.data.ModWorldData;
import shirumengya.endless_deep_space.init.EndlessDeepSpaceModGameRules;

import java.util.Arrays;
import java.util.UUID;

public class GuidingStoneBlockEntity extends BlockEntity {
   public final float[] color;
   public boolean firstPlace;
   public UUID uuid;
   public static final TicketType<BlockPos> GUIDING_STONE = TicketType.create("guiding_stone", Vec3i::compareTo);
   public static final TicketType<BlockPos> GUIDING_STONE_LOAD = TicketType.create("guiding_stone_load", Vec3i::compareTo, 80);
   public static final int TICKET_TYPE_MAX_DIFFUSIBLE_VALUE = 33;
   
   public GuidingStoneBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
      super(ModBlockEntities.GUIDING_STONE.get(), p_155229_, p_155230_);
      this.color = ((GuidingStoneBlock)p_155230_.getBlock()).color;
      this.uuid = Mth.createInsecureUUID();
   }
   
   @Override
   public AABB getRenderBoundingBox() {
      return BlockEntity.INFINITE_EXTENT_AABB;
   }
   
   public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
      if (!this.firstPlace) {
         BeaconBlockEntity.playSound(pLevel, pPos, SoundEvents.BEACON_ACTIVATE);
         GuidingStoneBlock.setStaticPlacedBy(pLevel, pPos, pState, null, new ItemStack(ModItems.AQUA_GUIDING_STONE.get()));
         if (pLevel instanceof ServerLevel serverLevel) {
            ChunkPos chunkPos = serverLevel.getChunkAt(pPos).getPos();
            serverLevel.getChunkSource().addRegionTicket(GuidingStoneBlockEntity.GUIDING_STONE, chunkPos,  GuidingStoneBlockEntity.TICKET_TYPE_MAX_DIFFUSIBLE_VALUE - serverLevel.getGameRules().getInt(EndlessDeepSpaceModGameRules.GUIDING_STONE_LOAD_TICKET_LEVEL), pPos);
            ModWorldData.WorldVariables worldData = ModWorldData.WorldVariables.get(serverLevel);
            worldData.addGuidingStonePos(this.uuid, pPos);
            worldData.syncData(serverLevel);
            if (!serverLevel.isLoaded(pPos)) {
               serverLevel.getChunkAt(pPos).setLoaded(true);
            }
            serverLevel.resetEmptyTime();
         }
         this.firstPlace = true;
      }
      
      if (pLevel instanceof ServerLevel serverLevel) {
         ChunkPos chunkPos = serverLevel.getChunkAt(pPos).getPos();
         serverLevel.getChunkSource().addRegionTicket(GUIDING_STONE, chunkPos, TICKET_TYPE_MAX_DIFFUSIBLE_VALUE - serverLevel.getGameRules().getInt(EndlessDeepSpaceModGameRules.GUIDING_STONE_LOAD_TICKET_LEVEL), pPos);
         ModWorldData.WorldVariables worldData = ModWorldData.WorldVariables.get(serverLevel);
         if (worldData.getGuidingStonePos(this.uuid) == null) {
            worldData.addGuidingStonePos(this.uuid, pPos);
            worldData.syncData(serverLevel);
         }
         if (!serverLevel.isLoaded(pPos)) {
            serverLevel.getChunkAt(pPos).setLoaded(true);
         }
         serverLevel.resetEmptyTime();
      }
      
      if (pLevel.getGameTime() % 80L == 0L && !Arrays.equals(this.color, new float[]{0.0F, 0.0F, 0.0F})) {
         BeaconBlockEntity.playSound(pLevel, pPos, SoundEvents.BEACON_AMBIENT);
      }
   }
   
   @Override
   public void setRemoved() {
      BeaconBlockEntity.playSound(this.level, this.worldPosition, SoundEvents.BEACON_DEACTIVATE);
      super.setRemoved();
   }
   
   @Override
   protected void saveAdditional(CompoundTag p_187471_) {
      super.saveAdditional(p_187471_);
      p_187471_.putBoolean("FirstPlace", this.firstPlace);
      p_187471_.putUUID("UUID", this.uuid);
   }
   
   @Override
   public void load(CompoundTag p_155245_) {
      super.load(p_155245_);
      this.firstPlace = p_155245_.getBoolean("FirstPlace");
      this.uuid = p_155245_.getUUID("UUID");
   }
}
