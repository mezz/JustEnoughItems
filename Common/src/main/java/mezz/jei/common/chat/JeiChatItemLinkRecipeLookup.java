package mezz.jei.common.chat;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.Internal;

import java.util.Optional;

public final class JeiChatItemLinkRecipeLookup {
	private JeiChatItemLinkRecipeLookup() {
	}

	public static int executeShowRecipeCommand(String linkText) {
		Optional<JeiChatItemLinks.IngredientLink> optionalLink = JeiChatItemLinks.parseCommandArgument(linkText);
		if (optionalLink.isEmpty()) {
			return 0;
		}

		Optional<IJeiRuntime> optionalRuntime = Internal.getOptionalJeiRuntime();
		if (optionalRuntime.isEmpty()) {
			return 0;
		}

		JeiChatItemLinks.IngredientLink link = optionalLink.get();
		IJeiRuntime runtime = optionalRuntime.get();
		boolean shown = showRecipeForIngredient(runtime, link);
		if (shown) {
			return 1;
		}
		return 0;
	}

	public static boolean showRecipeForIngredient(IJeiRuntime runtime, JeiChatItemLinks.IngredientLink link) {
		Optional<ITypedIngredient<?>> optionalIngredient = JeiChatItemLinks.resolveTypedIngredient(link, runtime.getIngredientManager());
		if (optionalIngredient.isEmpty()) {
			return false;
		}

		ITypedIngredient<?> typedIngredient = optionalIngredient.get();
		IRecipesGui recipesGui = runtime.getRecipesGui();
		IJeiHelpers jeiHelpers = runtime.getJeiHelpers();
		IFocusFactory focusFactory = jeiHelpers.getFocusFactory();
		IFocus<?> focus = createFocus(focusFactory, typedIngredient);

		recipesGui.show(focus);
		return true;
	}

	private static <T> IFocus<T> createFocus(IFocusFactory focusFactory, ITypedIngredient<T> typedIngredient) {
		return focusFactory.createFocus(RecipeIngredientRole.OUTPUT, typedIngredient);
	}
}
