package mezz.jei.api.recipe.category.extensions.vanilla.brewing;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.util.context.ContextMap;

import java.util.List;

/**
 * Implement this interface to convert a custom platform brewing recipe into recipes
 * that can be displayed as part of {@link RecipeTypes#BREWING}.
 *
 * <p>
 * Register this extension by getting the extendable brewing category from
 * {@link IVanillaCategoryExtensionRegistration#getBrewingCategory()}
 * and then registering it with
 * {@link IExtendableBrewingRecipeCategory#addExtension(Class, IBrewingCategoryExtension)}.
 * </p>
 *
 * @param <R> the custom platform brewing recipe type
 * @since 30.17.0
 */
@FunctionalInterface
public interface IBrewingCategoryExtension<R> {
	/**
	 * Convert a custom platform brewing recipe into JEI brewing recipes (or none).
	 *
	 * <p>
	 * A single platform recipe may return multiple JEI recipes when its output depends on the input,
	 * such as a recipe that preserves regular, splash, and lingering potion containers.
	 * Return an empty list when the platform recipe should not be displayed.
	 * The returned list and its elements must not be null.
	 * Each returned recipe must have a UID that uniquely identifies its displayed inputs, ingredients, and output.
	 * </p>
	 *
	 * @param recipe the custom platform brewing recipe
	 * @param vanillaRecipeFactory factory for creating JEI brewing recipes
	 * @param contextMap context for resolving recipe displays
	 * @return the JEI brewing recipes represented by the custom platform recipe
	 * @since 30.17.0
	 */
	List<IJeiBrewingRecipe> getBrewingRecipes(
		R recipe,
		IVanillaRecipeFactory vanillaRecipeFactory,
		ContextMap contextMap
	);
}
