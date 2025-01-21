package shirumengya.endless_deep_space.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;

@Mixin(EndDragonFight.class)
public interface EndDragonFightInvoker {
	@Accessor("portalLocation")
	@Nullable
	public BlockPos getPortalLocation();
}