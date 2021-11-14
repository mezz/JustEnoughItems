package mezz.jei.input;

import mezz.jei.input.click.MouseClickState;
import mezz.jei.util.MathUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.Rectangle2d;

import javax.annotation.Nullable;

public class LimitedAreaMouseHandler implements IMouseHandler {
	private final IMouseHandler mouseHandler;
	private final Rectangle2d area;

	public static IMouseHandler create(IMouseHandler mouseHandler, @Nullable Rectangle2d area) {
		if (area == null) {
			return mouseHandler;
		}
		return new LimitedAreaMouseHandler(mouseHandler, area);
	}

	private LimitedAreaMouseHandler(IMouseHandler mouseHandler, Rectangle2d area) {
		this.mouseHandler = mouseHandler;
		this.area = area;
	}

	@Override
	public IMouseHandler handleClick(Screen screen, double mouseX, double mouseY, int mouseButton, MouseClickState clickState) {
		if (MathUtil.contains(this.area, mouseX, mouseY)) {
			if (this.mouseHandler.handleClick(screen, mouseX, mouseY, mouseButton, clickState) != null) {
				return this;
			}
		}
		return null;
	}

	@Override
	public void handleMouseClickedOut(int mouseButton) {
		this.mouseHandler.handleMouseClickedOut(mouseButton);
	}

	@Override
	public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
		return MathUtil.contains(this.area, mouseX, mouseY) &&
				this.mouseHandler.handleMouseScrolled(mouseX, mouseY, scrollDelta);
	}
}
