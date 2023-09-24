package mezz.jei.api.recipe.category.extensions;

import java.util.function.Function;
import java.util.function.Predicate;

import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.IExtendableCraftingRecipeCategory;

/**
 * @deprecated breaking change: replaced by a simpler interface {@link IExtendableCraftingRecipeCategory} to support RecipeHolder
 */
@Deprecated(since = "16.0.0", forRemoval = true)
public interface IExtendableRecipeCategory<T, W extends IRecipeCategoryExtension<T>> extends IRecipeCategory<T> {
	/**
	 * Add an extension that handles a subset of the recipes in the recipe category.
	 *
	 * @param recipeClass      the class of recipes to handle
	 * @param extensionFactory a factory that can turn recipes into recipe extensions
	 * @deprecated extensions can be singletons now, use {@link IExtendableCraftingRecipeCategory#addExtension(Class, ICraftingCategoryExtension)}
	 */
	@Deprecated(since = "16.0.0", forRemoval = true)
	<R extends T> void addCategoryExtension(Class<? extends R> recipeClass, Function<R, ? extends W> extensionFactory);

	/**
	 * Add an extension that handles a subset of the recipes in the recipe category.
	 *
	 * @param recipeClass      the class of recipes to handle
	 * @param extensionFilter  a filter that returns true for instances of the recipe that can be handled by the extensionFactory
	 * @param extensionFactory a factory that can turn recipes into recipe extensions
	 * @since 7.2.0
	 *
	 * @deprecated extensions can be singletons now,
	 * use {@link IExtendableCraftingRecipeCategory#addExtension(Class, ICraftingCategoryExtension)}
	 * and {@link IRecipeCategoryExtension#isHandled(Object)}
	 */
	@Deprecated(since = "16.0.0", forRemoval = true)
	<R extends T> void addCategoryExtension(Class<? extends R> recipeClass, Predicate<R> extensionFilter, Function<R, ? extends W> extensionFactory);
}
