package mezz.jei.gui.elements;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.config.Constants;
import mezz.jei.gui.HoverChecker;
import mezz.jei.gui.TooltipRenderer;

public abstract class GuiIconToggleButton {
	private final IDrawable offIcon;
	private final IDrawable onIcon;
	private final GuiIconButton button;
	private final HoverChecker hoverChecker;

	public GuiIconToggleButton(IDrawable offIcon, IDrawable onIcon) {
		this.offIcon = offIcon;
		this.onIcon = onIcon;
		this.button = new GuiIconButton(2, new DrawableBlank(0, 0));
		this.hoverChecker = new HoverChecker(this.button);
	}

	public void updateBounds(Rectangle area) {
		this.button.width = area.width;
		this.button.height = area.height;
		this.button.x = area.x;
		this.button.y = area.y;
	}

	public void draw(int mouseX, int mouseY, float partialTicks) {
		this.button.render(mouseX, mouseY, partialTicks);
		IDrawable icon = isIconToggledOn() ? this.onIcon : this.offIcon;
		icon.draw(this.button.x + 2, this.button.y + 2);
	}

	public final boolean isMouseOver(double mouseX, double mouseY) {
		return this.hoverChecker.checkHover(mouseX, mouseY);
	}

	public final boolean handleMouseClick(double mouseX, double mouseY, int mouseButton) {
		return button.mouseClicked(mouseX, mouseY, mouseButton) &&
			onMouseClicked(mouseX, mouseY, mouseButton);
	}

	public final void drawTooltips(int mouseX, int mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			List<String> tooltip = new ArrayList<>();
			getTooltips(tooltip);
			TooltipRenderer.drawHoveringText(tooltip, mouseX, mouseY, Constants.MAX_TOOLTIP_WIDTH);
		}
	}

	protected abstract void getTooltips(List<String> tooltip);

	protected abstract boolean isIconToggledOn();

	protected abstract boolean onMouseClicked(double mouseX, double mouseY, int mouseButton);
}
