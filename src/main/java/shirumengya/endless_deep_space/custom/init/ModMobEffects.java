package shirumengya.endless_deep_space.custom.init;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;
import shirumengya.endless_deep_space.custom.effect.AttributeEffect;

public class ModMobEffects {
	public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, EndlessDeepSpaceMod.MODID);
	public static final RegistryObject<MobEffect> DAMAGE_REDUCTION = REGISTRY.register("damage_reduction", () -> new AttributeEffect(MobEffectCategory.BENEFICIAL, 8555163).addAttributeModifier(ModAttributes.DAMAGE_REDUCTION.get(), "2e32ba78-6bb9-3340-1bc0-7df50ac9eb5f", 2, AttributeModifier.Operation.ADDITION));
	public static final RegistryObject<MobEffect> DAMAGE_INCREASE = REGISTRY.register("damage_increase", () -> new AttributeEffect(MobEffectCategory.HARMFUL, 8222052).addAttributeModifier(ModAttributes.DAMAGE_REDUCTION.get(), "fbf26124-194c-0ae7-5a56-d0ed769424ec", -2, AttributeModifier.Operation.ADDITION));
	public static final RegistryObject<MobEffect> SWIMMING_ACCELERATION = REGISTRY.register("swimming_acceleration", () -> new AttributeEffect(MobEffectCategory.BENEFICIAL, -7822402).addAttributeModifier(ForgeMod.SWIM_SPEED.get(), "8088d34a-3f89-64b4-371c-7cd201052517", 0.2, AttributeModifier.Operation.ADDITION));
}