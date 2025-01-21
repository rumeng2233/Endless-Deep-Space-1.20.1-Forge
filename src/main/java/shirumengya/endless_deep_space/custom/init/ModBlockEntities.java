package shirumengya.endless_deep_space.custom.init;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;
import shirumengya.endless_deep_space.custom.block.entity.GuidingStoneBlockEntity;
import shirumengya.endless_deep_space.custom.block.entity.TransformStationBlockEntity;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, EndlessDeepSpaceMod.MODID);

    public static final RegistryObject<BlockEntityType<TransformStationBlockEntity>> TRANSFORM_STATION = REGISTRY.register("transform_station", () -> BlockEntityType.Builder.of(TransformStationBlockEntity::new, ModBlocks.TRANSFORM_STATION.get()).build(null));
    public static final RegistryObject<BlockEntityType<GuidingStoneBlockEntity>> GUIDING_STONE = REGISTRY.register("guiding_stone", () -> BlockEntityType.Builder.of(GuidingStoneBlockEntity::new, ModBlocks.WHITE_GUIDING_STONE.get(), ModBlocks.ORANGE_GUIDING_STONE.get(), ModBlocks.MAGENTA_GUIDING_STONE.get(), ModBlocks.BLACK_GUIDING_STONE.get(), ModBlocks.BLUE_GUIDING_STONE.get(), ModBlocks.CYAN_GUIDING_STONE.get(), ModBlocks.BROWN_GUIDING_STONE.get(), ModBlocks.GRAY_GUIDING_STONE.get(), ModBlocks.GREEN_GUIDING_STONE.get(), ModBlocks.LIME_GUIDING_STONE.get(), ModBlocks.PINK_GUIDING_STONE.get(), ModBlocks.RED_GUIDING_STONE.get(), ModBlocks.LIGHT_BLUE_GUIDING_STONE.get(), ModBlocks.YELLOW_GUIDING_STONE.get(), ModBlocks.LIGHT_GRAY_GUIDING_STONE.get(), ModBlocks.PURPLE_GUIDING_STONE.get(), ModBlocks.AQUA_GUIDING_STONE.get(), ModBlocks.PURE_BLACK_GUIDING_STONE.get(), ModBlocks.SKY_BLUE_GUIDING_STONE.get(), ModBlocks.BLUE_GREEN_GUIDING_STONE.get(), ModBlocks.WARNING_GUIDING_STONE.get(), ModBlocks.PURE_GREEN_GUIDING_STONE.get(), ModBlocks.PURE_BLUE_GUIDING_STONE.get(), ModBlocks.PURE_RED_GUIDING_STONE.get(), ModBlocks.PURE_MAGENTA_GUIDING_STONE.get()).build(null));
}