package mezz.jei.gui.input.handlers;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.input.IClickableIngredientInternal;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.input.CombinedRecipeFocusSource;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
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
					boolean handled = handleRecipeBookmark(clicked, recipesGui);
					if (!handled) {
						List<IFocus<?>> focuses = roles.stream()
								.<IFocus<?>>map(role -> focusFactory.createFocus(role, clicked.getTypedIngredient()))
								.toList();
						recipesGui.show(focuses);
					}
				}
				return LimitedAreaInputHandler.create(this, clicked.getArea());
			});
	}

	private <T> boolean handleRecipeBookmark(IClickableIngredientInternal<T> clicked, IRecipesGui recipesGui) {
		if (clicked.getTypedIngredient().getIngredient() instanceof RecipeBookmark<?> recipeBookmark){
			recipesGui.show(recipeBookmark.getFocuses().getAllFocuses());
			recipesGui.setRecipeCategory(recipeBookmark.getRecipeCategory());
			recipesGui.setRecipeIndex(recipeBookmark.getIndex());
			return true;
		}
		return false;
	}

}
