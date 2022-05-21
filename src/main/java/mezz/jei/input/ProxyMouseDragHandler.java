package mezz.jei.input;

import net.minecraft.client.gui.screen.Screen;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ProxyMouseDragHandler implements IMouseDragHandler {
	private final Supplier<IMouseDragHandler> source;

	public ProxyMouseDragHandler(Supplier<IMouseDragHandler> source) {
		this.source = source;
	}

	@Nullable
	@Override
	public IMouseDragHandler handleDragStart(Screen screen, double mouseX, double mouseY) {
		return this.source.get().handleDragStart(screen, mouseX, mouseY);
	}

	@Nullable
	@Override
	public IMouseDragHandler handleDragComplete(Screen screen, double mouseX, double mouseY) {
		return this.source.get().handleDragComplete(screen, mouseX, mouseY);
	}

	@Override
	public void handleDragCanceled() {
		this.source.get().handleDragCanceled();
	}
}
