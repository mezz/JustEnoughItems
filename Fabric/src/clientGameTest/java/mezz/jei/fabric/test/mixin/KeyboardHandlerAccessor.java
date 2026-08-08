package mezz.jei.fabric.test.mixin;

import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(KeyboardHandler.class)
public interface KeyboardHandlerAccessor {
	@Invoker("keyPress")
	void jei$invokeKeyPress(long window, int key, int scancode, int action, int modifiers);

	@Invoker("charTyped")
	void jei$invokeCharTyped(long window, int codepoint, int modifiers);
}
