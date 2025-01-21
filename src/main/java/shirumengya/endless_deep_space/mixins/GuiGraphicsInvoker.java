package shirumengya.endless_deep_space.mixins;

import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiGraphics.class)
public interface GuiGraphicsInvoker {
	@Invoker("flushIfUnmanaged")
	public void invokerFlushIfUnmanaged();
}