package mezz.jei.input.mouse.handlers;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.Focus;
import mezz.jei.input.CombinedRecipeFocusSource;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;

public class FocusInputHandler implements IUserInputHandler {
	private final CombinedRecipeFocusSource focusSource;
	private final IRecipesGui recipesGui;

	public FocusInputHandler(CombinedRecipeFocusSource focusSource, IRecipesGui recipesGui) {
		this.focusSource = focusSource;
		this.recipesGui = recipesGui;
	}

	@Override
	@Nullable
	public IUserInputHandler handleUserInput(Screen screen, UserInput input) {
		IClickedIngredient<?> clicked = focusSource.getIngredientUnderMouse(input);
		if (clicked == null) {
			return null;
		}

		if (input.is(KeyBindings.showRecipe)) {
			if (!input.isSimulate()) {
				Focus<?> focus = new Focus<>(IFocus.Mode.OUTPUT, clicked.getValue());
				recipesGui.show(focus);
			}
			return LimitedAreaUserInputHandler.create(this, clicked.getArea());
		}

		if (input.is(KeyBindings.showUses)) {
			if (!input.isSimulate()) {
				Focus<?> focus = new Focus<>(IFocus.Mode.INPUT, clicked.getValue());
				recipesGui.show(focus);
			}
			return LimitedAreaUserInputHandler.create(this, clicked.getArea());
		}

		return null;
	}
}
