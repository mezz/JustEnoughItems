package mezz.jei.gui.overlay.elements;

import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.overlay.IngredientGridTooltipHelper;
import net.minecraft.client.gui.GuiGraphics;

public class RenderableElement<T> {
	private final IElement<T> element;
	private final IElementRenderer<T> renderer;

	public RenderableElement(IElement<T> element, IElementRenderer<T> renderer) {
		this.element = element;
		this.renderer = renderer;
	}

	public void render(GuiGraphics guiGraphics, ImmutableRect2i area, int padding) {
		renderer.render(guiGraphics, element, area, padding);
	}

	public void drawTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, IngredientGridTooltipHelper tooltipHelper) {
		renderer.drawTooltip(guiGraphics, mouseX, mouseY, tooltipHelper, element);
	}

	public IElement<T> getElement() {
		return element;
	}
}
