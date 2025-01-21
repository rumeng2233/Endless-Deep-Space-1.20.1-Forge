package shirumengya.endless_deep_space.custom.init;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;
import shirumengya.endless_deep_space.custom.recipe.TransformStationRecipe;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, EndlessDeepSpaceMod.MODID);

    public static final RegistryObject<RecipeSerializer<TransformStationRecipe>> TRANSFORM_STATION = REGISTRY.register("transform_station", () -> TransformStationRecipe.Serializer.INSTANCE);
}
