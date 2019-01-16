package mezz.jei.gui.elements;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.client.config.HoverChecker;
import net.minecraft.client.Minecraft;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.config.Constants;
import mezz.jei.gui.TooltipRenderer;

public abstract class GuiIconToggleButton {
	private final IDrawable offIcon;
	private final IDrawable onIcon;
	private final GuiIconButton button;
	private final HoverChecker hoverChecker;

	public GuiIconToggleButton(IDrawable offIcon, IDrawable onIcon) {
		this.offIcon = offIcon;
		this.onIcon = onIcon;
		this.button = new GuiIconButton(2, new DrawableBlank(0, 0), (mc, x, y) -> true);
		this.hoverChecker = new HoverChecker(this.button, 0);
	}

	public void updateBounds(Rectangle area) {
		this.button.width = area.width;
		this.button.height = area.height;
		this.button.x = area.x;
		this.button.y = area.y;
	}

	public void draw(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
		this.button.drawButton(minecraft, mouseX, mouseY, partialTicks);
		IDrawable icon = isIconToggledOn() ? this.onIcon : this.offIcon;
		icon.draw(minecraft, this.button.x + 2, this.button.y + 2);
	}

	public final boolean isMouseOver(int mouseX, int mouseY) {
		return this.hoverChecker.checkHover(mouseX, mouseY);
	}

	public final boolean handleMouseClick(int mouseX, int mouseY) {
		Minecraft minecraft = Minecraft.getMinecraft();
		return button.mousePressed(minecraft, mouseX, mouseY) && onMouseClicked(mouseX, mouseY);
	}

	public final void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			List<String> tooltip = new ArrayList<>();
			getTooltips(tooltip);
			TooltipRenderer.drawHoveringText(minecraft, tooltip, mouseX, mouseY, Constants.MAX_TOOLTIP_WIDTH);
		}
	}

	protected abstract void getTooltips(List<String> tooltip);

	protected abstract boolean isIconToggledOn();

	protected abstract boolean onMouseClicked(int mouseX, int mouseY);
}
