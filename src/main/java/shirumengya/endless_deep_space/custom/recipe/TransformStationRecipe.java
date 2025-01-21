package shirumengya.endless_deep_space.custom.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;
import shirumengya.endless_deep_space.custom.init.ModItems;

import java.util.Arrays;
import java.util.List;

public class TransformStationRecipe implements Recipe<SimpleContainer> {
    private final NonNullList<Ingredient> inputItems;
    private final List<ItemStack> outputItems;
    private final List<Integer> inputItemsCost;
    private final int transformSpeed;
    private final ResourceLocation id;

    public TransformStationRecipe(NonNullList<Ingredient> inputItems, List<ItemStack> outputItems, List<Integer> inputItemsCost, int transformSpeed, ResourceLocation id) {
        this.inputItems = inputItems;
        this.outputItems = outputItems;
        this.inputItemsCost = inputItemsCost;
        this.transformSpeed = transformSpeed;
        this.id = id;
    }

    @Override
    public boolean matches(SimpleContainer p_44002_, Level p_44003_) {
        if (p_44003_.isClientSide()) {
            return false;
        }

        return inputItems.get(0).test(p_44002_.getItem(0)) && (inputItems.size() <= 1 || inputItems.get(1).test(p_44002_.getItem(1)));
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModItems.TRANSFORM_STATION.get());
    }

    @Override
    public ItemStack assemble(SimpleContainer p_44001_, RegistryAccess p_267165_) {
        return outputItems.get(1);
    }

    @Override
    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess p_267052_) {
        return getResultItem(1);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return inputItems;
    }

    public ItemStack getResultItem(int item) {
        return outputItems.get(item);
    }

    public int getInputItemsCost(int item) {
        return inputItemsCost.get(item);
    }

    public int getTransformSpeed() {
        return transformSpeed == -1 ? Integer.MAX_VALUE : Math.max(1, transformSpeed);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static NonNullList<Ingredient> itemsFromJson(JsonArray p_44276_, int maxSize) {
        NonNullList<Ingredient> nonnulllist = NonNullList.create();

        for(int i = 0; i < Math.min(p_44276_.size(), maxSize); ++i) {
            Ingredient ingredient = Ingredient.fromJson(p_44276_.get(i), false);
            nonnulllist.add(ingredient);
        }

        return nonnulllist;
    }

    public static class Type implements RecipeType<TransformStationRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "transform_station";
    }

    public static class Serializer implements RecipeSerializer<TransformStationRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(EndlessDeepSpaceMod.MODID, "transform_station");

        @Override
        public TransformStationRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            ItemStack outputItemOne = pSerializedRecipe.has("output_one") ? ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "output_one")) : ItemStack.EMPTY;
            ItemStack outputItemTwo = pSerializedRecipe.has("output_two") ? ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "output_two")) : ItemStack.EMPTY;

            NonNullList<Ingredient> inputs = itemsFromJson(GsonHelper.getAsJsonArray(pSerializedRecipe, "ingredients"), 2);

            int inputCostOne = pSerializedRecipe.has("input_cost_one") ? GsonHelper.getAsInt(pSerializedRecipe, "input_cost_one") : 1;
            int inputCostTwo = pSerializedRecipe.has("input_cost_two") ? GsonHelper.getAsInt(pSerializedRecipe, "input_cost_two") : 1;

            return new TransformStationRecipe(inputs, Arrays.asList(outputItemOne, outputItemTwo), Arrays.asList(inputCostOne, inputCostTwo), pSerializedRecipe.has("transform_speed") ? GsonHelper.getAsInt(pSerializedRecipe, "transform_speed") : 10, pRecipeId);
        }

        @Override
        public @Nullable TransformStationRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(pBuffer.readInt(), Ingredient.EMPTY);

            for(int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromNetwork(pBuffer));
            }

            ItemStack outputItemOne = pBuffer.readItem();
            ItemStack outputItemTwo = pBuffer.readItem();

            return new TransformStationRecipe(inputs, Arrays.asList(outputItemOne, outputItemTwo), Arrays.asList(pBuffer.readInt(), pBuffer.readInt()), pBuffer.readInt(), pRecipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, TransformStationRecipe pRecipe) {
            pBuffer.writeInt(pRecipe.inputItems.size());

            for (Ingredient ingredient : pRecipe.getIngredients()) {
                ingredient.toNetwork(pBuffer);
            }

            pBuffer.writeItemStack(pRecipe.getResultItem(0), false);
            pBuffer.writeItemStack(pRecipe.getResultItem(1), false);

            pBuffer.writeInt(pRecipe.getInputItemsCost(0));
            pBuffer.writeInt(pRecipe.getInputItemsCost(1));

            pBuffer.writeInt(pRecipe.getTransformSpeed());
        }
    }
}
