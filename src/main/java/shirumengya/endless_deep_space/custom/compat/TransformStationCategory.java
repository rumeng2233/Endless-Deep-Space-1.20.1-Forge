package shirumengya.endless_deep_space.custom.compat;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;
import shirumengya.endless_deep_space.custom.init.ModItems;
import shirumengya.endless_deep_space.custom.recipe.TransformStationRecipe;

import java.util.ArrayList;
import java.util.Arrays;

public class TransformStationCategory implements IRecipeCategory<TransformStationRecipe> {
   public static final ResourceLocation UID = new ResourceLocation(EndlessDeepSpaceMod.MODID, "transform_station");
   public static final ResourceLocation BACKGROUND = new ResourceLocation("endless_deep_space:textures/jei/transform_station.png");
   
   public static final RecipeType<TransformStationRecipe> TRANSFORM_STATION_RECIPE_TYPE = new RecipeType<>(UID, TransformStationRecipe.class);
   
   private final IDrawable background;
   private final IDrawable icon;
   private final IDrawableAnimated animatedProgressArrow;
   
   public TransformStationCategory(IGuiHelper helper) {
      this.background = helper.createDrawable(BACKGROUND, 0, 0, 95, 59);
      this.icon = helper.createDrawableItemStack(new ItemStack(ModItems.TRANSFORM_STATION.get()));
      this.animatedProgressArrow = helper.createAnimatedDrawable(helper.createDrawable(BACKGROUND, 96, 0, 16, 16), 160, IDrawableAnimated.StartDirection.LEFT, false);
   }
   
   @Override
   public RecipeType<TransformStationRecipe> getRecipeType() {
      return TRANSFORM_STATION_RECIPE_TYPE;
   }
   
   @Override
   public Component getTitle() {
      return Component.translatable("block.endless_deep_space.transform_station");
   }
   
   @Override
   public IDrawable getBackground() {
      return this.background;
   }
   
   @Override
   public IDrawable getIcon() {
      return this.icon;
   }
   
   @Override
   public void setRecipe(IRecipeLayoutBuilder iRecipeLayoutBuilder, TransformStationRecipe transformStationRecipe, IFocusGroup iFocusGroup) {
      ItemStack[] inputOne = transformStationRecipe.getIngredients().get(0).getItems();
      ItemStack[] inputTwo = transformStationRecipe.getIngredients().size() > 1 ? transformStationRecipe.getIngredients().get(1).getItems() : new ItemStack[]{ItemStack.EMPTY};
      for (ItemStack stack : inputOne) {
         stack.setCount(transformStationRecipe.getInputItemsCost(0));
      }
      for (ItemStack stack : inputTwo) {
         stack.setCount(transformStationRecipe.getInputItemsCost(1));
      }
      
      if (inputOne.length <= 1) {
         iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT, 4, 4).addIngredients(transformStationRecipe.getIngredients().get(0));
      } else {
         iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT, 4, 4).addItemStacks(new ArrayList<>(Arrays.asList(inputOne)));
      }
      
      if (inputTwo.length <= 1 && transformStationRecipe.getIngredients().size() > 1) {
         iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT, 4, 40).addIngredients(transformStationRecipe.getIngredients().get(1));
      } else {
         iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT, 4, 40).addItemStacks(new ArrayList<>(Arrays.asList(inputTwo)));
      }
      
      iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, 76, 4).addItemStack(transformStationRecipe.getResultItem(0));
      iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, 76, 40).addItemStack(transformStationRecipe.getResultItem(1));
   }
   
   @Override
   public void draw(TransformStationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
      this.animatedProgressArrow.draw(guiGraphics, 40, 22);
      if (recipe.getTransformSpeed() != -1) {
         drawCookingTime(guiGraphics, 45, 1600 / recipe.getTransformSpeed(), this.background);
      }
   }
   
   public static void drawCookingTime(GuiGraphics guiGraphics, int y, int time, IDrawable background) {
      if (time > 0) {
         float cookTimeSeconds = time / 20.0F;
         Component timeString = Component.translatable("gui.jei.category.smelting.time.seconds", cookTimeSeconds);
         Font fontRenderer = Minecraft.getInstance().font;
         int stringWidth = fontRenderer.width(timeString);
         guiGraphics.drawString(fontRenderer, timeString, background.getWidth() - stringWidth - 21, y, 0xFF808080, false);
      }
   }
}
