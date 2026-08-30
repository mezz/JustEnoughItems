package mezz.jei.library.gui.recipes;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.renderer.Rect2i;

import java.util.List;

public final class RecipeWidgetTooltipDispatcher {
	private RecipeWidgetTooltipDispatcher() {
	}

	public static void addWidgetTooltips(
		ITooltipBuilder tooltip,
		List<IRecipeWidget> widgets,
		double mouseX,
		double mouseY,
		int categoryWidth,
		int categoryHeight
	) {
		if (!contains(mouseX, mouseY, categoryWidth, categoryHeight)) {
			return;
		}

		RecipeWidgetRenderer.forEachWidget(widgets, mouseX, mouseY, (widget, position, relativeMouseX, relativeMouseY) -> {
			if (isMouseOver(widget, mouseX, mouseY)) {
				widget.getTooltip(tooltip, relativeMouseX, relativeMouseY);
			}
		});
	}

	private static boolean isMouseOver(IRecipeWidget widget, double mouseX, double mouseY) {
		Rect2i area = widget.getArea();
		return MathUtil.contains(area, mouseX, mouseY);
	}

	private static boolean contains(double mouseX, double mouseY, int width, int height) {
		return mouseX >= 0 &&
			mouseY >= 0 &&
			mouseX < width &&
			mouseY < height;
	}
}
