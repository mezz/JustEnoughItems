package mezz.jei.input;

import mezz.jei.input.click.MouseClickState;
import mezz.jei.util.MathUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

import javax.annotation.Nullable;

public class LimitedAreaMouseHandler implements IMouseHandler {
	private final IMouseHandler mouseHandler;
	private final Rect2i area;

	public static IMouseHandler create(IMouseHandler mouseHandler, @Nullable Rect2i area) {
		if (area == null) {
			return mouseHandler;
		}
		return new LimitedAreaMouseHandler(mouseHandler, area);
	}

	private LimitedAreaMouseHandler(IMouseHandler mouseHandler, Rect2i area) {
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
