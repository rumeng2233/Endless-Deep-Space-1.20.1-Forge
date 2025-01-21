package shirumengya.endless_deep_space.mixins;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shirumengya.endless_deep_space.custom.client.gui.screens.EndlessDeepSpaceCredits;
import shirumengya.endless_deep_space.custom.client.sounds.ModMusicManager;

@Mixin({CreditsAndAttributionScreen.class})
public abstract class CreditsAndAttributionScreenMixin extends Screen {
	private static final Component END_POEM_TEXT = Component.translatable("message.endless_deep_space.end_poem");

	public CreditsAndAttributionScreenMixin() {
        super(GameNarrator.NO_TITLE);

    }

	@Inject(method = {"init"}, at = {@At("HEAD")})
	public void init(CallbackInfo ci) {
		CreditsAndAttributionScreen screen = ((CreditsAndAttributionScreen)(Object)this);
		if (ModMusicManager.seenCredits) {
			int i = this.font.width(END_POEM_TEXT);
			int j = this.width - i - 2;
			this.addRenderableWidget(new PlainTextButton(j, this.height - 10, i, 10, END_POEM_TEXT, (p_280834_) -> {
				this.minecraft.setScreen(new EndlessDeepSpaceCredits(EndlessDeepSpaceCredits.Chapters.MINECRAFT_END_POEM, 0.5F, () -> {
					this.minecraft.setScreen(screen);
				}));
			}, this.font));
		}
	}
}
