package shirumengya.endless_deep_space.custom.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

public class SwordItemBlock {
    public static final HumanoidModel.ArmPose SWORD_BLOCK = getSwordBlocking();

    public static HumanoidModel.ArmPose getSwordBlocking() {
        return HumanoidModel.ArmPose.create("SWORD_BLOCK", true, (model, entity, arm) -> {
        });
    }

    public static void renderArm(boolean type, ModelPart arm) {
        if (type) {
            arm.xRot = arm.xRot * 0.5F - 0.9424779F;
            arm.yRot = (-(float) Math.PI / 6.0F);
        } else {
            arm.xRot = arm.xRot * 0.5F - 0.9424779F;
            arm.yRot = ((float)Math.PI / 6.0F);
        }
    }

    public static void applyItemArmTransform(PoseStack p_109383_, HumanoidArm p_109384_, float p_109385_) {
        int i = p_109384_ == HumanoidArm.RIGHT ? 1 : -1;
        p_109383_.translate((float) i * 0.56F, -0.52F + p_109385_ * -0.6F, -0.72F);
    }

    public static boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
        if (!player.isUsingItem())
            return false;
        applyItemArmTransform(poseStack, arm, equipProcess);
        int horizontal = (arm == HumanoidArm.RIGHT) ? 1 : -1;
        poseStack.translate(horizontal * -0.14142136F, 0.08F, 0.14142136F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-102.25F));
        poseStack.mulPose(Axis.YP.rotationDegrees(horizontal * 13.365F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(horizontal * 78.05F));
        return true;
    }
}
