package shirumengya.endless_deep_space.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
	@Accessor("blockPosition")
	public void setBlockPosition(BlockPos pos);

	@Accessor("chunkPosition")
	public void setChunkPosition(ChunkPos pos);

	@Accessor("deltaMovement")
	public void setDeltaMovement(Vec3 vec3);

	@Accessor("position")
	public void setPosition(Vec3 vec3);

	@Accessor("removalReason")
	public void setRemovalReason(Entity.RemovalReason removalReason);

	@Accessor("level")
	public void setLevel(Level level);
}