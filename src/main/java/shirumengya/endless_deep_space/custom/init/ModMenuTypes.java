package shirumengya.endless_deep_space.custom.init;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import shirumengya.endless_deep_space.EndlessDeepSpaceMod;
import shirumengya.endless_deep_space.custom.menus.TransformStationMenu;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, EndlessDeepSpaceMod.MODID);

    public static final RegistryObject<MenuType<TransformStationMenu>> TRANSFORM_STATION = REGISTRY.register("transform_station", () -> IForgeMenuType.create(TransformStationMenu::new));
}
