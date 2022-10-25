package mezz.jei.api.recipe;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * The {@link IRecipeManager} offers several functions for retrieving and handling recipes.
 * Get the instance from {@link IJeiRuntime#getRecipeManager()}.
 */
public interface IRecipeManager {
	/**
	 * Create a recipe lookup for the given recipe type.
	 *
	 * {@link IRecipeLookup} is a helper class that lets you choose
	 * the results you want, and then get them.
	 *
	 * @since 9.5.0
	 */
	<R> IRecipeLookup<R> createRecipeLookup(RecipeType<R> recipeType);

	/**
	 * Create a recipe category lookup for the given recipe type.
	 *
	 * {@link IRecipeCategoriesLookup} is a helper class that lets you choose
	 * the results you want, and then get them.
	 *
	 * @since 9.5.0
	 */
	IRecipeCategoriesLookup createRecipeCategoryLookup();

	/**
	 * Create a recipe catalyst lookup for the given recipe type.
	 *
	 * {@link IRecipeCatalystLookup} is a helper class that lets you choose
	 * the results you want, and then get them.
	 *
	 * @since 9.5.0
	 */
	IRecipeCatalystLookup createRecipeCatalystLookup(RecipeType<?> recipeType);

	/**
	 * Hides recipes so that they will not be displayed.
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipeType the recipe type for this recipe.
	 * @param recipes    the recipes to hide.
	 *
	 * @see #unhideRecipes(RecipeType, Collection)
	 * @see RecipeTypes for all the built-in recipe types that are added by JEI.
	 *
	 * @since 9.5.0
	 */
	<T> void hideRecipes(RecipeType<T> recipeType, Collection<T> recipes);

	/**
	 * Unhides recipes that were hidden by {@link #hideRecipes(RecipeType, Collection)}
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipeType the recipe type for this recipe.
	 * @param recipes    the recipes to unhide.
	 *
	 * @see #hideRecipes(RecipeType, Collection)
	 * @see RecipeTypes for all the built-in recipe types that are added by JEI.
	 *
	 * @since 9.5.0
	 */
	<T> void unhideRecipes(RecipeType<T> recipeType, Collection<T> recipes);

	/**
	 * Add new recipes while the game is running.
	 *
	 * @see RecipeTypes for all the built-in recipe types that are added by JEI.
	 *
	 * @since 9.5.0
	 */
	<T> void addRecipes(RecipeType<T> recipeType, List<T> recipes);

	/**
	 * Hide an entire recipe category of recipes from JEI.
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipeType the unique ID for the recipe category
	 * @see #unhideRecipeCategory(RecipeType)
	 *
	 * @since 9.5.0
	 */
	void hideRecipeCategory(RecipeType<?> recipeType);

	/**
	 * Unhides a recipe category that was hidden by {@link #hideRecipeCategory(RecipeType)}.
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipeType the unique ID for the recipe category
	 * @see #hideRecipeCategory(RecipeType)
	 *
	 * @since 9.5.0
	 */
	void unhideRecipeCategory(RecipeType<?> recipeType);

	/**
	 * Returns a drawable recipe layout, for addons that want to draw the layouts somewhere.
	 * Layouts created this way do not have recipe transfer buttons, they are not useful for this purpose.
	 *
	 * @param recipeCategory the recipe category that the recipe belongs to
	 * @param recipe         the specific recipe to draw.
	 * @param focus          the focus of the recipe layout.
	 */
	<T> IRecipeLayoutDrawable createRecipeLayoutDrawable(IRecipeCategory<T> recipeCategory, T recipe, @Nullable IFocus<?> focus);

	/**
	 * Get the registered recipe type for the given unique id.
	 * <p>
	 * This is useful for integrating with other mods that do not share their
	 * recipe types directly from their API.
	 *
	 * @see RecipeType#getUid()
	 * @since 11.2.3
	 */
	Optional<RecipeType<?>> getRecipeType(ResourceLocation uid);
}
