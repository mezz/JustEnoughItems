package mezz.jei.common.input.handlers;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.focus.Focus;
import mezz.jei.common.input.IKeyBindings;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.CombinedRecipeFocusSource;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Optional;

public class FocusInputHandler implements IUserInputHandler {
	private final CombinedRecipeFocusSource focusSource;
	private final IRecipesGui recipesGui;

	public FocusInputHandler(CombinedRecipeFocusSource focusSource, IRecipesGui recipesGui) {
		this.focusSource = focusSource;
		this.recipesGui = recipesGui;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IKeyBindings keyBindings) {
		if (input.is(keyBindings.getShowRecipe())) {
			return handleShow(input, List.of(RecipeIngredientRole.OUTPUT), keyBindings);
		}

		if (input.is(keyBindings.getShowUses())) {
			return handleShow(input, List.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.CATALYST), keyBindings);
		}

		return Optional.empty();
	}

	private Optional<IUserInputHandler> handleShow(UserInput input, List<RecipeIngredientRole> roles, IKeyBindings keyBindings) {
		return focusSource.getIngredientUnderMouse(input, keyBindings)
			.findFirst()
			.map(clicked -> {
				if (!input.isSimulate()) {
					List<IFocus<?>> focuses = roles.stream()
						.<IFocus<?>>map(role -> new Focus<>(role, clicked.getTypedIngredient()))
						.toList();
					recipesGui.show(focuses);
				}
				return LimitedAreaInputHandler.create(this, clicked.getArea());
			});
	}
}
