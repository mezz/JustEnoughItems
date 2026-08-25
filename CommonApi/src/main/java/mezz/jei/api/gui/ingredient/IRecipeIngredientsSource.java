package mezz.jei.api.gui.ingredient;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.tags.TagKey;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * A source of ingredients in a recipe: a role, a set of ingredient variations,
 * and an optional tag.
 *
 * <p>This is the unified view shared by JEI's recipe slots (via {@link IRecipeSlotView})
 * and by external elements implemented by addon mods.
 * Results are evaluated on every call, so the ingredients can change over time.</p>
 *
 * @since 30.26.0
 */
public interface IRecipeIngredientsSource {
	/**
	 * The role of the ingredients in this source (input, output, catalyst, etc.).
	 *
	 * @since 30.26.0
	 */
	RecipeIngredientRole getRole();

	/**
	 * All ingredient variations that can be shown by this source.
	 *
	 * @since 30.26.0
	 */
	Stream<ITypedIngredient<?>> getAllIngredients();

	/**
	 * The ingredient variation that is shown at this moment.
	 * Defaults to the first ingredient from {@link #getAllIngredients()}.
	 *
	 * @since 30.26.0
	 */
	default Optional<ITypedIngredient<?>> getDisplayedIngredient() {
		return getAllIngredients().findFirst();
	}

	/**
	 * The tag represented by every ingredient in this source, if there is one.
	 *
	 * @since 30.26.0
	 */
	default Optional<TagKey<?>> getTagKey() {
		return Optional.empty();
	}
}
