package mezz.jei.gui.overlay.elements;

import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.overlay.IngredientGridTooltipHelper;
import net.minecraft.client.gui.GuiGraphics;

public interface IElementRenderer<T> {
	void render(GuiGraphics guiGraphics, IElement<T> element, ImmutableRect2i area, int padding);

	void drawTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, IngredientGridTooltipHelper tooltipHelper, IElement<T> element);
}
