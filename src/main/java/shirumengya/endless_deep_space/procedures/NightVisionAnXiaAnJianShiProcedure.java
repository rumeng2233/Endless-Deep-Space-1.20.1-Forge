package shirumengya.endless_deep_space.procedures;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import shirumengya.endless_deep_space.custom.networking.ModMessages;
import shirumengya.endless_deep_space.custom.networking.packet.SendEndlessDeepSpaceCommonToastS2CPacket;
import shirumengya.endless_deep_space.init.EndlessDeepSpaceModItems;
import shirumengya.endless_deep_space.network.EndlessDeepSpaceModVariables;

public class NightVisionAnXiaAnJianShiProcedure {

	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (new Object() {
			public boolean checkGamemode(Entity _ent) {
				if (_ent instanceof ServerPlayer _serverPlayer) {
					return _serverPlayer.gameMode.getGameModeForPlayer() == GameType.CREATIVE;
				} else if (_ent.level().isClientSide() && _ent instanceof Player _player) {
					return Minecraft.getInstance().getConnection().getPlayerInfo(_player.getGameProfile().getId()) != null && Minecraft.getInstance().getConnection().getPlayerInfo(_player.getGameProfile().getId()).getGameMode() == GameType.CREATIVE;
				}
				return false;
			}
		}.checkGamemode(entity) || new Object() {
			public boolean checkGamemode(Entity _ent) {
				if (_ent instanceof ServerPlayer _serverPlayer) {
					return _serverPlayer.gameMode.getGameModeForPlayer() == GameType.SPECTATOR;
				} else if (_ent.level().isClientSide() && _ent instanceof Player _player) {
					return Minecraft.getInstance().getConnection().getPlayerInfo(_player.getGameProfile().getId()) != null && Minecraft.getInstance().getConnection().getPlayerInfo(_player.getGameProfile().getId()).getGameMode() == GameType.SPECTATOR;
				}
				return false;
			}
		}.checkGamemode(entity) || (entity instanceof Player _playerHasItem ? _playerHasItem.getInventory().contains(new ItemStack(EndlessDeepSpaceModItems.TOTEM_SWORD.get())) : false)) {
			{
				boolean _setval = !(entity.getCapability(EndlessDeepSpaceModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new EndlessDeepSpaceModVariables.PlayerVariables())).NightVision;
				entity.getCapability(EndlessDeepSpaceModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.NightVision = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			if ((entity.getCapability(EndlessDeepSpaceModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new EndlessDeepSpaceModVariables.PlayerVariables())).NightVision) {
				if (entity instanceof ServerPlayer _serverPlayer && !_serverPlayer.level().isClientSide)
					ModMessages.sendToPlayer(new SendEndlessDeepSpaceCommonToastS2CPacket(new ItemStack(Items.COMMAND_BLOCK), Component.translatable("message.endless_deep_space"), Component.translatable("key.endless_deep_space.night_vision_key.on"), 3000L, -11534256, -16777216), _serverPlayer);
			} else {
				if (entity instanceof ServerPlayer _serverPlayer && !_serverPlayer.level().isClientSide)
					ModMessages.sendToPlayer(new SendEndlessDeepSpaceCommonToastS2CPacket(new ItemStack(Items.COMMAND_BLOCK), Component.translatable("message.endless_deep_space"), Component.translatable("key.endless_deep_space.night_vision_key.off"), 3000L, -11534256, -16777216), _serverPlayer);
			}
		} else {
			if (entity instanceof ServerPlayer _serverPlayer && !_serverPlayer.level().isClientSide)
				ModMessages.sendToPlayer(new SendEndlessDeepSpaceCommonToastS2CPacket(new ItemStack(Items.BARRIER), Component.translatable("message.endless_deep_space"), Component.translatable("key.endless_deep_space.night_vision_key.error"), 3000L, -11534256, -65536), _serverPlayer);
		}
	}
}
