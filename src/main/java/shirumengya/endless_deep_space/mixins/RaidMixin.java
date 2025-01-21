package shirumengya.endless_deep_space.mixins;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shirumengya.endless_deep_space.custom.init.ModEntities;
import shirumengya.endless_deep_space.custom.networking.ModMessages;
import shirumengya.endless_deep_space.custom.networking.packet.SetRaidDelayS2CPacket;

import java.util.Objects;
import java.util.Set;

@Mixin({Raid.class})
public abstract class RaidMixin {

	@Shadow @Final private ServerBossEvent raidEvent;

	@Shadow @Final private ServerLevel level;

	@Shadow @Final private RandomSource random;

	@Shadow private int groupsSpawned;

	public RaidMixin() {
		
	}

	@Inject(method = {"tick"}, at = {@At("HEAD")})
	public void updatePlayers(CallbackInfo ci) {
		Raid raid = ((Raid)(Object)this);
		if (!raid.isStopped()) {
			Set<ServerPlayer> set = Sets.newHashSet(this.raidEvent.getPlayers());
			if (!set.isEmpty()) {
				for (ServerPlayer serverplayer : set) {
					ModMessages.sendToPlayer(new SetRaidDelayS2CPacket(80), serverplayer);
				}
			}
		}
	}

	@Inject(method = {"spawnGroup"}, at = {@At("TAIL")})
	private void spawnCustomRaider(BlockPos p_37756_, CallbackInfo ci) {
		Raid raid = ((Raid)(Object)this);
		if (this.random.nextDouble() < 0.25D) {
			Raider raider = ModEntities.MUTATION_RAVAGER.get().create(this.level);
            if (raider != null) {
                raid.joinRaid(this.groupsSpawned + 1, raider, p_37756_, false);
				this.groupsSpawned++;
            }
            Raider raider1 = EntityType.EVOKER.create(this.level);
			if (raider1 != null) {
				raid.joinRaid(this.groupsSpawned + 1, raider1, p_37756_, false);
				this.groupsSpawned++;
				raider1.moveTo(p_37756_, 0.0F, 0.0F);
				raider1.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
				raider1.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
				raider1.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
				raider1.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
				raider1.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
				Objects.requireNonNull(raider1.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(100.0F);
				raider1.setHealth(raider1.getMaxHealth());
                if (raider != null) {
                    raider1.startRiding(raider);
                }
            }
		}
	}
}
