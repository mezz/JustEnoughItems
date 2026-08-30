package mezz.jei.library.gui.recipes;

import mezz.jei.api.gui.widgets.IRecipeWidget;
import net.minecraft.client.renderer.Rect2i;

import java.util.List;

public final class RecipeWidgetRenderer {
	private RecipeWidgetRenderer() {
	}

	public static void forEachWidget(
		List<IRecipeWidget> widgets,
		double mouseX,
		double mouseY,
		WidgetRenderCall renderCall
	) {
		for (IRecipeWidget widget : widgets) {
			Rect2i area = widget.getArea();
			renderCall.draw(
				widget,
				area,
				mouseX - area.getX(),
				mouseY - area.getY()
			);
		}
	}

	@FunctionalInterface
	public interface WidgetRenderCall {
		void draw(IRecipeWidget widget, Rect2i area, double mouseX, double mouseY);
	}
}
