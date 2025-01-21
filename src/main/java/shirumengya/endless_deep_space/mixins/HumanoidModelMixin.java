package shirumengya.endless_deep_space.mixins;

import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shirumengya.endless_deep_space.custom.item.SwordItemBlock;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> extends AgeableListModel<T> implements ArmedModel, HeadedModel {

    @Shadow
    public HumanoidModel.ArmPose rightArmPose;

    @Shadow
    public HumanoidModel.ArmPose leftArmPose;

    @Shadow
    @Final
    public ModelPart leftArm;

    @Shadow
    @Final
    public ModelPart rightArm;

    @Inject(method = {"poseRightArm"}, at = {@At("HEAD")}, cancellable = true)
    private void renderRight(T entity, CallbackInfo ci) {
        if (this.rightArmPose == SwordItemBlock.SWORD_BLOCK) {
            SwordItemBlock.renderArm(true, this.rightArm);
            ci.cancel();
        }
    }

    @Inject(method = {"poseLeftArm"}, at = {@At("HEAD")}, cancellable = true)
    private void renderLeft(T entity, CallbackInfo ci) {
        if (this.leftArmPose == SwordItemBlock.SWORD_BLOCK) {
            SwordItemBlock.renderArm(false, this.leftArm);
            ci.cancel();
        }
    }

    @Inject(method = {"setupAttackAnimation"}, at = {@At("HEAD")}, cancellable = true)
    private void renderCancel(T entity, float ageInTicks, CallbackInfo ci) {
        if (entity.getMainArm() == HumanoidArm.RIGHT && this.rightArmPose == SwordItemBlock.SWORD_BLOCK) {
            ci.cancel();
        }
        if (entity.getMainArm() == HumanoidArm.LEFT && this.leftArmPose == SwordItemBlock.SWORD_BLOCK) {
            ci.cancel();
        }
    }
}
