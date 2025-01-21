package shirumengya.endless_deep_space.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.resources.sounds.GuardianAttackSoundInstance;
import net.minecraft.client.resources.sounds.SnifferSoundInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shirumengya.endless_deep_space.custom.client.sounds.MutationRavagerRemoteAttackSoundInstance;
import shirumengya.endless_deep_space.custom.client.sounds.OceanDefenderLaserAttackSoundInstance;
import shirumengya.endless_deep_space.custom.entity.boss.oceandefenders.OceanDefender;
import shirumengya.endless_deep_space.custom.entity.miniboss.MutationRavager;
import shirumengya.endless_deep_space.init.EndlessDeepSpaceModSounds;

@Mixin({ClientPacketListener.class})
public abstract class ClientPacketListenerMixin {

	@Shadow private ClientLevel level;

	@Shadow @Final private Minecraft minecraft;

	public ClientPacketListenerMixin() {
	}

	@Inject(method = {"handleEntityEvent"}, at = {@At("HEAD")}, cancellable = true)
	public void handleEntityEvent(ClientboundEntityEventPacket p_105010_, CallbackInfo ci) {
		ClientPacketListener listener = ((ClientPacketListener)(Object)this);
		PacketUtils.ensureRunningOnSameThread(p_105010_, listener, this.minecraft);
		Entity entity = p_105010_.getEntity(this.level);
		if (entity != null) {
			switch (p_105010_.getEventId()) {
				case -7:
					this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
					this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0F, 1.0F, false);
					if (entity == this.minecraft.player) {
						this.minecraft.gameRenderer.displayItemActivation(new ItemStack(Items.BARRIER));
					}
					break;
				case -6:
					this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.PORTAL, 3);
					break;
				case -5:
					this.minecraft.getSoundManager().play(new OceanDefenderLaserAttackSoundInstance((OceanDefender) entity));
					break;
				case -4:
					this.minecraft.getSoundManager().play(new MutationRavagerRemoteAttackSoundInstance((MutationRavager) entity));
					break;
				case -2:
					this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
					this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0F, 1.0F, false);
					if (entity == this.minecraft.player) {
						this.minecraft.gameRenderer.displayItemActivation(new ItemStack(Items.TOTEM_OF_UNDYING));
					}
					break;
				case -1:
					this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), EndlessDeepSpaceModSounds.ITEM_SWORD_BLOCK.get(), entity.getSoundSource(), 1.0F, 0.8F + entity.level().random.nextFloat() * 0.4F, false);
					break;
				case 21:
					this.minecraft.getSoundManager().play(new GuardianAttackSoundInstance((Guardian)entity));
					break;
				case 35:
					this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
					this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0F, 1.0F, false);
					if (entity == this.minecraft.player) {
						this.minecraft.gameRenderer.displayItemActivation(findTotem(this.minecraft.player));
					}
					break;
				case 63:
					this.minecraft.getSoundManager().play(new SnifferSoundInstance((Sniffer)entity));
					break;
				default:
					entity.handleEntityEvent(p_105010_.getEventId());
			}
		}

		ci.cancel();
	}

	private static ItemStack findTotem(Player p_104928_) {
		for(InteractionHand interactionhand : InteractionHand.values()) {
			ItemStack itemstack = p_104928_.getItemInHand(interactionhand);
			if (itemstack.is(Items.TOTEM_OF_UNDYING)) {
				return itemstack;
			}
		}

		return new ItemStack(Items.TOTEM_OF_UNDYING);
	}
}
