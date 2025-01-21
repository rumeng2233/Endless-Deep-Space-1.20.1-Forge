package shirumengya.endless_deep_space.custom.init;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;
import shirumengya.endless_deep_space.custom.block.GuidingStoneBlock;
import shirumengya.endless_deep_space.custom.block.TransformStationBlock;

public class ModBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, EndlessDeepSpaceMod.MODID);

	public static final RegistryObject<Block> TRANSFORM_STATION = REGISTRY.register("transform_station", () -> new TransformStationBlock(BlockBehaviour.Properties.copy(Blocks.CRAFTING_TABLE)));
	public static final RegistryObject<Block> WHITE_GUIDING_STONE = REGISTRY.register("white_guiding_stone", () -> new GuidingStoneBlock(DyeColor.WHITE.getTextureDiffuseColors()));
	public static final RegistryObject<Block> ORANGE_GUIDING_STONE = REGISTRY.register("orange_guiding_stone", () -> new GuidingStoneBlock(DyeColor.ORANGE.getTextureDiffuseColors()));
	public static final RegistryObject<Block> MAGENTA_GUIDING_STONE = REGISTRY.register("magenta_guiding_stone", () -> new GuidingStoneBlock(DyeColor.MAGENTA.getTextureDiffuseColors()));
	public static final RegistryObject<Block> LIGHT_BLUE_GUIDING_STONE = REGISTRY.register("light_blue_guiding_stone", () -> new GuidingStoneBlock(DyeColor.LIGHT_BLUE.getTextureDiffuseColors()));
	public static final RegistryObject<Block> YELLOW_GUIDING_STONE = REGISTRY.register("yellow_guiding_stone", () -> new GuidingStoneBlock(DyeColor.YELLOW.getTextureDiffuseColors()));
	public static final RegistryObject<Block> LIME_GUIDING_STONE = REGISTRY.register("lime_guiding_stone", () -> new GuidingStoneBlock(DyeColor.LIME.getTextureDiffuseColors()));
	public static final RegistryObject<Block> PINK_GUIDING_STONE = REGISTRY.register("pink_guiding_stone", () -> new GuidingStoneBlock(DyeColor.PINK.getTextureDiffuseColors()));
	public static final RegistryObject<Block> GRAY_GUIDING_STONE = REGISTRY.register("gray_guiding_stone", () -> new GuidingStoneBlock(DyeColor.GRAY.getTextureDiffuseColors()));
	public static final RegistryObject<Block> LIGHT_GRAY_GUIDING_STONE = REGISTRY.register("light_gray_guiding_stone", () -> new GuidingStoneBlock(DyeColor.LIGHT_GRAY.getTextureDiffuseColors()));
	public static final RegistryObject<Block> CYAN_GUIDING_STONE = REGISTRY.register("cyan_guiding_stone", () -> new GuidingStoneBlock(DyeColor.CYAN.getTextureDiffuseColors()));
	public static final RegistryObject<Block> PURPLE_GUIDING_STONE = REGISTRY.register("purple_guiding_stone", () -> new GuidingStoneBlock(DyeColor.PURPLE.getTextureDiffuseColors()));
	public static final RegistryObject<Block> BLUE_GUIDING_STONE = REGISTRY.register("blue_guiding_stone", () -> new GuidingStoneBlock(DyeColor.BLUE.getTextureDiffuseColors()));
	public static final RegistryObject<Block> BROWN_GUIDING_STONE = REGISTRY.register("brown_guiding_stone", () -> new GuidingStoneBlock(DyeColor.BROWN.getTextureDiffuseColors()));
	public static final RegistryObject<Block> GREEN_GUIDING_STONE = REGISTRY.register("green_guiding_stone", () -> new GuidingStoneBlock(DyeColor.GREEN.getTextureDiffuseColors()));
	public static final RegistryObject<Block> RED_GUIDING_STONE = REGISTRY.register("red_guiding_stone", () -> new GuidingStoneBlock(DyeColor.RED.getTextureDiffuseColors()));
	public static final RegistryObject<Block> BLACK_GUIDING_STONE = REGISTRY.register("black_guiding_stone", () -> new GuidingStoneBlock(DyeColor.BLACK.getTextureDiffuseColors()));
	public static final RegistryObject<Block> AQUA_GUIDING_STONE = REGISTRY.register("aqua_guiding_stone", () -> new GuidingStoneBlock(new float[]{0.333333333333F, 1.0F, 1.0F}));
	public static final RegistryObject<Block> PURE_BLACK_GUIDING_STONE = REGISTRY.register("pure_black_guiding_stone", () -> new GuidingStoneBlock(new float[]{0.0F, 0.0F, 0.0F}));
	public static final RegistryObject<Block> SKY_BLUE_GUIDING_STONE = REGISTRY.register("sky_blue_guiding_stone", () -> new GuidingStoneBlock(new float[]{0.4F, 0.8F, 1.0F}));
	public static final RegistryObject<Block> BLUE_GREEN_GUIDING_STONE = REGISTRY.register("blue_green_guiding_stone", () -> new GuidingStoneBlock(new float[]{0.223529411765F, 0.772549019608F, 0.733333333333F}));
	public static final RegistryObject<Block> WARNING_GUIDING_STONE = REGISTRY.register("warning_guiding_stone", () -> new GuidingStoneBlock(new float[]{-1.0F, -1.0F, -1.0F}));
	public static final RegistryObject<Block> PURE_RED_GUIDING_STONE = REGISTRY.register("pure_red_guiding_stone", () -> new GuidingStoneBlock(new float[]{1.0F, 0.0F, 0.0F}));
	public static final RegistryObject<Block> PURE_GREEN_GUIDING_STONE = REGISTRY.register("pure_green_guiding_stone", () -> new GuidingStoneBlock(new float[]{0.0F, 1.0F, 0.0F}));
	public static final RegistryObject<Block> PURE_BLUE_GUIDING_STONE = REGISTRY.register("pure_blue_guiding_stone", () -> new GuidingStoneBlock(new float[]{0.0F, 0.0F, 1.0F}));
	public static final RegistryObject<Block> PURE_MAGENTA_GUIDING_STONE = REGISTRY.register("pure_magenta_guiding_stone", () -> new GuidingStoneBlock(new float[]{1.0F, 0.0F, 1.0F}));
	
	
	
	public static boolean never(BlockState p_50806_, BlockGetter p_50807_, BlockPos p_50808_) {
		return false;
	}
	
	public static boolean ever(BlockState p_50806_, BlockGetter p_50807_, BlockPos p_50808_) {
		return true;
	}
}
