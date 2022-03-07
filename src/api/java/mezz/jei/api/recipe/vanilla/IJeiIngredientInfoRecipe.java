package mezz.jei.api.recipe.vanilla;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * A recipe in JEI that displays a page of text information about an ingredient.
 *
 * Create your own with {@link IRecipeRegistration#addIngredientInfo}.
 *
 * @since 9.5.0
 */
public interface IJeiIngredientInfoRecipe {
	/**
	 * The input ingredients for the recipe.
	 *
	 * @since 9.5.0
	 */
	@Unmodifiable
	List<ITypedIngredient<?>> getIngredients();

	/**
	 * A short description of the ingredients, broken up across multiple lines of text.
	 *
	 * @since 9.5.0
	 */
	@Unmodifiable
	List<FormattedText> getDescription();
}
