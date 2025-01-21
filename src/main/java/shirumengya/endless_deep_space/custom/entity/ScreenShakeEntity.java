package shirumengya.endless_deep_space.custom.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import shirumengya.endless_deep_space.custom.init.ModEntities;

public class ScreenShakeEntity extends Entity {
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(ScreenShakeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAGNITUDE = SynchedEntityData.defineId(ScreenShakeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(ScreenShakeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FADE_DURATION = SynchedEntityData.defineId(ScreenShakeEntity.class, EntityDataSerializers.INT);

    public ScreenShakeEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    public ScreenShakeEntity(Level world, Vec3 position, float radius, float magnitude, int duration, int fadeDuration) {
        super(ModEntities.SCREEN_SHAKE.get(), world);
        this.setRadius(radius);
        this.setMagnitude(magnitude);
        this.setDuration(duration);
        this.setFadeDuration(fadeDuration);
        this.setPos(position.x, position.y, position.z);
    }

    @OnlyIn(Dist.CLIENT)
    public float getShakeAmount(Entity entity, float delta) {
        float ticksDelta = this.tickCount + delta;
        float timeFrac = 1.0f - (ticksDelta - this.getDuration()) / (this.getFadeDuration() + 1.0f);
        float baseAmount = ticksDelta < this.getDuration() ? this.getMagnitude() : timeFrac * timeFrac * this.getMagnitude();
        Vec3 playerPos = entity.getEyePosition(delta);
        float distFrac = (float) (1.0f - Mth.clamp(position().distanceTo(playerPos) / this.getRadius(), 0, 1));
        return baseAmount * distFrac * distFrac;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount > (this.getDuration() + this.getFadeDuration())) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(RADIUS, 10.0f);
        this.getEntityData().define(MAGNITUDE, 1.0f);
        this.getEntityData().define(DURATION, 0);
        this.getEntityData().define(FADE_DURATION, 5);
    }

    public float getRadius() {
        return this.getEntityData().get(RADIUS);
    }

    public void setRadius(float radius) {
        this.getEntityData().set(RADIUS, radius);
    }

    public float getMagnitude() {
        return this.getEntityData().get(MAGNITUDE);
    }

    public void setMagnitude(float magnitude) {
        this.getEntityData().set(MAGNITUDE, magnitude);
    }

    public int getDuration() {
        return this.getEntityData().get(DURATION);
    }

    public void setDuration(int duration) {
        this.getEntityData().set(DURATION, duration);
    }

    public int getFadeDuration() {
        return this.getEntityData().get(FADE_DURATION);
    }

    public void setFadeDuration(int fadeDuration) {
        this.getEntityData().set(FADE_DURATION, fadeDuration);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.setRadius(compound.getFloat("Radius"));
        this.setMagnitude(compound.getFloat("Magnitude"));
        this.setDuration(compound.getInt("Duration"));
        this.setFadeDuration(compound.getInt("FadeDuration"));
        this.tickCount = compound.getInt("TicksExisted");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putFloat("Radius", this.getRadius());
        compound.putFloat("Magnitude", this.getMagnitude());
        compound.putInt("Duration", this.getDuration());
        compound.putInt("FadeDuration", this.getFadeDuration());
        compound.putInt("TicksExisted", this.tickCount);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public static void ScreenShake(Level world, Vec3 position, float radius, float magnitude, int duration, int fadeDuration) {
        if (!world.isClientSide) {
            ScreenShakeEntity ScreenShake = new ScreenShakeEntity(world, position, radius, magnitude, duration, fadeDuration);
            world.addFreshEntity(ScreenShake);
        }
    }
}
