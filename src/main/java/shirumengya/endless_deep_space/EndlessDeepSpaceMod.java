package shirumengya.endless_deep_space;

import shirumengya.endless_deep_space.init.EndlessDeepSpaceModSounds;
import shirumengya.endless_deep_space.init.EndlessDeepSpaceModPotions;
import shirumengya.endless_deep_space.init.EndlessDeepSpaceModMobEffects;
import shirumengya.endless_deep_space.init.EndlessDeepSpaceModItems;
import shirumengya.endless_deep_space.init.EndlessDeepSpaceModEnchantments;
import shirumengya.endless_deep_space.custom.networking.ModMessages;
import shirumengya.endless_deep_space.custom.item.CustomItemProperties;
import shirumengya.endless_deep_space.custom.init.ModTabs;
import shirumengya.endless_deep_space.custom.init.ModSounds;
import shirumengya.endless_deep_space.custom.init.ModRecipes;
import shirumengya.endless_deep_space.custom.init.ModMobEffects;
import shirumengya.endless_deep_space.custom.init.ModMenuTypes;
import shirumengya.endless_deep_space.custom.init.ModItems;
import shirumengya.endless_deep_space.custom.init.ModEntities;
import shirumengya.endless_deep_space.custom.init.ModBlocks;
import shirumengya.endless_deep_space.custom.init.ModBlockEntities;
import shirumengya.endless_deep_space.custom.init.ModAttributes;
import shirumengya.endless_deep_space.custom.client.event.ClientEvent;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.Minecraft;

import java.util.function.Supplier;
import java.util.function.Function;
import java.util.function.BiConsumer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Collection;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.AbstractMap;

import java.lang.reflect.Field;

import java.io.StringWriter;
import java.io.PrintWriter;

@Mod("endless_deep_space")
public class EndlessDeepSpaceMod {
	public static final Logger LOGGER = LogManager.getLogger(EndlessDeepSpaceMod.class);
	public static final String MODID = "endless_deep_space";

	public EndlessDeepSpaceMod() {
		// Start of user code block mod constructor
		// End of user code block mod constructor
		MinecraftForge.EVENT_BUS.register(this);
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		EndlessDeepSpaceModSounds.REGISTRY.register(bus);
		EndlessDeepSpaceModItems.REGISTRY.register(bus);
		EndlessDeepSpaceModEnchantments.REGISTRY.register(bus);
		EndlessDeepSpaceModMobEffects.REGISTRY.register(bus);
		EndlessDeepSpaceModPotions.REGISTRY.register(bus);
		// Start of user code block mod init
		bus.addListener(this::commonSetup);
		bus.addListener(this::setupClient);
		ModMobEffects.REGISTRY.register(bus);
		ModAttributes.REGISTRY.register(bus);
		ModEntities.REGISTRY.register(bus);
		ModItems.REGISTRY.register(bus);
		ModTabs.REGISTRY.register(bus);
		ModSounds.REGISTRY.register(bus);
		ModBlocks.REGISTRY.register(bus);
		ModBlockEntities.REGISTRY.register(bus);
		ModMenuTypes.REGISTRY.register(bus);
		ModRecipes.REGISTRY.register(bus);
		// End of user code block mod init
	}

	// Start of user code block mod methods
	public static final String MOD_VERSION = "0.9.8";
	public static final List<String> incompatibleMods = Arrays.asList(/*"fantasy_ending"*/"");

	private void commonSetup(FMLCommonSetupEvent event) {
		ModMessages.register();
	}

	private void setupClient(FMLClientSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(new ClientEvent());
		CustomItemProperties.addCustomItemProperties();
	}

	// End of user code block mod methods
	public static class TextboxSetMessage {
		private final String textboxid;
		private final String data;

		public TextboxSetMessage(FriendlyByteBuf buffer) {
			this.textboxid = buffer.readComponent().getString();
			this.data = buffer.readComponent().getString();
		}

		public TextboxSetMessage(String textboxid, String data) {
			this.textboxid = textboxid;
			this.data = data;
		}

		public static void buffer(TextboxSetMessage message, FriendlyByteBuf buffer) {
			buffer.writeComponent(Component.literal(message.textboxid));
			buffer.writeComponent(Component.literal(message.data));
		}

		public static void handler(TextboxSetMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
			NetworkEvent.Context context = contextSupplier.get();
			context.enqueueWork(() -> {
				if (!context.getDirection().getReceptionSide().isServer() && message.data != null) {
					Screen currentScreen = Minecraft.getInstance().screen;
					Map<String, EditBox> textFieldsMap = new HashMap<>();
					if (currentScreen != null) {
						Field[] fields = currentScreen.getClass().getDeclaredFields();
						for (Field field : fields) {
							if (EditBox.class.isAssignableFrom(field.getType())) {
								try {
									field.setAccessible(true);
									EditBox textField = (EditBox) field.get(currentScreen);
									if (textField != null) {
										textFieldsMap.put(field.getName(), textField);
									}
								} catch (IllegalAccessException ex) {
									StringWriter sw = new StringWriter();
									PrintWriter pw = new PrintWriter(sw);
									ex.printStackTrace(pw);
									String exceptionAsString = sw.toString();
									EndlessDeepSpaceMod.LOGGER.error(exceptionAsString);
								}
							}
						}
					}
					if (textFieldsMap.get(message.textboxid) != null) {
						textFieldsMap.get(message.textboxid).setValue(message.data);
					}
				}
			});
			context.setPacketHandled(true);
		}
	}

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class initer {
		@SubscribeEvent
		public static void init(FMLCommonSetupEvent event) {
			EndlessDeepSpaceMod.addNetworkMessage(TextboxSetMessage.class, TextboxSetMessage::buffer, TextboxSetMessage::new, TextboxSetMessage::handler);
		}
	}

	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	private static int messageID = 0;

	public static <T> void addNetworkMessage(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
		PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer);
		messageID++;
	}

	private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

	public static void queueServerWork(int tick, Runnable action) {
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
			workQueue.add(new AbstractMap.SimpleEntry<>(action, tick));
	}

	@SubscribeEvent
	public void tick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			List<AbstractMap.SimpleEntry<Runnable, Integer>> actions = new ArrayList<>();
			workQueue.forEach(work -> {
				work.setValue(work.getValue() - 1);
				if (work.getValue() == 0)
					actions.add(work);
			});
			actions.forEach(e -> e.getKey().run());
			workQueue.removeAll(actions);
		}
	}
}
