package mezz.jei.gui.input.handlers;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.CombinedRecipeFocusSource;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Optional;

public class FocusInputHandler implements IUserInputHandler {
	private final CombinedRecipeFocusSource focusSource;
	private final IRecipesGui recipesGui;
	private final IFocusFactory focusFactory;

	public FocusInputHandler(CombinedRecipeFocusSource focusSource, IRecipesGui recipesGui, IFocusFactory focusFactory) {
		this.focusSource = focusSource;
		this.recipesGui = recipesGui;
		this.focusFactory = focusFactory;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		if (input.is(keyBindings.getShowRecipe())) {
			return handleShow(input, List.of(RecipeIngredientRole.OUTPUT), keyBindings);
		}

		if (input.is(keyBindings.getShowUses())) {
			return handleShow(input, List.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.CATALYST), keyBindings);
		}

		return Optional.empty();
	}

	private Optional<IUserInputHandler> handleShow(UserInput input, List<RecipeIngredientRole> roles, IInternalKeyMappings keyBindings) {
		return focusSource.getIngredientUnderMouse(input, keyBindings)
			.findFirst()
			.map(clicked -> {
				if (!input.isSimulate()) {
					List<IFocus<?>> focuses = roles.stream()
						.<IFocus<?>>map(role -> focusFactory.createFocus(role, clicked.getTypedIngredient()))
						.toList();
					recipesGui.show(focuses);
				}
				ImmutableRect2i area = clicked.getArea();
				return LimitedAreaInputHandler.create(this, area);
			});
	}
}
