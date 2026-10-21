package mezz.jei.fabric.test;

import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/** Records tooltips at the target Minecraft renderer boundary without drawing their contents. */
public final class TooltipBoundsCapture {
	private static final ThreadLocal<Consumer<ImmutableRect2i>> CAPTURE = new ThreadLocal<>();

	private TooltipBoundsCapture() {}

	static ImmutableRect2i capture(Runnable draw) {
		var bounds = new AtomicReference<>(ImmutableRect2i.EMPTY);
		Consumer<ImmutableRect2i> previous = CAPTURE.get();
		CAPTURE.set(bounds::set);
		try {
			draw.run();
			return bounds.get();
		} finally {
			if (previous == null) {
				CAPTURE.remove();
			} else {
				CAPTURE.set(previous);
			}
		}
	}

	public static boolean record(Font font, List<ClientTooltipComponent> lines, int x, int y, ClientTooltipPositioner positioner) {
		Consumer<ImmutableRect2i> capture = CAPTURE.get();
		if (capture == null) {
			return false;
		}
		int width = lines.stream().mapToInt(line -> line.getWidth(font)).max().orElse(0);
		int height = lines.stream().mapToInt(ClientTooltipComponent::getHeight).sum();
		if (lines.size() == 1) {
			height -= 2;
		}
		var window = Minecraft.getInstance().getWindow();
		var position = positioner.positionTooltip(window.getGuiScaledWidth(), window.getGuiScaledHeight(), x, y, width, height);
		capture.accept(new ImmutableRect2i(position.x(), position.y(), width, height));
		return true;
	}
}
