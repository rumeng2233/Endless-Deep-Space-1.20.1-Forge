package shirumengya.endless_deep_space.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shirumengya.endless_deep_space.custom.networking.ModMessages;
import shirumengya.endless_deep_space.custom.networking.packet.SetRecordDelayS2CPacket;

@Mixin({JukeboxBlockEntity.class})
public abstract class JukeboxBlockEntityMixin {

	public JukeboxBlockEntityMixin() {
		
	}

	@Inject(method = {"spawnMusicParticles"}, at = {@At("HEAD")}, cancellable = true)
	private void spawnMusicParticles(Level p_270782_, BlockPos p_270940_, CallbackInfo ci) {
		if (p_270782_ instanceof ServerLevel serverlevel) {
			Vec3 vec3 = Vec3.atBottomCenterOf(p_270940_).add(0.0D, (double)1.2F, 0.0D);
			float f = (float)p_270782_.getRandom().nextInt(4) / 24.0F;
			serverlevel.sendParticles(ParticleTypes.NOTE, vec3.x(), vec3.y(), vec3.z(), 0, (double)f, 0.0D, 0.0D, 1.0D);
			for(Player player : p_270782_.players()) {
				if (player instanceof ServerPlayer) {
					ServerPlayer serverplayer = (ServerPlayer)player;
					if (serverplayer.distanceToSqr(p_270940_.getX(), p_270940_.getY(), p_270940_.getZ()) < 1024.0D) {
						ModMessages.sendToPlayer(new SetRecordDelayS2CPacket(80), serverplayer);
					}
				}
			}
		}

		ci.cancel();
	}
}
