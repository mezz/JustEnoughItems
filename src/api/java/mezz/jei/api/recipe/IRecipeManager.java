package mezz.jei.api.recipe;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.List;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.runtime.IJeiRuntime;

/**
 * The {@link IRecipeManager} offers several functions for retrieving and handling recipes.
 * Get the instance from {@link IJeiRuntime#getRecipeManager()}.
 */
public interface IRecipeManager {
	/**
	 * Returns a list of Recipe Categories for the focus.
	 * @param focus an optional search focus to narrow the results on
	 * @param includeHidden set true to include recipe categories that are hidden or have no recipes.
	 * @since 7.7.1
	 */
	<V> List<IRecipeCategory<?>> getRecipeCategories(@Nullable IFocus<V> focus, boolean includeHidden);

	/**
	 * Returns a list of Recipe Categories for multiple focuses.
	 *
	 * @param focuses       an optional collection of search focus to narrow the results on
	 * @param includeHidden set true to include recipe categories that are hidden or have no recipes.
	 * @since 9.3.0
	 */
	List<IRecipeCategory<?>> getRecipeCategories(Collection<? extends IFocus<?>> focuses, boolean includeHidden);

	/**
	 * Returns a list of Recipe Categories for the focus
	 * @param recipeCategoryUids a list of recipe category uids to retrieve
	 * @param focus an optional search focus to narrow the results on
	 * @param includeHidden set true to include recipe categories that are hidden or have no recipes.
	 * @since 7.7.1
	 */
	<V> List<IRecipeCategory<?>> getRecipeCategories(Collection<ResourceLocation> recipeCategoryUids, @Nullable IFocus<V> focus, boolean includeHidden);

	/**
	 * Returns the recipe category for the given UID.
	 * Returns null if the recipe category does not exist.
	 * @since 7.7.1
	 */
	@Nullable
	IRecipeCategory<?> getRecipeCategory(ResourceLocation recipeCategoryUid, boolean includeHidden);

	/**
	 * Returns a list of recipes in the recipeCategory that have the given focuses.
	 *
	 * @param recipeCategory the recipe category to find recipes in
	 * @param focuses the current search focuses, or an empty list if there is no focus.
	 * @param includeHidden set true to include recipes that are hidden.
	 *
	 * @since 9.3.0
	 */
	<T> List<T> getRecipes(IRecipeCategory<T> recipeCategory, List<? extends IFocus<?>> focuses, boolean includeHidden);

	/**
	 * Returns an unmodifiable collection of ingredients that can craft the recipes from recipeCategory.
	 * For instance, the crafting table ItemStack is returned here for Crafting recipe category.
	 * These are registered with {@link IRecipeCatalystRegistration#addRecipeCatalyst(IIngredientType, Object, ResourceLocation...)}.
	 *
	 * @since 9.3.0
	 */
	List<ITypedIngredient<?>> getRecipeCatalystsTyped(IRecipeCategory<?> recipeCategory, boolean includeHidden);

	/**
	 * Returns a drawable recipe layout, for addons that want to draw the layouts somewhere.
	 * Layouts created this way do not have recipe transfer buttons, they are not useful for this purpose.
	 *
	 * @param recipeCategory the recipe category that the recipe belongs to
	 * @param recipe         the specific recipe to draw.
	 * @param focus          the focus of the recipe layout.
	 */
	<T> IRecipeLayoutDrawable createRecipeLayoutDrawable(IRecipeCategory<T> recipeCategory, T recipe, IFocus<?> focus);

	/**
	 * Hides a recipe so that it will not be displayed.
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipe            the recipe to hide.
	 * @param recipeCategoryUid the unique ID for the recipe category this recipe is a part of.
	 *                          See {@link VanillaRecipeCategoryUid} for vanilla recipe category UIDs.
	 * @see #unhideRecipe(Object, ResourceLocation)
	 */
	<T> void hideRecipe(T recipe, ResourceLocation recipeCategoryUid);

	/**
	 * Unhides a recipe that was hidden by {@link #hideRecipe(Object, ResourceLocation)}
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipe            the recipe to unhide.
	 * @param recipeCategoryUid the unique ID for the recipe category this recipe is a part of.
	 *                          See {@link VanillaRecipeCategoryUid} for vanilla recipe category UIDs.
	 * @see #hideRecipe(Object, ResourceLocation)
	 */
	<T> void unhideRecipe(T recipe, ResourceLocation recipeCategoryUid);

	/**
	 * Hide an entire recipe category of recipes from JEI.
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipeCategoryUid the unique ID for the recipe category
	 * @see #unhideRecipeCategory(ResourceLocation)
	 */
	void hideRecipeCategory(ResourceLocation recipeCategoryUid);

	/**
	 * Unhides a recipe category that was hidden by {@link #hideRecipeCategory(ResourceLocation)}.
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipeCategoryUid the unique ID for the recipe category
	 * @see #hideRecipeCategory(ResourceLocation)
	 */
	void unhideRecipeCategory(ResourceLocation recipeCategoryUid);

	/**
	 * Add a new recipe while the game is running.
	 */
	@Deprecated
	<T> void addRecipe(T recipe, ResourceLocation recipeCategoryUid);

	/**
	 * Returns a new focus.
	 *
	 * @deprecated Use {@link IJeiRuntime#createFocus(RecipeIngredientRole, IIngredientType, Object)} instead.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	<V> IFocus<V> createFocus(IFocus.Mode mode, V ingredient);

	/**
	 * Returns a list of recipes in the recipeCategory that have the focus.
	 * @param recipeCategory the recipe category to find recipes in
	 * @param focus the current search focus, or null if there is no focus.
	 * @param includeHidden set true to include recipes that are hidden.
	 * @since 7.7.1
	 *
	 * @deprecated Use {@link #getRecipes(IRecipeCategory, List, boolean)} instead.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	<T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, @Nullable IFocus<V> focus, boolean includeHidden);

	/**
	 * Returns an unmodifiable collection of ingredients that can craft the recipes from recipeCategory.
	 * For instance, the crafting table ItemStack is returned here for Crafting recipe category.
	 * These are registered with {@link IRecipeCatalystRegistration#addRecipeCatalyst(IIngredientType, Object, ResourceLocation...)}.
	 * @since 7.7.1
	 *
	 * @deprecated Use {@link #getRecipeCatalystsTyped(IRecipeCategory, boolean)} instead.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	List<Object> getRecipeCatalysts(IRecipeCategory<?> recipeCategory, boolean includeHidden);
}
