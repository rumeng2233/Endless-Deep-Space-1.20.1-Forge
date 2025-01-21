package shirumengya.endless_deep_space.custom.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import shirumengya.endless_deep_space.mixins.EntityAccessor;

import java.util.function.Supplier;

public class DeleteEntityS2CPacket {
    private final int entityID;

    public DeleteEntityS2CPacket(int id) {
        this.entityID = id;
    }

    public DeleteEntityS2CPacket(FriendlyByteBuf buf) {
        this.entityID = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityID);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().player.level().getEntity(this.entityID);
            if (entity != null) {
                entity.setRemoved(Entity.RemovalReason.DISCARDED);
                entity.canUpdate(false);
                entity.gameEvent(GameEvent.ENTITY_DIE);
                //entity.setPos(new Vec3(Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY));
                entity.setRemoved(Entity.RemovalReason.UNLOADED_TO_CHUNK);
                entity.setLevelCallback(EntityInLevelCallback.NULL);
                entity.tickCount = Integer.MIN_VALUE;
                entity.setDeltaMovement(new Vec3(Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY));
                entity.setBoundingBox(new AABB(Vec3.ZERO, Vec3.ZERO));
                ((EntityAccessor) entity).setBlockPosition(new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
                ((EntityAccessor) entity).setChunkPosition(new ChunkPos(Integer.MAX_VALUE, Integer.MIN_VALUE));
                ((EntityAccessor) entity).setDeltaMovement(new Vec3(Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY));
                ((EntityAccessor) entity).setPosition(new Vec3(Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY));
                ((EntityAccessor) entity).setRemovalReason(Entity.RemovalReason.UNLOADED_TO_CHUNK);
                entity.invalidateCaps();
            }
        });
        return true;
    }

}