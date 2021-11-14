package mezz.jei.input.click;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.gui.Focus;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IMouseHandler;
import mezz.jei.input.InputHandler;
import mezz.jei.input.LimitedAreaMouseHandler;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.blaze3d.platform.InputConstants;

import javax.annotation.Nullable;

public class ClickFocusHandler implements IMouseHandler {
	private final InputHandler inputHandler;
	private final RecipesGui recipesGui;

	public ClickFocusHandler(InputHandler inputHandler, RecipesGui recipesGui) {
		this.inputHandler = inputHandler;
		this.recipesGui = recipesGui;
	}

	@Override
	@Nullable
	public IMouseHandler handleClick(Screen screen, double mouseX, double mouseY, int mouseButton, MouseClickState clickState) {
		IClickedIngredient<?> clicked = inputHandler.getFocusUnderMouseForClick(mouseX, mouseY);
		if (clicked == null) {
			return null;
		}
		if (handleMouseClickedFocus(mouseButton, clicked, clickState)) {
			return LimitedAreaMouseHandler.create(this, clicked.getArea());
		}
		InputConstants.Key input = InputConstants.Type.MOUSE.getOrCreate(mouseButton);
		if (inputHandler.handleFocusKeybinds(clicked, input, clickState)) {
			return LimitedAreaMouseHandler.create(this, clicked.getArea());
		}
		return null;
	}

	private <V> boolean handleMouseClickedFocus(int mouseButton, IClickedIngredient<V> clicked, MouseClickState clickState) {
		if (mouseButton == 0) {
			if (!clickState.isSimulate()) {
				Focus<?> focus = new Focus<>(IFocus.Mode.OUTPUT, clicked.getValue());
				recipesGui.show(focus);
			}
			return true;
		} else if (mouseButton == 1) {
			if (!clickState.isSimulate()) {
				Focus<?> focus = new Focus<>(IFocus.Mode.INPUT, clicked.getValue());
				recipesGui.show(focus);
			}
			return true;
		}

		return false;
	}
}
