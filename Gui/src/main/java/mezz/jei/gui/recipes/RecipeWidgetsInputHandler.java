package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.common.input.IInputTarget;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.ApiInputAdapter;
import mezz.jei.common.input.interaction.IInputInteraction;
import mezz.jei.common.input.interaction.ReleaseInsideBounds;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

import java.util.Optional;

/** Handles JEI widget controls before ingredient commands and recipe API callbacks. */
public record RecipeWidgetsInputHandler(IRecipeLayoutDrawable<?> recipeLayout) implements IInputTarget {
	@Override
	public Optional<IInputInteraction> beginInput(Screen screen, UserInput input, IInternalKeyMappings keys) {
		if (!recipeLayout.isMouseOver(input.getMouseX(), input.getMouseY())) {
			return Optional.empty();
		}
		for (var widget : recipeLayout.getWidgets()) {
			if (widget instanceof IInputTarget target && widget instanceof IJeiInputHandler handler) {
				Optional<IInputInteraction> interaction = ApiInputAdapter.beginInputRelativeTo(screen, input, keys, target,
					() -> getScreenArea(handler));
				if (interaction.isPresent()) {
					return interaction.map(value -> new ReleaseInsideBounds(value, recipeLayout::isMouseOver));
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean scroll(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (!recipeLayout.isMouseOver(mouseX, mouseY)) {
			return false;
		}
		for (var widget : recipeLayout.getWidgets()) {
			if (widget instanceof IInputTarget && widget instanceof IJeiInputHandler handler) {
				ScreenRectangle area = getScreenArea(handler);
				if (MathUtil.contains(area, mouseX, mouseY) &&
					handler.handleMouseScrolled(mouseX - area.left(), mouseY - area.top(), scrollX, scrollY)
				) {
					return true;
				}
			}
		}
		return false;
	}

	private ScreenRectangle getScreenArea(IJeiInputHandler handler) {
		Rect2i recipeArea = recipeLayout.getRect();
		ScreenRectangle widgetArea = handler.getArea();
		return new ScreenRectangle(recipeArea.getX() + widgetArea.left(), recipeArea.getY() + widgetArea.top(), widgetArea.width(), widgetArea.height());
	}
}
