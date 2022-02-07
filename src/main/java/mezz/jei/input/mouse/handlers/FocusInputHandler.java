package mezz.jei.input.mouse.handlers;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.Focus;
import mezz.jei.input.CombinedRecipeFocusSource;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FocusInputHandler implements IUserInputHandler {
	private final CombinedRecipeFocusSource focusSource;
	private final IRecipesGui recipesGui;

	public FocusInputHandler(CombinedRecipeFocusSource focusSource, IRecipesGui recipesGui) {
		this.focusSource = focusSource;
		this.recipesGui = recipesGui;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input) {
		if (input.is(KeyBindings.showRecipe)) {
			return handleShow(input, List.of(RecipeIngredientRole.OUTPUT));
		}

		if (input.is(KeyBindings.showUses)) {
			return handleShow(input, List.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.CATALYST));
		}

		return Optional.empty();
	}

	private Optional<IUserInputHandler> handleShow(UserInput input, List<RecipeIngredientRole> roles) {
		return focusSource.getIngredientUnderMouse(input)
			.map(clicked -> {
				if (!input.isSimulate()) {
					List<IFocus<?>> focuses = roles.stream()
						.<IFocus<?>>map(role -> new Focus<>(role, clicked.getValue()))
						.toList();
					recipesGui.show(focuses);
				}
				return LimitedAreaInputHandler.create(this, clicked.getArea());
			});
	}
}
