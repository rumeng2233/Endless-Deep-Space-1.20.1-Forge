package shirumengya.endless_deep_space.mixins;

import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ThrownTrident.class)
public interface ThrownTridentInvoker {
	@Invoker("getPickupItem")
	public ItemStack invokerGetPickupItem();
}