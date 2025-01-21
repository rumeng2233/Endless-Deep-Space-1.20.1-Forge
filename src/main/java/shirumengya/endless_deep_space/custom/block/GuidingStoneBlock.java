package shirumengya.endless_deep_space.custom.block;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import shirumengya.endless_deep_space.custom.block.entity.GuidingStoneBlockEntity;
import shirumengya.endless_deep_space.custom.entity.ScreenShakeEntity;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;
import shirumengya.endless_deep_space.custom.init.ModBlockEntities;
import shirumengya.endless_deep_space.custom.init.ModBlocks;
import shirumengya.endless_deep_space.custom.init.ModEntities;
import shirumengya.endless_deep_space.custom.init.ModItems;
import shirumengya.endless_deep_space.custom.world.data.ModWorldData;
import shirumengya.endless_deep_space.init.EndlessDeepSpaceModGameRules;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GuidingStoneBlock extends BaseEntityBlock {
   public final float[] color;
   
   public GuidingStoneBlock(float[] color) {
      super(BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).instrument(NoteBlockInstrument.HAT).strength(3.0F).lightLevel((p_50828_) -> {
         return !Arrays.equals(color, new float[]{0.0F, 0.0F, 0.0F}) ? 15 : 0;
      }).noOcclusion().isRedstoneConductor(ModBlocks::ever));
      this.color = color;
   }
   
   @Override
   public void setPlacedBy(Level p_49847_, BlockPos p_49848_, BlockState p_49849_, @Nullable LivingEntity p_49850_, ItemStack p_49851_) {
      GuidingStoneBlock.setStaticPlacedBy(p_49847_, p_49848_, p_49849_, p_49850_, p_49851_);
   }
   
   public static void setStaticPlacedBy(Level p_49847_, BlockPos p_49848_, BlockState p_49849_, @Nullable LivingEntity p_49850_, ItemStack p_49851_) {
      BlockEntity blockentity = p_49847_.getBlockEntity(p_49848_);
      if (blockentity instanceof GuidingStoneBlockEntity) {
         if (!p_49847_.isClientSide) {
            if (p_49847_.getBlockState(p_49848_) == ModBlocks.AQUA_GUIDING_STONE.get().defaultBlockState()) {
               if (updateBase(p_49847_, p_49848_.getX(), p_49848_.getY(), p_49848_.getZ()) >= 4) {
                  boolean flag = checkWater(p_49847_, p_49848_.getX(), p_49848_.getY(), p_49848_.getZ());
                  if (p_49850_ instanceof ServerPlayer player && player.gameMode.isCreative() || (p_49847_.getEntitiesOfClass(OceanDefender.class, new AABB(p_49848_.getCenter(), p_49848_.getCenter()).inflate(400 / 2d), e -> true).isEmpty() && p_49847_.getDifficulty() != Difficulty.PEACEFUL && flag)) {
                     OceanDefender defender = ModEntities.OCEAN_DEFENDER.get().create(p_49847_);
                     if (defender != null) {
                        ScreenShakeEntity.ScreenShake(p_49847_, p_49848_.getCenter(), 400, 0.1F, 60, 20);
                        defender.setPos(p_49848_.getCenter());
                        p_49847_.addFreshEntity(defender);
                        OceanDefender.spawnCoralDefenders(defender);
                        if (!defender.isSilent()) {
                           defender.level().globalLevelEvent(1023, defender.blockPosition(), 0);
                        }
                        for (ServerPlayer serverplayer : p_49847_.getEntitiesOfClass(ServerPlayer.class, defender.getBoundingBox().inflate(400.0D))) {
                           CriteriaTriggers.SUMMONED_ENTITY.trigger(serverplayer, defender);
                        }
                        removeBase(p_49847_, p_49848_.getX(), p_49848_.getY(), p_49848_.getZ(), flag);
                        p_49847_.destroyBlock(p_49848_, false);
                     }
                  }
               }
            }
         }
      }
   }
   
   @Override
   public boolean shouldDisplayFluidOverlay(BlockState state, BlockAndTintGetter level, BlockPos pos, FluidState fluidState) {
      return true;
   }
   
   @Override
   public float @Nullable [] getBeaconColorMultiplier(BlockState state, LevelReader level, BlockPos pos, BlockPos beaconPos) {
      return (!Arrays.equals(this.color, new float[]{0.0F, 0.0F, 0.0F}) && !Arrays.equals(this.color, new float[]{-1.0F, -1.0F, -1.0F})) ? this.color : super.getBeaconColorMultiplier(state, level, pos, beaconPos);
   }
   
   @Override
   public List<ItemStack> getDrops(BlockState p_287732_, LootParams.Builder p_287596_) {
      return Collections.singletonList(new ItemStack(this, 1));
   }
   
   @Override
   public VoxelShape getVisualShape(BlockState p_60479_, BlockGetter p_60480_, BlockPos p_60481_, CollisionContext p_60482_) {
      return Shapes.empty();
   }
   
   @Override
   public void onPlace(BlockState p_60566_, Level p_60567_, BlockPos p_60568_, BlockState p_60569_, boolean p_60570_) {
      GuidingStoneBlock.setStaticPlacedBy(p_60567_, p_60568_, p_60566_, null, new ItemStack(ModItems.AQUA_GUIDING_STONE.get()));
      super.onPlace(p_60566_, p_60567_, p_60568_, p_60569_, p_60570_);
   }
   
   @Override
   public void onRemove(BlockState p_60515_, Level p_60516_, BlockPos p_60517_, BlockState p_60518_, boolean p_60519_) {
      if (p_60516_ instanceof ServerLevel serverLevel) {
         BlockEntity blockEntity = p_60516_.getBlockEntity(p_60517_);
         if (blockEntity instanceof GuidingStoneBlockEntity guidingStoneBlockEntity) {
            ChunkPos chunkPos = serverLevel.getChunkAt(p_60517_).getPos();
            serverLevel.getChunkSource().removeRegionTicket(GuidingStoneBlockEntity.GUIDING_STONE, chunkPos, GuidingStoneBlockEntity.TICKET_TYPE_MAX_DIFFUSIBLE_VALUE - serverLevel.getGameRules().getInt(EndlessDeepSpaceModGameRules.GUIDING_STONE_LOAD_TICKET_LEVEL), p_60517_);
            ModWorldData.WorldVariables worldData = ModWorldData.WorldVariables.get(serverLevel);
            worldData.removeGuidingStonePos(guidingStoneBlockEntity.uuid);
            worldData.syncData(serverLevel);
         }
      }
      super.onRemove(p_60515_, p_60516_, p_60517_, p_60518_, p_60519_);
   }
   
   @Override
   public @Nullable BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
      return new GuidingStoneBlockEntity(p_153215_, p_153216_);
   }
   
   @Override
   public RenderShape getRenderShape(BlockState p_49232_) {
      return RenderShape.MODEL;
   }
   
   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
      return createTickerHelper(pBlockEntityType, ModBlockEntities.GUIDING_STONE.get(), (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1, pPos, pState1));
   }
   
   private static int updateBase(Level p_155093_, int p_155094_, int p_155095_, int p_155096_) {
      int i = 0;
      
      for(int j = 1; j <= 4; i = j++) {
         int k = p_155095_ - j;
         if (k < p_155093_.getMinBuildHeight()) {
            break;
         }
         
         boolean flag = true;
         
         for(int l = p_155094_ - j; l <= p_155094_ + j && flag; ++l) {
            for(int i1 = p_155096_ - j; i1 <= p_155096_ + j; ++i1) {
               if (!p_155093_.getBlockState(new BlockPos(l, k, i1)).isConduitFrame(p_155093_, new BlockPos(l, k, i1), new BlockPos(p_155094_, p_155095_, p_155096_))) {
                  flag = false;
                  break;
               }
            }
         }
         
         if (!flag) {
            break;
         }
      }
      
      return i;
   }
   
   private static boolean checkWater(Level p_155093_, int p_155094_, int p_155095_, int p_155096_) {
      boolean flag = true;
      for(int j = 1; j <= 4; j++) {
         int k = p_155095_ - j;
         if (k < p_155093_.getMinBuildHeight()) {
            return false;
         }
         for (int l = p_155094_ - 4; l <= p_155094_ + 4 && flag; ++l) {
            for (int i1 = p_155096_ - 4; i1 <= p_155096_ + 4; ++i1) {
               if (!p_155093_.isWaterAt(new BlockPos(l, k, i1)) && !p_155093_.getBlockState(new BlockPos(l, k, i1)).isConduitFrame(p_155093_, new BlockPos(l, k, i1), new BlockPos(p_155094_, p_155095_, p_155096_))) {
                  flag = false;
                  break;
               }
            }
         }
      }
      return flag;
   }
   
   private static void removeBase(Level p_155093_, int p_155094_, int p_155095_, int p_155096_, boolean water) {
      for(int j = 1; j <= 4; j++) {
         int k = p_155095_ - j;
         if (k < p_155093_.getMinBuildHeight()) {
            break;
         }
         
         boolean flag = true;
         
         for(int l = p_155094_ - j; l <= p_155094_ + j && flag; ++l) {
            for(int i1 = p_155096_ - j; i1 <= p_155096_ + j; ++i1) {
               if (!p_155093_.getBlockState(new BlockPos(l, k, i1)).isConduitFrame(p_155093_, new BlockPos(l, k, i1), new BlockPos(p_155094_, p_155095_, p_155096_))) {
                  flag = false;
                  break;
               } else {
                  p_155093_.destroyBlock(new BlockPos(l, k, i1), false);
                  if (water) {
                     p_155093_.setBlock(new BlockPos(l, k, i1), Blocks.WATER.defaultBlockState(), 3);
                  }
               }
            }
         }
         
         if (!flag) {
            break;
         }
      }
   }
}
