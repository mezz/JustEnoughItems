package mezz.jei.fabric.test.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {
	@Invoker("onPress")
	void jei$invokeOnPress(long window, int button, int action, int modifiers);

	@Invoker("onMove")
	void jei$invokeOnMove(long window, double x, double y);

	@Invoker("onScroll")
	void jei$invokeOnScroll(long window, double horizontal, double vertical);
}
