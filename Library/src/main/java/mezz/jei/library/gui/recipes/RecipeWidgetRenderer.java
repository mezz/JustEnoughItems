package mezz.jei.library.gui.recipes;

import mezz.jei.api.gui.widgets.IRecipeWidget;
import net.minecraft.client.gui.navigation.ScreenPosition;

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
			ScreenPosition position = widget.getPosition();
			renderCall.draw(
				widget,
				position,
				mouseX - position.x(),
				mouseY - position.y()
			);
		}
	}

	@FunctionalInterface
	public interface WidgetRenderCall {
		void draw(IRecipeWidget widget, ScreenPosition position, double mouseX, double mouseY);
	}
}
