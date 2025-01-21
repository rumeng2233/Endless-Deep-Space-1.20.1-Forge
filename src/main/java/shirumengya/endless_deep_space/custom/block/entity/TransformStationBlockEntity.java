package shirumengya.endless_deep_space.custom.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import shirumengya.endless_deep_space.custom.init.ModBlockEntities;
import shirumengya.endless_deep_space.custom.menus.TransformStationMenu;
import shirumengya.endless_deep_space.custom.recipe.TransformStationRecipe;

import java.util.Optional;

public class TransformStationBlockEntity extends BlockEntity implements MenuProvider {
   private final ItemStackHandler itemHandler = new ItemStackHandler(4);
   private LazyOptional<IItemHandler> lazyItemHander = LazyOptional.empty();
   
   private static final int INPUT_SLOT_ONE = 0;
   private static final int INPUT_SLOT_TWO = 1;
   private static final int OUTPUT_SLOT_ONE = 2;
   private static final int OUTPUT_SLOT_TWO = 3;
   
   protected final ContainerData data;
   private int progress = 0;
   private int maxProgress = 1600;
   
   public TransformStationBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
      super(ModBlockEntities.TRANSFORM_STATION.get(), p_155229_, p_155230_);
      
      this.data = new ContainerData() {
         @Override
         public int get(int pIndex) {
            return switch (pIndex) {
               case 0 -> TransformStationBlockEntity.this.progress;
               case 1 -> TransformStationBlockEntity.this.maxProgress;
               default -> 0;
            };
         }
         
         @Override
         public void set(int pIndex, int pValue) {
            switch (pIndex) {
               case 0 -> TransformStationBlockEntity.this.progress = pValue;
               case 1 -> TransformStationBlockEntity.this.maxProgress = pValue;
            }
         }
         
         @Override
         public int getCount() {
            return 4;
         }
      };
   }
   
   @Override
   public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
      if (cap == ForgeCapabilities.ITEM_HANDLER) {
         return lazyItemHander.cast();
      }
      
      return super.getCapability(cap, side);
   }
   
   @Override
   public void onLoad() {
      super.onLoad();
      lazyItemHander = LazyOptional.of(() -> itemHandler);
   }
   
   @Override
   public void invalidateCaps() {
      super.invalidateCaps();
      lazyItemHander.invalidate();
   }
   
   public void drops() {
      SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
      for(int i = 0; i < itemHandler.getSlots(); i++) {
         inventory.setItem(i, itemHandler.getStackInSlot(i));
      }
      Containers.dropContents(this.level, this.worldPosition, inventory);
   }
   
   @Override
   public Component getDisplayName() {
      return Component.translatable("block.endless_deep_space.transform_station");
   }
   
   @Override
   @Nullable
   public AbstractContainerMenu createMenu(int p_39954_, Inventory p_39955_, Player p_39956_) {
      return new TransformStationMenu(p_39954_, p_39955_, this, this.data);
   }
   
   @Override
   protected void saveAdditional(CompoundTag pTag) {
      pTag.put("Inventory", itemHandler.serializeNBT());
      pTag.putInt("TransformProgress", progress);
      super.saveAdditional(pTag);
   }
   
   @Override
   public void load(CompoundTag pTag) {
      super.load(pTag);
      itemHandler.deserializeNBT(pTag.getCompound("Inventory"));
      progress = pTag.getInt("TransformProgress");
   }
   
   public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
      if (hasRecipe()) {
         increaseCraftingProgress();
         setChanged(pLevel, pPos, pState);
         if (hasProgressFinished()) {
            craftItem();
            resetProgress();
         }
      } else {
         resetProgress();
      }
   }
   
   private void craftItem() {
      Optional<TransformStationRecipe> recipe = getCurrentRecipe();
      ItemStack resultOne = recipe.get().getResultItem(0).copy();
      ItemStack resultTwo = recipe.get().getResultItem(1).copy();
      
      this.itemHandler.extractItem(INPUT_SLOT_ONE, recipe.get().getInputItemsCost(0), false);
      this.itemHandler.extractItem(INPUT_SLOT_TWO, recipe.get().getIngredients().size() > 1 ? recipe.get().getInputItemsCost(1) : 0, false);
      
      this.itemHandler.setStackInSlot(OUTPUT_SLOT_ONE, new ItemStack(resultOne.getItem(), this.itemHandler.getStackInSlot(OUTPUT_SLOT_ONE).getCount() + resultOne.getCount()));
      this.itemHandler.setStackInSlot(OUTPUT_SLOT_TWO, new ItemStack(resultTwo.getItem(), this.itemHandler.getStackInSlot(OUTPUT_SLOT_TWO).getCount() + resultTwo.getCount()));
   }
   
   private boolean hasRecipe() {
      Optional<TransformStationRecipe> recipe = getCurrentRecipe();
      
      if(recipe.isEmpty()) {
         return false;
      }
      ItemStack resultOne = recipe.get().getResultItem(0).copy();
      ItemStack resultTwo = recipe.get().getResultItem(1).copy();
      
      if (this.itemHandler.getStackInSlot(INPUT_SLOT_ONE).getCount() < recipe.get().getInputItemsCost(0) || (recipe.get().getIngredients().size() > 1 && this.itemHandler.getStackInSlot(INPUT_SLOT_TWO).getCount() < recipe.get().getInputItemsCost(1))) {
         return false;
      }
      
      return canInsertAmountIntoOutputSlot(resultOne.getCount(), resultTwo.getCount()) && canInsertItemIntoOutputSlot(resultOne.getItem(), resultTwo.getItem());
   }
   
   private Optional<TransformStationRecipe> getCurrentRecipe() {
      SimpleContainer inventory = new SimpleContainer(this.itemHandler.getSlots());
      for(int i = 0; i < itemHandler.getSlots(); i++) {
         inventory.setItem(i, this.itemHandler.getStackInSlot(i));
      }
      
      return this.level.getRecipeManager().getRecipeFor(TransformStationRecipe.Type.INSTANCE, inventory, level);
   }
   
   private boolean canInsertItemIntoOutputSlot(Item item, Item item1) {
      return (this.itemHandler.getStackInSlot(OUTPUT_SLOT_ONE).isEmpty() || this.itemHandler.getStackInSlot(OUTPUT_SLOT_ONE).is(item)) && (this.itemHandler.getStackInSlot(OUTPUT_SLOT_TWO).isEmpty() || this.itemHandler.getStackInSlot(OUTPUT_SLOT_TWO).is(item1));
   }
   
   private boolean canInsertAmountIntoOutputSlot(int count, int count1) {
      return (this.itemHandler.getStackInSlot(OUTPUT_SLOT_ONE).getCount() + count <= this.itemHandler.getStackInSlot(OUTPUT_SLOT_ONE).getMaxStackSize()) && (this.itemHandler.getStackInSlot(OUTPUT_SLOT_TWO).getCount() + count1 <= this.itemHandler.getStackInSlot(OUTPUT_SLOT_TWO).getMaxStackSize());
   }
   
   private boolean hasProgressFinished() {
      return progress >= maxProgress;
   }
   
   private void increaseCraftingProgress() {
      progress += getCurrentRecipe().get().getTransformSpeed();
   }
   
   private void resetProgress() {
      progress = 0;
   }
}
